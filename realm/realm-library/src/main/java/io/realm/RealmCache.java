/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.exceptions.RealmFileException;
import io.realm.internal.Capabilities;
import io.realm.internal.ObjectServerFacade;
import io.realm.internal.OsObjectStore;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmNotifier;
import io.realm.internal.Table;
import io.realm.internal.Util;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.log.RealmLog;


/**
 * To cache {@link Realm}, {@link DynamicRealm} instances and related resources.
 * Every thread will share the same {@link Realm} and {@link DynamicRealm} instances which are referred to the same
 * {@link RealmConfiguration}.
 * One {@link RealmCache} is created for each {@link RealmConfiguration}, and it caches all the {@link Realm} and
 * {@link DynamicRealm} instances which are created from the same {@link RealmConfiguration}.
 */
final class RealmCache {

    interface Callback {
        void onResult(int count);
    }

    interface Callback0 {
        void onCall();
    }

    private static class RefAndCount {
        // The Realm instance in this thread.
        private final ThreadLocal<BaseRealm> localRealm = new ThreadLocal<>();
        // How many references to this Realm instance in this thread.
        private final ThreadLocal<Integer> localCount = new ThreadLocal<>();
        // How many threads have instances refer to this configuration.
        private int globalCount = 0;
    }

    private enum RealmCacheType {
        TYPED_REALM,
        DYNAMIC_REALM;

        static RealmCacheType valueOf(Class<? extends BaseRealm> clazz) {
            if (clazz == Realm.class) {
                return TYPED_REALM;
            } else if (clazz == DynamicRealm.class) {
                return DYNAMIC_REALM;
            }

            throw new IllegalArgumentException(WRONG_REALM_CLASS_MESSAGE);
        }
    }

    private static class CreateRealmRunnable<T extends BaseRealm> implements Runnable {
        private final RealmConfiguration configuration;
        private final BaseRealm.InstanceCallback<T> callback;
        private final Class<T> realmClass;
        private final CountDownLatch canReleaseBackgroundInstanceLatch = new CountDownLatch(1);
        private final RealmNotifier notifier;
        // The Future this runnable belongs to.
        private Future future;

