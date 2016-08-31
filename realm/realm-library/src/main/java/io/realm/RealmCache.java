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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.realm.exceptions.RealmFileException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.log.RealmLog;

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
        private final ThreadLocal<BaseRealm> localRealm = new ThreadLocal<BaseRealm>();
        // How many references to this Realm instance in this thread.
        private final ThreadLocal<Integer> localCount = new ThreadLocal<Integer>();
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
    // Separated references and counters for typed Realm and dynamic Realm.
    private final EnumMap<RealmCacheType, RefAndCount> refAndCountMap;

    final private RealmConfiguration configuration;

    // Column indices are cached to speed up opening typed Realm. If a Realm instance is created in one thread, creating
    // Realm instances in other threads doesn't have to initialize the column indices again.
    private ColumnIndices typedColumnIndices;

    // Realm path will be used as the key to store different RealmCaches. Different Realm configurations with same path
    // are not allowed and an exception will be thrown when trying to add it to the cache map.
    private static Map<String, RealmCache> cachesMap = new HashMap<String, RealmCache>();

    private static final String DIFFERENT_KEY_MESSAGE = "Wrong key used to decrypt Realm.";
    private static final String WRONG_REALM_CLASS_MESSAGE = "The type of Realm class must be Realm or DynamicRealm.";

    private RealmCache(RealmConfiguration config) {
        configuration = config;
        refAndCountMap = new EnumMap<RealmCacheType, RefAndCount>(RealmCacheType.class);
        for (RealmCacheType type : RealmCacheType.values()) {
            refAndCountMap.put(type, new RefAndCount());
        }
    }

    /**
     * Creates a new Realm instance or get an existing instance for current thread.
     *
     * @param configuration {@link RealmConfiguration} will be used to create or get the instance.
     * @param realmClass class of {@link Realm} or {@link DynamicRealm} to be created in or gotten from the cache.
     * @return the {@link Realm} or {@link DynamicRealm} instance.
     */
    static synchronized <E extends BaseRealm> E createRealmOrGetFromCache(RealmConfiguration configuration,
                                                        Class<E> realmClass) {
        boolean isCacheInMap = true;
        RealmCache cache = cachesMap.get(configuration.getPath());
        if (cache == null) {
            // Create a new cache
            cache = new RealmCache(configuration);
            // The new cache should be added to the map later.
            isCacheInMap = false;

            copyAssetFileIfNeeded(configuration);
        } else {
            // Throw the exception if validation failed.
            cache.validateConfiguration(configuration);
        }

        RefAndCount refAndCount = cache.refAndCountMap.get(RealmCacheType.valueOf(realmClass));

        if (refAndCount.localRealm.get() == null) {
            // Create a new local Realm instance
            BaseRealm realm;


            if (realmClass == Realm.class) {
                // RealmMigrationNeededException might be thrown here.
                realm = Realm.createInstance(configuration, cache.typedColumnIndices);
            } else if (realmClass == DynamicRealm.class) {
                realm = DynamicRealm.createInstance(configuration);
            } else {
                throw new IllegalArgumentException(WRONG_REALM_CLASS_MESSAGE);
            }

            // The Realm instance has been created without exceptions. Cache and reference count can be updated now.

            // The cache is not in the map yet. Add it to the map after the Realm instance created successfully.
            if (!isCacheInMap) {
                cachesMap.put(configuration.getPath(), cache);
            }
            refAndCount.localRealm.set(realm);
            refAndCount.localCount.set(0);
        }

        Integer refCount = refAndCount.localCount.get();
        if (refCount == 0) {
            if (realmClass == Realm.class && refAndCount.globalCount == 0) {
                cache.typedColumnIndices = refAndCount.localRealm.get().schema.columnIndices;
            }
            // This is the first instance in current thread, increase the global count.
            refAndCount.globalCount++;
        }
        refAndCount.localCount.set(refCount + 1);

        @SuppressWarnings("unchecked")
        E realm = (E) refAndCount.localRealm.get();
        return realm;
    }

    /**
     * Releases a given {@link Realm} or {@link DynamicRealm} from cache. The instance will be closed by this method
     * if there is no more local reference to this Realm instance in current Thread.
     *
     * @param realm Realm instance to be released from cache.
     */
    static synchronized void release(BaseRealm realm) {
        String canonicalPath = realm.getPath();
        RealmCache cache = cachesMap.get(canonicalPath);
        Integer refCount = null;
        RefAndCount refAndCount = null;

        if (cache != null) {
            refAndCount = cache.refAndCountMap.get(RealmCacheType.valueOf(realm.getClass()));
            refCount = refAndCount.localCount.get();
        }
        if (refCount == null) {
            refCount = 0;
        }

        if (refCount <= 0) {
            RealmLog.w("Realm " + canonicalPath + " has been closed already.");
            return;
        }

        // Decrease the local counter.
        refCount -= 1;

        if (refCount == 0) {
            // The last instance in this thread.
            // Clear local ref & counter
            refAndCount.localCount.set(null);
            refAndCount.localRealm.set(null);

            // Clear global counter
            refAndCount.globalCount--;
            if (refAndCount.globalCount < 0) {
                // Should never happen.
                throw new IllegalStateException("Global reference counter of Realm" + canonicalPath +
                        " got corrupted.");
            }

            // Clear the column indices cache if needed
            if (realm instanceof Realm && refAndCount.globalCount == 0) {
                // All typed Realm instances of this file are cleared from cache
                cache.typedColumnIndices = null;
            }

            int totalRefCount = 0;
            for (RealmCacheType type : RealmCacheType.values()) {
                totalRefCount += cache.refAndCountMap.get(type).globalCount;
            }
            // No more instance of typed Realm and dynamic Realm. Remove the configuration from cache.
            if (totalRefCount == 0) {
                cachesMap.remove(canonicalPath);
            }

            // No more local reference to this Realm in current thread, close the instance.
            realm.doClose();
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
            // Same configuration objects
            return;
        }

        // Check that encryption keys aren't different. key is not in RealmConfiguration's toString.
        if (!Arrays.equals(configuration.getEncryptionKey(), newConfiguration.getEncryptionKey())) {
            throw new IllegalArgumentException(DIFFERENT_KEY_MESSAGE);
        } else {
            // A common problem is that people are forgetting to override `equals` in their custom migration class.
            // Try to detect this problem specifically so we can throw a better error message.
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
    static synchronized void invokeWithGlobalRefCount(RealmConfiguration configuration, Callback callback) {
        RealmCache cache = cachesMap.get(configuration.getPath());
        if (cache == null) {
            callback.onResult(0);
            return;
        }
        int totalRefCount = 0;
        for (RealmCacheType type : RealmCacheType.values()) {
            totalRefCount += cache.refAndCountMap.get(type).globalCount;
        }
        callback.onResult(totalRefCount);
    }

   /**
     * Runs the callback function with synchronization on {@link RealmCache}.
     *
     * @param callback the callback will be executed.
     */
    static synchronized void invokeWithLock(Callback0 callback) {
        callback.onCall();
    }

    /**
     * Copies Realm database file from Android asset directory to the directory given in the {@link RealmConfiguration}.
     * Copy is performed only at the first time when there is no Realm database file.
     *
     * @param configuration configuration object for Realm instance.
     * @throws RealmFileException if copying the file fails.
     */
    private static void copyAssetFileIfNeeded(RealmConfiguration configuration) {
        IOException exceptionWhenClose = null;
        if (configuration.hasAssetFile()) {
            File realmFile = new File(configuration.getRealmDirectory(), configuration.getRealmFileName());
            if (realmFile.exists()) {
                return;
            }

            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = configuration.getAssetFile();
                if (inputStream == null) {
                    throw new RealmFileException(RealmFileException.Kind.ACCESS_ERROR,
                            "Invalid input stream to asset file.");
                }

                outputStream = new FileOutputStream(realmFile);
                byte[] buf = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > -1) {
                    outputStream.write(buf, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new RealmFileException(RealmFileException.Kind.ACCESS_ERROR,
                        "Could not resolve the path to the Realm asset file.", e);
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
                        // Ignore this one if there was an exception when close inputStream.
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
    }
}
