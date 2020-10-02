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

import android.content.Context;
import android.os.Looper;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.CheckedRow;
import io.realm.internal.ColumnInfo;
import io.realm.internal.InvalidRow;
import io.realm.internal.OsObjectStore;
import io.realm.internal.OsRealmConfig;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.Util;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.log.RealmLog;

/**
 * Base class for all Realm instances.
 *
 * @see io.realm.Realm
 * @see io.realm.DynamicRealm
 */
@SuppressWarnings("WeakerAccess")
abstract class BaseRealm implements Closeable {
    private static final String INCORRECT_THREAD_CLOSE_MESSAGE =
            "Realm access from incorrect thread. Realm instance can only be closed on the thread it was created.";
    static final String INCORRECT_THREAD_MESSAGE =
            "Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.";
    static final String CLOSED_REALM_MESSAGE =
            "This Realm instance has already been closed, making it unusable.";
    private static final String NOT_IN_TRANSACTION_MESSAGE =
            "Changing Realm data can only be done from inside a transaction.";
    static final String LISTENER_NOT_ALLOWED_MESSAGE =
            "Listeners cannot be used on current thread.";
    static final String DELETE_NOT_SUPPORTED_UNDER_PARTIAL_SYNC =
            "This API is not supported by partially " +
            "synchronized Realms. Either unsubscribe using 'Realm.unsubscribeAsync()' or " +
            "delete the objects using a query and 'RealmResults.deleteAllFromRealm()'";

    static volatile Context applicationContext;

    // Thread pool for all async operations (Query & transaction)
    static final RealmThreadPoolExecutor asyncTaskExecutor = RealmThreadPoolExecutor.newDefaultExecutor();

    /**
     * Thread pool executor used for write operations - only one thread is needed as writes cannot
     * be parallelized.
     */
    public static final RealmThreadPoolExecutor WRITE_EXECUTOR = RealmThreadPoolExecutor.newSingleThreadExecutor();

    final boolean frozen; // Cache the value in Java, since it is accessed frequently and doesn't change.
    final long threadId;
    protected final RealmConfiguration configuration;
    // Which RealmCache is this Realm associated to. It is null if the Realm instance is opened without being put into a
    // cache. It is also null if the Realm is closed.
    private RealmCache realmCache;
    public OsSharedRealm sharedRealm;
    private boolean shouldCloseSharedRealm;
    private OsSharedRealm.SchemaChangedCallback schemaChangedCallback = new OsSharedRealm.SchemaChangedCallback() {
        @Override
        public void onSchemaChanged() {
            RealmSchema schema = getSchema();
            if (schema != null) {
                schema.refresh();
            }
        }
    };

    // Create a realm instance and associate it to a RealmCache.
    BaseRealm(RealmCache cache, @Nullable OsSchemaInfo schemaInfo, OsSharedRealm.VersionID version) {
        this(cache.getConfiguration(), schemaInfo, version);
        this.realmCache = cache;
    }

    // Create a realm instance without associating it to any RealmCache.
    BaseRealm(final RealmConfiguration configuration, @Nullable OsSchemaInfo schemaInfo, OsSharedRealm.VersionID version) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = configuration;
        this.realmCache = null;

        OsSharedRealm.MigrationCallback migrationCallback = null;
        if (schemaInfo != null && configuration.getMigration() != null) {
            migrationCallback = createMigrationCallback(configuration.getMigration());
        }

        OsSharedRealm.InitializationCallback initializationCallback = null;
        final Realm.Transaction initialDataTransaction = configuration.getInitialDataTransaction();
        if (initialDataTransaction != null) {
            initializationCallback = new OsSharedRealm.InitializationCallback() {
                @Override
                public void onInit(OsSharedRealm sharedRealm) {
                    Realm instance = Realm.createInstance(sharedRealm);
                    initialDataTransaction.execute(instance);
                }
            };
        }