        CreateRealmRunnable(RealmNotifier notifier, RealmConfiguration configuration,
                BaseRealm.InstanceCallback<T> callback, Class<T> realmClass) {
            this.configuration = configuration;
            this.realmClass = realmClass;
            this.callback = callback;
            this.notifier = notifier;
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            T instance = null;
            try {
                // First call that will run all schema validation, migrations or initial transactions.
                instance = createRealmOrGetFromCache(configuration, realmClass);
                boolean results = notifier.post(new Runnable() {
                    @Override
                    public void run() {
                        // If the RealmAsyncTask.cancel() is called before, we just return without creating the Realm
                        // instance on the caller thread.
                        // Thread.isInterrupted() cannot be used for checking here since CountDownLatch.await() will
                        // will clear interrupted status.
                        // Using the future to check which this runnable belongs to is to ensure if it is canceled from
                        // the caller thread before, the callback will never be delivered.
                        if (future == null || future.isCancelled()) {
                            canReleaseBackgroundInstanceLatch.countDown();
                            return;
                        }
                        T instanceToReturn = null;
                        Throwable throwable = null;
                        try {
                            // This will run on the caller thread, but since the first `createRealmOrGetFromCache`
                            // should have completed at this point, all expensive initializer functions have already
                            // run.
                            instanceToReturn = createRealmOrGetFromCache(configuration, realmClass);
                        } catch (Throwable e) {
                            throwable = e;
                        } finally {
                            canReleaseBackgroundInstanceLatch.countDown();
                        }
                        if (instanceToReturn != null) {
                            callback.onSuccess(instanceToReturn);
                        } else {
                            // throwable is non-null
                            //noinspection ConstantConditions
                            callback.onError(throwable);
                        }
                    }
                });
                if (!results) {
                    canReleaseBackgroundInstanceLatch.countDown();
                }
                // There is a small chance that the posted runnable cannot be executed because of the thread terminated
                // before the runnable gets fetched from the event queue.
                if (!canReleaseBackgroundInstanceLatch.await(2, TimeUnit.SECONDS)) {
                    RealmLog.warn("Timeout for creating Realm instance in foreground thread in `CreateRealmRunnable` ");
                }
            } catch (InterruptedException e) {
                RealmLog.warn(e, "`CreateRealmRunnable` has been interrupted.");
            } catch (final Throwable e) {
                // DownloadingRealmInterruptedException is treated specially.
                // It async open is canceled, this could interrupt the download, but the user should
                // not care in this case, so just ignore it.
                if (!ObjectServerFacade.getSyncFacadeIfPossible().wasDownloadInterrupted(e)) {
                    RealmLog.error(e, "`CreateRealmRunnable` failed.");
                    notifier.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e);
                        }
                    });
                }
            } finally {
                if (instance != null) {
                    instance.close();
                }
            }
        }
    }

    private static final String ASYNC_NOT_ALLOWED_MSG =
            "Realm instances cannot be loaded asynchronously on a non-looper thread.";
    private static final String ASYNC_CALLBACK_NULL_MSG =
            "The callback cannot be null.";

    // Separated references and counters for typed Realm and dynamic Realm.
    private final EnumMap<RealmCacheType, RefAndCount> refAndCountMap;

    // Path to the Realm file to identify this cache.
    private final String realmPath;

    // This will be only valid if getTotalGlobalRefCount() > 0.
    // NOTE: We do reset this when globalCount reaches 0, but if exception thrown in doCreateRealmOrGetFromCache at the
    // first time when globalCount == 0, this could have a non-null value but it will be reset when the next
    // doCreateRealmOrGetFromCache is called with globalCount == 0.
    private RealmConfiguration configuration;

    // Realm path will be used to identify different RealmCaches. Different Realm configurations with same path
    // are not allowed and an exception will be thrown when trying to add it to the cache list.
    // A weak ref is used to hold the RealmCache instance. The weak ref entry will be cleared if and only if there
    // is no Realm instance holding a strong ref to it and there is no Realm instance associated it is BEING created.
    private static final List<WeakReference<RealmCache>> cachesList = new ArrayList<WeakReference<RealmCache>>();

    // See leak()
    // isLeaked flag is used to avoid adding strong ref multiple times without iterating the list.
    private final AtomicBoolean isLeaked = new AtomicBoolean(false);
    // Keep strong ref to the leaked RealmCache
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Collection<RealmCache> leakedCaches = new ConcurrentLinkedQueue<RealmCache>();

    private static final String DIFFERENT_KEY_MESSAGE = "Wrong key used to decrypt Realm.";
    private static final String WRONG_REALM_CLASS_MESSAGE = "The type of Realm class must be Realm or DynamicRealm.";

    private RealmCache(String path) {
        realmPath = path;
        refAndCountMap = new EnumMap<>(RealmCacheType.class);
        for (RealmCacheType type : RealmCacheType.values()) {
            refAndCountMap.put(type, new RefAndCount());
        }
    }

    private static RealmCache getCache(String realmPath, boolean createIfNotExist) {
        RealmCache cacheToReturn = null;
        synchronized (cachesList) {
            Iterator<WeakReference<RealmCache>> it = cachesList.iterator();

            while (it.hasNext()) {
                RealmCache cache = it.next().get();
                if (cache == null) {
                    // Clear the entry if there is no one holding the RealmCache.
                    it.remove();
                } else if (cache.realmPath.equals(realmPath)) {
                    cacheToReturn = cache;
                }
            }

            if (cacheToReturn == null && createIfNotExist) {
                cacheToReturn = new RealmCache(realmPath);
                cachesList.add(new WeakReference<RealmCache>(cacheToReturn));
            }
        }
        return cacheToReturn;
    }

    static <T extends BaseRealm> RealmAsyncTask createRealmOrGetFromCacheAsync(
            RealmConfiguration configuration, BaseRealm.InstanceCallback<T> callback, Class<T> realmClass) {
        RealmCache cache = getCache(configuration.getPath(), true);
        return cache.doCreateRealmOrGetFromCacheAsync(configuration, callback, realmClass);
    }

    private synchronized <T extends BaseRealm> RealmAsyncTask doCreateRealmOrGetFromCacheAsync(
            RealmConfiguration configuration, BaseRealm.InstanceCallback<T> callback, Class<T> realmClass) {
        Capabilities capabilities = new AndroidCapabilities();
        capabilities.checkCanDeliverNotification(ASYNC_NOT_ALLOWED_MSG);
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException(ASYNC_CALLBACK_NULL_MSG);
        }

        // Always create a Realm instance in the background thread even when there are instances existing on current
        // thread. This to ensure that onSuccess will always be called in the following event loop but not current one.
        CreateRealmRunnable<T> createRealmRunnable = new CreateRealmRunnable<T>(
                new AndroidRealmNotifier(null, capabilities), configuration, callback, realmClass);
        Future<?> future = BaseRealm.asyncTaskExecutor.submitTransaction(createRealmRunnable);
        createRealmRunnable.setFuture(future);

        return new RealmAsyncTaskImpl(future, BaseRealm.asyncTaskExecutor);
    }

    /**
     * Creates a new Realm instance or get an existing instance for current thread.
     *
     * @param configuration {@link RealmConfiguration} will be used to create or get the instance.
     * @param realmClass class of {@link Realm} or {@link DynamicRealm} to be created in or gotten from the cache.
     * @return the {@link Realm} or {@link DynamicRealm} instance.
     */
    static <E extends BaseRealm> E createRealmOrGetFromCache(RealmConfiguration configuration,
            Class<E> realmClass) {
        RealmCache cache = getCache(configuration.getPath(), true);

        return cache.doCreateRealmOrGetFromCache(configuration, realmClass);
    }

    private synchronized <E extends BaseRealm> E doCreateRealmOrGetFromCache(RealmConfiguration configuration,
            Class<E> realmClass) {

        RefAndCount refAndCount = refAndCountMap.get(RealmCacheType.valueOf(realmClass));

        if (getTotalGlobalRefCount() == 0) {
            copyAssetFileIfNeeded(configuration);
            boolean fileExists = configuration.realmExists();

            OsSharedRealm sharedRealm = null;
            try {
                if (configuration.isSyncConfiguration()) {
                    // If waitForInitialRemoteData() was enabled, we need to make sure that all data is downloaded
                    // before proceeding. We need to open the Realm instance first to start any potential underlying
                    // SyncSession so this will work. TODO: This needs to be decoupled.
                    if (!fileExists) {
                        sharedRealm = OsSharedRealm.getInstance(configuration);
                        try {
                            ObjectServerFacade.getSyncFacadeIfPossible().downloadRemoteChanges(configuration);
                        } catch (Throwable t) {
                            // If an error happened while downloading initial data, we need to reset the file so we can
                            // download it again on the next attempt.
                            sharedRealm.close();
                            sharedRealm = null;
                            // FIXME: We don't have a way to ensure that the Realm instance on client thread has been
                            //        closed for now.
                            // https://github.com/realm/realm-java/issues/5416
                            BaseRealm.deleteRealm(configuration);
                            throw t;
                        }
                    }
                } else {
                    if (fileExists) {
                        // Primary key problem only exists before we release sync.
                        sharedRealm = OsSharedRealm.getInstance(configuration);
                        Table.migratePrimaryKeyTableIfNeeded(sharedRealm);
                    }
                }
            } finally {
                if (sharedRealm != null) {
                    sharedRealm.close();
                }
            }

            // We are holding the lock, and we can set the invalidated configuration since there is no global ref to it.
            this.configuration = configuration;
        } else {
            // Throws exception if validation failed.
            validateConfiguration(configuration);
        }

        if (refAndCount.localRealm.get() == null) {
            // Creates a new local Realm instance
            BaseRealm realm;

            if (realmClass == Realm.class) {
                // RealmMigrationNeededException might be thrown here.
                realm = Realm.createInstance(this);
            } else if (realmClass == DynamicRealm.class) {
                realm = DynamicRealm.createInstance(this);
            } else {
                throw new IllegalArgumentException(WRONG_REALM_CLASS_MESSAGE);
            }

            // The Realm instance has been created without exceptions. Cache and reference count can be updated now.
            refAndCount.localRealm.set(realm);
            refAndCount.localCount.set(0);

            // This is the first instance in current thread, increase the global count.
            refAndCount.globalCount++;
        }

        Integer refCount = refAndCount.localCount.get();
        refAndCount.localCount.set(refCount + 1);

        //noinspection unchecked
        return (E) refAndCount.localRealm.get();
    }

    /**
     * Releases a given {@link Realm} or {@link DynamicRealm} from cache. The instance will be closed by this method
     * if there is no more local reference to this Realm instance in current Thread.
     *
     * @param realm Realm instance to be released from cache.
     */
    synchronized void release(BaseRealm realm) {
        String canonicalPath = realm.getPath();
        RefAndCount refAndCount = refAndCountMap.get(RealmCacheType.valueOf(realm.getClass()));
        Integer refCount = refAndCount.localCount.get();
        if (refCount == null) {
            refCount = 0;
        }

        if (refCount <= 0) {
            RealmLog.warn("%s has been closed already. refCount is %s", canonicalPath, refCount);
            return;
        }

        // Decreases the local counter.
        refCount -= 1;

        if (refCount == 0) {
            // The last instance in this thread.
            // Clears local ref & counter.
            refAndCount.localCount.set(null);
            refAndCount.localRealm.set(null);

            // Clears global counter.
            refAndCount.globalCount--;
            if (refAndCount.globalCount < 0) {
                // Should never happen.
                throw new IllegalStateException("Global reference counter of Realm" + canonicalPath +
                        " got corrupted.");
            }

            // No more local reference to this Realm in current thread, close the instance.
            realm.doClose();

            // No more instance of typed Realm and dynamic Realm.
            if (getTotalGlobalRefCount() == 0) {
                // We keep the cache in the caches list even when its global counter reaches 0. It will be reused when
                // next time a Realm instance with the same path is opened. By not removing it, the lock on
                // cachesList is not needed here.
                configuration = null;
                ObjectServerFacade.getFacade(realm.getConfiguration().isSyncConfiguration())
                        .realmClosed(realm.getConfiguration());
            }

        } else {
            refAndCount.localCount.set(refCount);
        }
    }

    /**
     * Makes sure that the new configuration doesn't clash with any cached configurations for the
     * Realm.
     *
     * @throws IllegalArgumentException if the new configuration isn't valid.
     */
    private void validateConfiguration(RealmConfiguration newConfiguration) {
        if (configuration.equals(newConfiguration)) {
            // Same configuration objects.
            return;
        }

        // Checks that encryption keys aren't different. key is not in RealmConfiguration's toString.
        if (!Arrays.equals(configuration.getEncryptionKey(), newConfiguration.getEncryptionKey())) {
            throw new IllegalArgumentException(DIFFERENT_KEY_MESSAGE);
        } else {
            // A common problem is that people are forgetting to override `equals` in their custom migration class.
            // Tries to detect this problem specifically so we can throw a better error message.
            RealmMigration newMigration = newConfiguration.getMigration();
            RealmMigration oldMigration = configuration.getMigration();
            if (oldMigration != null
                    && newMigration != null
                    && oldMigration.getClass().equals(newMigration.getClass())
                    && !newMigration.equals(oldMigration)) {
                throw new IllegalArgumentException("Configurations cannot be different if used to open the same file. " +
                        "The most likely cause is that equals() and hashCode() are not overridden in the " +
                        "migration class: " + newConfiguration.getMigration().getClass().getCanonicalName());
            }

            throw new IllegalArgumentException("Configurations cannot be different if used to open the same file. " +
                    "\nCached configuration: \n" + configuration +
                    "\n\nNew configuration: \n" + newConfiguration);
        }
    }

    /**
     * Runs the callback function with the total reference count of {@link Realm} and {@link DynamicRealm} who refer to
     * the given {@link RealmConfiguration}.
     *
     * @param configuration the {@link RealmConfiguration} of {@link Realm} or {@link DynamicRealm}.
     * @param callback the callback will be executed with the global reference count.
     */
    static void invokeWithGlobalRefCount(RealmConfiguration configuration, Callback callback) {
        // NOTE: Although getCache is locked on the cacheMap, this whole method needs to be lock with it as
        // well. Since we need to ensure there is no Realm instance can be opened when this method is called (for
        // deleteRealm).
        // Recursive lock cannot be avoided here.
        synchronized (cachesList) {
            RealmCache cache = getCache(configuration.getPath(), false);
            if (cache == null) {
                callback.onResult(0);
                return;
            }
            cache.doInvokeWithGlobalRefCount(callback);
        }
    }

    private synchronized void doInvokeWithGlobalRefCount(Callback callback) {
        callback.onResult(getTotalGlobalRefCount());
    }

    /**
     * Runs the callback function with synchronization on {@link RealmCache}.
     *
     * @param callback the callback will be executed.
     */
    synchronized void invokeWithLock(Callback0 callback) {
        callback.onCall();
    }

    /**
     * Copies Realm database file from Android asset directory to the directory given in the {@link RealmConfiguration}.
     * Copy is performed only at the first time when there is no Realm database file.
     *
     * WARNING: This method is not thread-safe so external synchronization is required before using it.
     *
     * @param configuration configuration object for Realm instance.
     * @throws RealmFileException if copying the file fails.
     */
    private static void copyAssetFileIfNeeded(final RealmConfiguration configuration) {
        final File realmFileFromAsset = configuration.hasAssetFile() ?
                new File(configuration.getRealmDirectory(), configuration.getRealmFileName())
                : null;
        final String syncServerCertificateAssetName = ObjectServerFacade.getFacade(
                configuration.isSyncConfiguration()).getSyncServerCertificateAssetName(configuration);
        final boolean certFileExists = !Util.isEmptyString(syncServerCertificateAssetName);

        if (realmFileFromAsset!= null || certFileExists) {
            OsObjectStore.callWithLock(configuration, new Runnable() {
                @Override
                public void run() {
                    if (realmFileFromAsset != null) {
                        copyFileIfNeeded(configuration.getAssetFilePath(), realmFileFromAsset);
                    }

                    // Copy Sync Server certificate path if available
                    if (certFileExists) {
                        String syncServerCertificateFilePath = ObjectServerFacade.getFacade(
                                configuration.isSyncConfiguration()).getSyncServerCertificateFilePath(configuration);

                        File certificateFile = new File(syncServerCertificateFilePath);
                        copyFileIfNeeded(syncServerCertificateAssetName, certificateFile);
                    }
                }
            });
        }
    }

    private static void copyFileIfNeeded(String assetFileName, File file) {
        if (file.exists()) {
            return;
        }

        IOException exceptionWhenClose = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = BaseRealm.applicationContext.getAssets().open(assetFileName);
            if (inputStream == null) {
                throw new RealmFileException(RealmFileException.Kind.ACCESS_ERROR,
                        "Invalid input stream to the asset file: " + assetFileName);
            }

            outputStream = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > -1) {
                outputStream.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RealmFileException(RealmFileException.Kind.ACCESS_ERROR,
                    "Could not resolve the path to the asset file: " + assetFileName, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    exceptionWhenClose = e;
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Ignores this one if there was an exception when close inputStream.
                    if (exceptionWhenClose == null) {
                        exceptionWhenClose = e;
                    }
                }
            }
        }

        // No other exception has been thrown, only the exception when close. So, throw it.
        if (exceptionWhenClose != null) {
            throw new RealmFileException(RealmFileException.Kind.ACCESS_ERROR, exceptionWhenClose);
        }
    }

    static int getLocalThreadCount(RealmConfiguration configuration) {
        RealmCache cache = getCache(configuration.getPath(), false);
        if (cache == null) {
            return 0;
        }

        // Access local ref count only, no need to by synchronized.
        int totalRefCount = 0;
        for (RefAndCount refAndCount : cache.refAndCountMap.values()) {
            Integer localCount = refAndCount.localCount.get();
            totalRefCount += (localCount != null) ? localCount : 0;
        }
        return totalRefCount;
    }

    public RealmConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @return the total global ref count.
     */
    private int getTotalGlobalRefCount() {
        int totalRefCount = 0;
        for (RefAndCount refAndCount : refAndCountMap.values()) {
            totalRefCount += refAndCount.globalCount;
        }

        return totalRefCount;
    }

    /**
     * If a Realm instance is GCed but `Realm.close()` is not called before, we still want to track the cache for
     * debugging. Adding them to the list to keep the strong ref of the cache to prevent the cache gets GCed.
     */
    void leak() {
        if (!isLeaked.getAndSet(true)) {
            leakedCaches.add(this);
        }
    }
}
