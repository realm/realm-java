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
import io.realm.annotations.Beta;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.CheckedRow;
import io.realm.internal.ColumnInfo;
import io.realm.internal.InvalidRow;
import io.realm.internal.ObjectServerFacade;
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
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.log.RealmLog;
import io.realm.sync.permissions.ObjectPrivileges;
import io.realm.sync.permissions.RealmPrivileges;

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
    BaseRealm(RealmCache cache, @Nullable OsSchemaInfo schemaInfo) {
        this(cache.getConfiguration(), schemaInfo);
        this.realmCache = cache;
    }

    // Create a realm instance without associating it to any RealmCache.
    BaseRealm(final RealmConfiguration configuration, @Nullable OsSchemaInfo schemaInfo) {
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
                    initialDataTransaction.execute(Realm.createInstance(sharedRealm));
                }
            };
        }

        OsRealmConfig.Builder configBuilder = new OsRealmConfig.Builder(configuration)
                .autoUpdateNotification(true)
                .migrationCallback(migrationCallback)
                .schemaInfo(schemaInfo)
                .initializationCallback(initializationCallback);
        this.sharedRealm = OsSharedRealm.getInstance(configBuilder);
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
     * It also calls any listeners associated with the Realm if neeeded.
     * <p>
     * WARNING: Calling this on a thread with async queries will turn those queries into synchronous queries.
     * In most cases it is better to use {@link RealmChangeListener}s to be notified about changes to the
     * Realm on a given thread than it is to use this method.
     *
     * @throws IllegalStateException if attempting to refresh from within a transaction.
     */
    public void refresh() {
        checkIfValid();
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
     * If you would like the {@code asFlowable()} to stop emitting items, you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * realm.asFlowable().first().subscribe( ... ) // You only get the results once
     * }
     * </pre>
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
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
     */
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
     */
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
     * Checks if a Realm's underlying resources are still available or not getting accessed from the wrong thread.
     */
    protected void checkIfValid() {
        if (sharedRealm == null || sharedRealm.isClosed()) {
            throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
        }

        // Checks if we are in the right thread.
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(BaseRealm.INCORRECT_THREAD_MESSAGE);
        }
    }

    protected void checkIfInTransaction() {
        if (!sharedRealm.isInTransaction()) {
            throw new IllegalStateException("Changing Realm data can only be done from inside a transaction.");
        }
    }

    protected void checkIfPartialRealm() {
        boolean isPartialRealm = false;
        if (configuration.isSyncConfiguration()) {
            isPartialRealm = ObjectServerFacade.getSyncFacadeIfPossible().isPartialRealm(configuration);
        }

        if (!isPartialRealm) {
            throw new IllegalStateException("This method is only available on partially synchronized Realms.");
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
     * Checks if the Realm is not built with a SyncRealmConfiguration.
     */
    void checkNotInSync() {
        if (configuration.isSyncConfiguration()) {
            throw new IllegalArgumentException("You cannot perform changes to a schema. " +
                    "Please update app and restart.");
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
     * Returns the privileges granted to the current user for this Realm.
     *
     * @return the privileges granted the current user for this Realm.
     */
    @Beta
    @ObjectServer
    public RealmPrivileges getPrivileges() {
        checkIfValid();
        return new RealmPrivileges(sharedRealm.getPrivileges());
    }

    /**
     * Returns the privileges granted to the current user for the given object.
     *
     * @param object Realm object to get privileges for.
     * @return the privileges granted the current user for the object.
     * @throws IllegalArgumentException if the object is either null, unmanaged or not part of this Realm.
     */
    public ObjectPrivileges getPrivileges(RealmModel object) {
        checkIfValid();
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("Non-null 'object' required.");
        }
        if (!RealmObject.isManaged(object)) {
            throw new IllegalArgumentException("Only managed objects have privileges. This is a an unmanaged object: " + object.toString());
        }
        if (!((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(getPath())) {
            throw new IllegalArgumentException("Object belongs to a different Realm.");
        }
        UncheckedRow row = (UncheckedRow) ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm();
        return new ObjectPrivileges(sharedRealm.getObjectPrivileges(row));
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
        if (this.threadId != Thread.currentThread().getId()) {
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
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }

        return sharedRealm == null || sharedRealm.isClosed();
    }

    /**
     * Checks if this {@link io.realm.Realm} contains any objects.
     *
     * @return {@code true} if empty, @{code false} otherwise.
     */
    abstract public boolean isEmpty();

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

    <E extends RealmModel> E get(Class<E> clazz, long rowIndex, boolean acceptDefaultValue, List<String> excludeFields) {
        Table table = getSchema().getTable(clazz);
        UncheckedRow row = table.getUncheckedRow(rowIndex);
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
     * <p>
     * If the Realm is a partially synchronized Realm, all subscriptions will be cleared as well.
     *
     * @throws IllegalStateException if the corresponding Realm is a partially synchronized Realm, is
     * closed or called from an incorrect thread.
     */
    public void deleteAll() {
        checkIfValid();
        if (sharedRealm.isPartial()) {
            throw new IllegalStateException(DELETE_NOT_SUPPORTED_UNDER_PARTIAL_SYNC);
        }
        boolean isPartialRealm = sharedRealm.isPartial();
        for (RealmObjectSchema objectSchema : getSchema().getAll()) {
            getSchema().getTable(objectSchema.getClassName()).clear(isPartialRealm);
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
        OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configuration);
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
                            OsSharedRealm.getInstance(configBuilder);
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

    // FIXME: This stuff doesn't appear to be used.  It should either be explained or deleted.
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