        OsRealmConfig.Builder configBuilder = new OsRealmConfig.Builder(configuration)
                .fifoFallbackDir(new File(BaseRealm.applicationContext.getFilesDir(), ".realm.temp"))
                .autoUpdateNotification(true)
                .migrationCallback(migrationCallback)
                .schemaInfo(schemaInfo)
                .initializationCallback(initializationCallback);
        this.sharedRealm = OsSharedRealm.getInstance(configBuilder, version);
        this.frozen = sharedRealm.isFrozen();
        this.shouldCloseSharedRealm = true;
        sharedRealm.registerSchemaChangedCallback(schemaChangedCallback);
    }

    // Create a realm instance directly from a OsSharedRealm instance. This instance doesn't have the ownership of the
    // given OsSharedRealm instance. The OsSharedRealm instance should not be closed when close() called.
    BaseRealm(OsSharedRealm sharedRealm) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = sharedRealm.getConfiguration();
        this.realmCache = null;

        this.sharedRealm = sharedRealm;
        this.frozen = sharedRealm.isFrozen();
        this.shouldCloseSharedRealm = false;
    }

   /**
     * Sets the auto-refresh status of the Realm instance.
     * <p>
     * Auto-refresh is a feature that enables automatic update of the current Realm instance and all its derived objects
     * (RealmResults and RealmObject instances) when a commit is performed on a Realm acting on the same file in
     * another thread. This feature is only available if the Realm instance lives on a {@link android.os.Looper} enabled
     * thread.
     *
     * @param autoRefresh {@code true} will turn auto-refresh on, {@code false} will turn it off.
     * @throws IllegalStateException if called from a non-Looper thread.
     */
    public void setAutoRefresh(boolean autoRefresh) {
        checkIfValid();
        sharedRealm.setAutoRefresh(autoRefresh);
    }

    /**
     * Retrieves the auto-refresh status of the Realm instance.
     *
     * @return the auto-refresh status.
     */
    public boolean isAutoRefresh() {
        return sharedRealm.isAutoRefresh();
    }

    /**
     * Refreshes the Realm instance and all the RealmResults and RealmObjects instances coming from it.
     * It also calls any listeners associated with the Realm if needed.
     * <p>
     * WARNING: Calling this on a thread with async queries will turn those queries into synchronous queries.
     * This means this method will throw a {@link RealmException} if
     * {@link RealmConfiguration.Builder#allowQueriesOnUiThread(boolean)} was used with {@code true} to
     * obtain a Realm instance. In most cases it is better to use {@link RealmChangeListener}s to be notified
     * about changes to the Realm on a given thread than it is to use this method.
     *
     * @throws IllegalStateException if attempting to refresh from within a transaction.
     * @throws RealmException if called from the UI thread after opting out via {@link RealmConfiguration.Builder#allowQueriesOnUiThread(boolean)}.
     */
    public void refresh() {
        checkIfValid();
        checkAllowQueriesOnUiThread();

        if (isInTransaction()) {
            throw new IllegalStateException("Cannot refresh a Realm instance inside a transaction.");
        }
        sharedRealm.refresh();
    }

    /**
     * Checks if the Realm is currently in a transaction.
     *
     * @return {@code true} if inside a transaction, {@code false} otherwise.
     */
    public boolean isInTransaction() {
        checkIfValid();
        return sharedRealm.isInTransaction();
    }

    protected <T extends BaseRealm> void addListener(RealmChangeListener<T> listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        checkIfValid();
        sharedRealm.capabilities.checkCanDeliverNotification(LISTENER_NOT_ALLOWED_MESSAGE);
        if (frozen) {
            throw new IllegalStateException("It is not possible to add a change listener to a frozen Realm since it never changes.");
        }
        //noinspection unchecked
        sharedRealm.realmNotifier.addChangeListener((T) this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    protected <T extends BaseRealm> void removeListener(RealmChangeListener<T> listener) {
        //noinspection ConstantConditions
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (isClosed()) {
            RealmLog.warn("Calling removeChangeListener on a closed Realm %s, " +
                    "make sure to close all listeners before closing the Realm.", configuration.getPath());
        }
        //noinspection unchecked
        sharedRealm.realmNotifier.removeChangeListener((T) this, listener);
    }

    /**
     * Returns an RxJava Flowable that monitors changes to this Realm. It will emit the current state
     * when subscribed to. Items will continually be emitted as the Realm is updated -
     * {@code onComplete} will never be called.
     * <p>
     * Items emitted from Realm Flowables are frozen (See {@link #freeze()}. This means that they
     * are immutable and can be read on any thread.
     * <p>
     * Realm Flowables always emit items from the thread holding the live Realm. This means that if
     * you need to do further processing, it is recommend to observe the values on a computation
     * scheduler:
     * <p>
     * {@code
     * realm.asFlowable()
     *   .observeOn(Schedulers.computation())
     *   .map(rxRealm -> doExpensiveWork(rxRealm))
     *   .observeOn(AndroidSchedulers.mainThread())
     *   .subscribe( ... );
     * }
     * <p>
     * If you would like the {@code asFlowable()} to stop emitting items, you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * realm.asFlowable().first().subscribe( ... ); // You only get the results once
     * }
     * </pre>
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
     * @throws IllegalStateException if the Realm wasn't opened on a Looper thread.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public abstract Flowable asFlowable();

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    protected void removeAllListeners() {
        if (isClosed()) {
            RealmLog.warn("Calling removeChangeListener on a closed Realm %s, " +
                    "make sure to close all listeners before closing the Realm.", configuration.getPath());
        }
        sharedRealm.realmNotifier.removeChangeListeners(this);
    }

    /**
     * Writes a compacted copy of the Realm to the given destination File.
     * <p>
     * The destination file cannot already exist.
     * <p>
     * Note that if this is called from within a transaction it writes the current data, and not the data as it was when
     * the last transaction was committed.
     *
     * @param destination file to save the Realm to.
     * @throws RealmFileException if an error happened when accessing the underlying Realm file or writing to the
     * destination file.
     */
    public void writeCopyTo(File destination) {
        //noinspection ConstantConditions
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        sharedRealm.writeCopy(destination, null);
    }

    /**
     * Writes a compacted and encrypted copy of the Realm to the given destination File.
     * <p>
     * The destination file cannot already exist.
     * <p>
     * Note that if this is called from within a transaction it writes the current data, and not the data as it was when
     * the last transaction was committed.
     * <p>
     *
     * @param destination file to save the Realm to.
     * @param key a 64-byte encryption key.
     * @throws IllegalArgumentException if destination argument is null.
     * @throws RealmFileException if an error happened when accessing the underlying Realm file or writing to the
     * destination file.
     */
    public void writeEncryptedCopyTo(File destination, byte[] key) {
        //noinspection ConstantConditions
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        sharedRealm.writeCopy(destination, key);
    }

    /**
     * Blocks the current thread until new changes to the Realm are available or {@link #stopWaitForChange()}
     * is called from another thread. Once stopWaitForChange is called, all future calls to this method will
     * return false immediately.
     *
     * @return {@code true} if the Realm was updated to the latest version, {@code false} if it was
     * cancelled by calling stopWaitForChange.
     * @throws IllegalStateException if calling this from within a transaction or from a Looper thread.
     * @throws RealmMigrationNeededException on typed {@link Realm} if the latest version contains
     * incompatible schema changes.
     * @deprecated this method will be removed on the next-major release.
     */
    @Deprecated
    public boolean waitForChange() {
        checkIfValid();
        if (isInTransaction()) {
            throw new IllegalStateException("Cannot wait for changes inside of a transaction.");
        }
        if (Looper.myLooper() != null) {
            throw new IllegalStateException("Cannot wait for changes inside a Looper thread. Use RealmChangeListeners instead.");
        }
        boolean hasChanged = sharedRealm.waitForChange();
        if (hasChanged) {
            // Since this Realm instance has been waiting for change, advance realm & refresh realm.
            sharedRealm.refresh();
        }
        return hasChanged;
    }

    /**
     * Makes any current {@link #waitForChange()} return {@code false} immediately. Once this is called,
     * all future calls to waitForChange will immediately return {@code false}.
     * <p>
     * This method is thread-safe and should _only_ be called from another thread than the one that
     * called waitForChange.
     *
     * @throws IllegalStateException if the {@link io.realm.Realm} instance has already been closed.
     * @deprecated this method will be removed in the next-major release
     */
    @Deprecated
    public void stopWaitForChange() {
        if (realmCache != null) {
            realmCache.invokeWithLock(new RealmCache.Callback0() {
                @Override
                public void onCall() {
                    // Checks if the Realm instance has been closed.
                    if (sharedRealm == null || sharedRealm.isClosed()) {
                        throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
                    }
                    sharedRealm.stopWaitForChange();
                }
            });
        } else {
            throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
        }
    }

    /**
     * Starts a transaction which must be closed by {@link io.realm.Realm#commitTransaction()} or aborted by
     * {@link io.realm.Realm#cancelTransaction()}. Transactions are used to atomically create, update and delete objects
     * within a Realm.
     * <p>
     * Before beginning a transaction, the Realm instance is updated to the latest version in order to include all
     * changes from other threads. This update does not trigger any registered {@link RealmChangeListener}.
     * <p>
     * It is therefore recommended to query for the items that should be modified from inside the transaction. Otherwise
     * there is a risk that some of the results have been deleted or modified when the transaction begins.
     * <p>
     * <pre>
     * {@code
     * // Don't do this
     * RealmResults<Person> persons = realm.where(Person.class).findAll();
     * realm.beginTransaction();
     * persons.first().setName("John");
     * realm.commitTransaction();
     *
     * // Do this instead
     * realm.beginTransaction();
     * RealmResults<Person> persons = realm.where(Person.class).findAll();
     * persons.first().setName("John");
     * realm.commitTransaction();
     * }
     * </pre>
     * <p>
     * Notice: it is not possible to nest transactions. If you start a transaction within a transaction an exception is
     * thrown.
     *
     * @throws RealmMigrationNeededException on typed {@link Realm} if the latest version contains
     * incompatible schema changes.
     */
    public void beginTransaction() {
        checkIfValid();
        sharedRealm.beginTransaction();
    }

    /**
     * All changes since {@link io.realm.Realm#beginTransaction()} are persisted to disk and the Realm reverts back to
     * being read-only. An event is sent to notify all other Realm instances that a change has occurred. When the event
     * is received, the other Realms will update their objects and {@link io.realm.RealmResults} to reflect the
     * changes from this commit.
     */
    public void commitTransaction() {
        checkIfValid();
        sharedRealm.commitTransaction();
    }

    /**
     * Reverts all writes (created, updated, or deleted objects) made in the current write transaction and end the
     * transaction.
     * <p>
     * The Realm reverts back to read-only.
     * <p>
     * Calling this when not in a transaction will throw an exception.
     */
    public void cancelTransaction() {
        checkIfValid();
        sharedRealm.cancelTransaction();
    }

    /**
     * Returns a frozen snapshot of the current Realm. This Realm can be read and queried from any thread without throwing
     * an {@link IllegalStateException}. A frozen Realm has its own lifecycle and can be closed by calling {@link #close()},
     * but fully closing the Realm that spawned the frozen copy will also close the frozen Realm.
     * <p>
     * Frozen data can be queried as normal, but trying to mutate it in any way or attempting to register any listener will
     * throw an {@link IllegalStateException}.
     * <p>
     * Note: Keeping a large number of Realms with different versions alive can have a negative impact on the filesize
     * of the Realm. In order to avoid such a situation, it is possible to set {@link RealmConfiguration.Builder#maxNumberOfActiveVersions(long)}.
     *
     * @return a frozen copy of this Realm.
     * @throws IllegalStateException if this method is called from inside a write transaction.
     */
    public abstract BaseRealm freeze();

    /**
     * Returns whether or not this Realm is frozen.
     *
     * @return {@code true} if the Realm is frozen, {@code false} if it is not.
     * @see #freeze()
     */
    public boolean isFrozen() {
        // This method needs to be threadsafe even for live Realms, so don't call {@link #checkIfValid}
        if (sharedRealm == null || sharedRealm.isClosed()) {
            throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
        }
        return frozen;
    }

    /**
     * Checks if a Realm's underlying resources are still available or not getting accessed from the wrong thread.
     */
    protected void checkIfValid() {
        if (sharedRealm == null || sharedRealm.isClosed()) {
            throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
        }

        // Checks if we are in the right thread.
        if (!frozen && threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(BaseRealm.INCORRECT_THREAD_MESSAGE);
        }
    }

    /**
     * Checks whether queries are allowed from the UI thread in the current RealmConfiguration.
     */
    protected void checkAllowQueriesOnUiThread() {
        // Warn on query being executed on UI thread if isAllowQueriesOnUiThread is set to true, throw otherwise
        if (getSharedRealm().capabilities.isMainThread()) {
            if (!getConfiguration().isAllowQueriesOnUiThread()) {
                throw new RealmException("Queries on the UI thread have been disabled. They can be enabled by setting 'RealmConfiguration.Builder.allowQueriesOnUiThread(true)'.");
            }
        }
    }

    /**
     * Checks whether writes are allowed from the UI thread in the current RealmConfiguration.
     */
    protected void checkAllowWritesOnUiThread() {
        // Warn on transaction being executed on UI thread if allowWritesOnUiThread is set to true, throw otherwise
        if (getSharedRealm().capabilities.isMainThread()) {
            if (!getConfiguration().isAllowWritesOnUiThread()) {
                throw new RealmException("Running transactions on the UI thread has been disabled. It can be enabled by setting 'RealmConfiguration.Builder.allowWritesOnUiThread(true)'.");
            }
        }
    }

    protected void checkIfInTransaction() {
        if (!sharedRealm.isInTransaction()) {
            throw new IllegalStateException("Changing Realm data can only be done from inside a transaction.");
        }
    }

    /**
     * Checks if the Realm is valid and in a transaction.
     */
    protected void checkIfValidAndInTransaction() {
        if (!isInTransaction()) {
            throw new IllegalStateException(NOT_IN_TRANSACTION_MESSAGE);
        }
    }

    /**
     * Creates a row representing an embedded object - for internal use only.
     *
     * @param className the class name of the object to create.
     * @param parentProxy The parent object which should hold a reference to the embedded object.
     * @param parentProperty the property in the parent class which holds the reference.
     * @param schema the Realm schema from which to obtain table information.
     * @param parentObjectSchema the parent object schema from which to obtain property information.
     * @return the row representing the newly created embedded object.
     * @throws IllegalArgumentException if any embedded object invariants are broken.
     */
    Row getEmbeddedObjectRow(final String className,
                             final RealmObjectProxy parentProxy,
                             final String parentProperty,
                             final RealmSchema schema,
                             final RealmObjectSchema parentObjectSchema) {
        final long parentPropertyColKey = parentObjectSchema.getColumnKey(parentProperty);
        final RealmFieldType parentPropertyType = parentObjectSchema.getFieldType(parentProperty);
        final Row row = parentProxy.realmGet$proxyState().getRow$realm();
        final RealmFieldType fieldType = parentObjectSchema.getFieldType(parentProperty);
        boolean propertyAcceptable = parentObjectSchema.isPropertyAcceptableForEmbeddedObject(fieldType);
        if (!propertyAcceptable) {
            throw new IllegalArgumentException(String.format("Field '%s' does not contain a valid link", parentProperty));
        }
        final String linkedType = parentObjectSchema.getPropertyClassName(parentProperty);

        // By now linkedType can only be either OBJECT or LIST, so no exhaustive check needed
        Row embeddedObject;
        if (linkedType.equals(className)) {
            long objKey = row.createEmbeddedObject(parentPropertyColKey, parentPropertyType);
            embeddedObject = schema.getTable(className).getCheckedRow(objKey);
        } else {
            throw new IllegalArgumentException(String.format("Parent type %s expects that property '%s' be of type %s but was %s.", parentObjectSchema.getClassName(), parentProperty, linkedType, className));
        }

        return embeddedObject;
    }

    /**
     * Checks if the Realm is not built with a SyncRealmConfiguration.
     */
    void checkNotInSync() {
        if (configuration.isSyncConfiguration()) {
            throw new UnsupportedOperationException("You cannot perform destructive changes to a schema of a synced Realm");
        }
    }

    /**
     * Returns the canonical path to where this Realm is persisted on disk.
     *
     * @return the canonical path to the Realm file.
     * @see File#getCanonicalPath()
     */
    public String getPath() {
        return configuration.getPath();
    }

    /**
     * Returns the {@link RealmConfiguration} for this Realm.
     *
     * @return the {@link RealmConfiguration} for this Realm.
     */
    public RealmConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the schema version for this Realm.
     *
     * @return the schema version for the Realm file backing this Realm.
     */
    public long getVersion() {
        return OsObjectStore.getSchemaVersion(sharedRealm);
    }

    /**
     * Closes the Realm instance and all its resources.
     * <p>
     * It's important to always remember to close Realm instances when you're done with it in order not to leak memory,
     * file descriptors or grow the size of Realm file out of measure.
     *
     * @throws IllegalStateException if attempting to close from another thread.
     */
    @Override
    public void close() {
        if (!frozen && this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_CLOSE_MESSAGE);
        }

        if (realmCache != null) {
            realmCache.release(this);
        } else {
            doClose();
        }
    }

    /**
     * Closes the Realm instances and all its resources without checking the {@link RealmCache}.
     */
    void doClose() {
        realmCache = null;
        if (sharedRealm != null && shouldCloseSharedRealm) {
            sharedRealm.close();
            sharedRealm = null;
        }
    }

    /**
     * Checks if the {@link io.realm.Realm} instance has already been closed.
     *
     * @return {@code true} if closed, {@code false} otherwise.
     * @throws IllegalStateException if attempting to close from another thread.
     */
    public boolean isClosed() {
        if (!frozen && this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }

        return sharedRealm == null || sharedRealm.isClosed();
    }

    /**
     * Checks if this {@link io.realm.Realm} contains any objects.
     *
     * @return {@code true} if empty, @{code false} otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Returns the schema for this Realm.
     *
     * @return The {@link RealmSchema} for this Realm.
     */
    public abstract RealmSchema getSchema();

    // Used by RealmList/RealmResults, to create RealmObject from a OsResults.
    // Invariant: if dynamicClassName != null -> clazz == DynamicRealmObject
    <E extends RealmModel> E get(@Nullable Class<E> clazz, @Nullable String dynamicClassName, UncheckedRow row) {
        final boolean isDynamicRealmObject = dynamicClassName != null;

        E result;
        if (isDynamicRealmObject) {
            //noinspection unchecked
            result = (E) new DynamicRealmObject(this, CheckedRow.getFromRow(row));
        } else {
            // 'clazz' is non-null when 'dynamicClassName' is null.
            //noinspection ConstantConditions
            result = configuration.getSchemaMediator().newInstance(clazz, this, row, getSchema().getColumnInfo(clazz),
                    false, Collections.<String>emptyList());
        }
        return result;
    }

    <E extends RealmModel> E get(Class<E> clazz, long rowKey, boolean acceptDefaultValue, List<String> excludeFields) {
        Table table = getSchema().getTable(clazz);
        UncheckedRow row = table.getUncheckedRow(rowKey);
        return configuration.getSchemaMediator().newInstance(clazz, this, row, getSchema().getColumnInfo(clazz),
                acceptDefaultValue, excludeFields);
    }

    // Used by RealmList/RealmResults
    // Invariant: if dynamicClassName != null -> clazz == DynamicRealmObject
    // TODO: Remove this after RealmList is backed by OS Results.
    <E extends RealmModel> E get(@Nullable Class<E> clazz, @Nullable String dynamicClassName, long rowIndex) {
        final boolean isDynamicRealmObject = dynamicClassName != null;
        // 'clazz' is non-null when 'dynamicClassName' is null.
        //noinspection ConstantConditions
        final Table table = isDynamicRealmObject ? getSchema().getTable(dynamicClassName) : getSchema().getTable(clazz);

        E result;
        if (isDynamicRealmObject) {
            @SuppressWarnings("unchecked")
            E dynamicObj = (E) new DynamicRealmObject(this,
                    (rowIndex != Table.NO_MATCH) ? table.getCheckedRow(rowIndex) : InvalidRow.INSTANCE);
            result = dynamicObj;
        } else {
            result = configuration.getSchemaMediator().newInstance(clazz, this,
                    (rowIndex != Table.NO_MATCH) ? table.getUncheckedRow(rowIndex) : InvalidRow.INSTANCE,
                    getSchema().getColumnInfo(clazz), false, Collections.<String>emptyList());
        }

        return result;
    }

    /**
     * Deletes all objects from this Realm.
     *
     * @throws IllegalStateException if the Realm is closed or called from an incorrect thread.
     */
    public void deleteAll() {
        checkIfValid();
        for (RealmObjectSchema objectSchema : getSchema().getAll()) {
            getSchema().getTable(objectSchema.getClassName()).clear();
        }
    }

    /**
     * Deletes the Realm file defined by the given configuration.
     */
    static boolean deleteRealm(final RealmConfiguration configuration) {
        final AtomicBoolean realmDeleted = new AtomicBoolean(true);
        boolean callbackExecuted = OsObjectStore.callWithLock(configuration, new Runnable() {
            @Override
            public void run() {
                String canonicalPath = configuration.getPath();
                File realmFolder = configuration.getRealmDirectory();
                String realmFileName = configuration.getRealmFileName();
                realmDeleted.set(Util.deleteRealm(canonicalPath, realmFolder, realmFileName));
            }
        });
        if (!callbackExecuted) {
            throw new IllegalStateException("It's not allowed to delete the file associated with an open Realm. " +
                    "Remember to close() all the instances of the Realm before deleting its file: "
                    + configuration.getPath());
        }
        return realmDeleted.get();
    }

    /**
     * Compacts the Realm file defined by the given configuration.
     *
     * @param configuration configuration for the Realm to compact.
     * @return {@code true} if compaction succeeded, {@code false} otherwise.
     */
    static boolean compactRealm(final RealmConfiguration configuration) {
        OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configuration, OsSharedRealm.VersionID.LIVE);
        Boolean result = sharedRealm.compact();
        sharedRealm.close();
        return result;
    }

    /**
     * Migrates the Realm file defined by the given configuration using the provided migration block.
     *
     * @param configuration configuration for the Realm that should be migrated. If this is a SyncConfiguration this
     * method does nothing.
     * @param migration if set, this migration block will override what is set in {@link RealmConfiguration}.
     * @throws FileNotFoundException if the Realm file doesn't exist.
     * @throws IllegalArgumentException if the provided configuration is a {@code SyncConfiguration}.
     */
    protected static void migrateRealm(final RealmConfiguration configuration, @Nullable final RealmMigration migration)
            throws FileNotFoundException {

        //noinspection ConstantConditions
        if (configuration == null) {
            throw new IllegalArgumentException("RealmConfiguration must be provided");
        }
        if (configuration.isSyncConfiguration()) {
            throw new IllegalArgumentException("Manual migrations are not supported for synced Realms");
        }
        if (migration == null && configuration.getMigration() == null) {
            throw new RealmMigrationNeededException(configuration.getPath(), "RealmMigration must be provided.");
        }

        final AtomicBoolean fileNotFound = new AtomicBoolean(false);

        RealmCache.invokeWithGlobalRefCount(configuration, new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                if (count != 0) {
                    throw new IllegalStateException("Cannot migrate a Realm file that is already open: "
                            + configuration.getPath());
                }

                File realmFile = new File(configuration.getPath());
                if (!realmFile.exists()) {
                    fileNotFound.set(true);
                    return;
                }

                RealmProxyMediator mediator = configuration.getSchemaMediator();
                OsSchemaInfo schemaInfo = new OsSchemaInfo(mediator.getExpectedObjectSchemaInfoMap().values());
                OsSharedRealm.MigrationCallback migrationCallback = null;
                final RealmMigration migrationToBeApplied = migration != null ? migration : configuration.getMigration();
                if (migrationToBeApplied != null) {
                    migrationCallback = createMigrationCallback(migrationToBeApplied);
                }
                OsRealmConfig.Builder configBuilder = new OsRealmConfig.Builder(configuration)
                        .autoUpdateNotification(false)
                        .schemaInfo(schemaInfo)
                        .migrationCallback(migrationCallback);
                OsSharedRealm sharedRealm = null;
                try {
                    sharedRealm =
                            OsSharedRealm.getInstance(configBuilder, OsSharedRealm.VersionID.LIVE);
                } finally {
                    if (sharedRealm != null) {
                        sharedRealm.close();
                    }
                }
            }
        });

        if (fileNotFound.get()) {
            throw new FileNotFoundException("Cannot migrate a Realm file which doesn't exist: "
                    + configuration.getPath());
        }
    }

    private static OsSharedRealm.MigrationCallback createMigrationCallback(final RealmMigration migration) {
        return new OsSharedRealm.MigrationCallback() {
            @Override
            public void onMigrationNeeded(OsSharedRealm sharedRealm, long oldVersion, long newVersion) {
                migration.migrate(DynamicRealm.createInstance(sharedRealm), oldVersion, newVersion);
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        if (shouldCloseSharedRealm && sharedRealm != null && !sharedRealm.isClosed()) {
            RealmLog.warn("Remember to call close() on all Realm instances. " +
                    "Realm %s is being finalized without being closed, " +
                    "this can lead to running out of native memory.", configuration.getPath()
            );
            if (realmCache != null) {
                realmCache.leak();
            }
        }
        super.finalize();
    }

    OsSharedRealm getSharedRealm() {
        return sharedRealm;
    }

    public static final class RealmObjectContext {
        private BaseRealm realm;
        private Row row;
        private ColumnInfo columnInfo;
        private boolean acceptDefaultValue;
        private List<String> excludeFields;

        public void set(BaseRealm realm, Row row, ColumnInfo columnInfo,
                boolean acceptDefaultValue, List<String> excludeFields) {
            this.realm = realm;
            this.row = row;
            this.columnInfo = columnInfo;
            this.acceptDefaultValue = acceptDefaultValue;
            this.excludeFields = excludeFields;
        }

        BaseRealm getRealm() {
            return realm;
        }

        public Row getRow() {
            return row;
        }

        public ColumnInfo getColumnInfo() {
            return columnInfo;
        }

        public boolean getAcceptDefaultValue() {
            return acceptDefaultValue;
        }

        public List<String> getExcludeFields() {
            return excludeFields;
        }

        public void clear() {
            realm = null;
            row = null;
            columnInfo = null;
            acceptDefaultValue = false;
            excludeFields = null;
        }
    }

    /**
     * CM: This is used when creating new proxy classes directly from the generated proxy code.
     * It is a bit unclear exactly how it works, but it seems to be some work-around for some
     * constructor shenanigans, i.e. values are set in this object just before the Proxy object
     * is created (see `RealmDefaultModuleMediator.newInstance)`).
     */
    static final class ThreadLocalRealmObjectContext extends ThreadLocal<RealmObjectContext> {
        @Override
        protected RealmObjectContext initialValue() {
            return new RealmObjectContext();
        }
    }

    public static final ThreadLocalRealmObjectContext objectContext = new ThreadLocalRealmObjectContext();

    /**
     * The Callback used when reporting back the result of loading a Realm asynchronously using either
     * {@link Realm#getInstanceAsync(RealmConfiguration, Realm.Callback)} or
     * {@link DynamicRealm#getInstanceAsync(RealmConfiguration, DynamicRealm.Callback)}.
     * <p>
     * Before creating the first Realm instance in a process, there are some initialization work that need to be done
     * such as creating or validating schemas, running migration if needed,
     * copy asset file if {@link RealmConfiguration.Builder#assetFile(String)} is supplied and execute the
     * {@link RealmConfiguration.Builder#initialData(Realm.Transaction)} if necessary. This work may take time
     * and block the caller thread for a while. To avoid the {@code getInstance()} call blocking the main thread, the
     * {@code getInstanceAsync()} can be used instead to do the initialization work in the background thread and
     * deliver a Realm instance to the caller thread.
     * <p>
     * In general, this method is mostly useful on the UI thread since that should be blocked as little as possible. On
     * any other Looper threads or other threads that don't support callbacks, using the standard {@code getInstance()}
     * should be fine.
     * <p>
     * Here is an example of using {@code getInstanceAsync()} when the app starts the first activity:
     * <pre>
     * public class MainActivity extends Activity {
     *
     *   private Realm realm = null;
     *   private RealmAsyncTask realmAsyncTask;
     *   private static RealmConfiguration config = new RealmConfiguration.Builder()
     *     .schema(42)
     *     .migration(new MyMigration()) // Potentially lengthy migration
     *     .build();
     *
     *   \@Override
     *   protected void onCreate(Bundle savedInstanceState) {
     *     super.onCreate(savedInstanceState);
     *     setContentView(R.layout.layout_main);
     *     realmAsyncTask = Realm.getInstanceAsync(config, new Callback() {
     *         \@Override
     *         public void onSuccess(Realm realm) {
     *             if (isDestroyed()) {
     *                 // If the activity is destroyed, the Realm instance should be closed immediately to avoid leaks.
     *                 // Or you can call realmAsyncTask.cancel() in onDestroy() to stop callback delivery.
     *                 realm.close();
     *             } else {
     *                 MainActivity.this.realm = realm;
     *                 // Remove the spinner and start the real UI.
     *             }
     *         }
     *     });
     *
     *     // Show a spinner before Realm instance returned by the callback.
     *   }
     *
     *   \@Override
     *   protected void onDestroy() {
     *     super.onDestroy();
     *     if (realm != null) {
     *         realm.close();
     *         realm = null;
     *     } else {
     *         // Calling cancel() on the thread where getInstanceAsync was called on to stop the callback delivery.
     *         // Otherwise you need to check if the activity is destroyed to close in the onSuccess() properly.
     *         realmAsyncTask.cancel();
     *     }
     *   }
     * }
     * </pre>
     *
     * @param <T> {@link Realm} or {@link DynamicRealm}.
     */
    public abstract static class InstanceCallback<T extends BaseRealm> {

        /**
         * Deliver a Realm instance to the caller thread.
         *
         * @param realm the Realm instance for the caller thread.
         */
        public abstract void onSuccess(T realm);

        /**
         * Deliver an error happens when creating the Realm instance to the caller thread. The default implementation
         * will throw an exception on the caller thread.
         *
         * @param exception happened while initializing Realm on a background thread.
         */
        public void onError(Throwable exception) {
            throw new RealmException("Exception happens when initializing Realm in the background thread.", exception);
        }
    }
}
