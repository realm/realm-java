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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.getkeepsafe.relinker.BuildConfig;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.annotations.internal.OptionalAPI;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SharedGroupManager;
import io.realm.internal.Table;
import io.realm.internal.TableView;
import io.realm.internal.UncheckedRow;
import io.realm.internal.android.DebugAndroidLogger;
import io.realm.internal.android.ReleaseAndroidLogger;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.log.RealmLog;
import rx.Observable;

/**
 * Base class for all Realm instances.
 *
 * @see io.realm.Realm
 * @see io.realm.DynamicRealm
 */
abstract class BaseRealm implements Closeable {
    protected static final long UNVERSIONED = -1;
    private static final String INCORRECT_THREAD_CLOSE_MESSAGE = "Realm access from incorrect thread. Realm instance can only be closed on the thread it was created.";
    private static final String INCORRECT_THREAD_MESSAGE = "Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.";
    private static final String CLOSED_REALM_MESSAGE = "This Realm instance has already been closed, making it unusable.";
    private static final String CANNOT_REFRESH_INSIDE_OF_TRANSACTION_MESSAGE = "Cannot refresh inside of a transaction.";

    // Map between a Handler and the canonical path to a Realm file
    protected static final Map<Handler, String> handlers = new ConcurrentHashMap<Handler, String>();

    // Thread pool for all async operations (Query & transaction)
    static final RealmThreadPoolExecutor asyncTaskExecutor = RealmThreadPoolExecutor.newDefaultExecutor();

    final long threadId;
    protected RealmConfiguration configuration;
    protected SharedGroupManager sharedGroupManager;
    RealmSchema schema;
    Handler handler;
    HandlerController handlerController;

    static {
        RealmLog.add(BuildConfig.DEBUG ? new DebugAndroidLogger() : new ReleaseAndroidLogger());
    }

    protected BaseRealm(RealmConfiguration configuration, boolean autoRefresh) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = configuration;
        this.sharedGroupManager = new SharedGroupManager(configuration);
        this.schema = new RealmSchema(this, sharedGroupManager.getTransaction());
        this.handlerController = new HandlerController(this);
        if (Looper.myLooper() == null) {
            if (autoRefresh) {
                throw new IllegalStateException("Cannot set auto-refresh in a Thread without a Looper");
            }
        } else {
            setAutoRefresh(autoRefresh);
        }
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
        if (Looper.myLooper() == null) {
            throw new IllegalStateException("Cannot set auto-refresh in a Thread without a Looper");
        }

        if (autoRefresh && !handlerController.isAutoRefreshEnabled()) { // Switch it on
            handler = new Handler(handlerController);
            handlers.put(handler, configuration.getPath());
        } else if (!autoRefresh && handlerController.isAutoRefreshEnabled() && handler != null) { // Switch it off
            removeHandler();
        }
        handlerController.setAutoRefresh(autoRefresh);
    }

    /**
     * Retrieves the auto-refresh status of the Realm instance.
     *
     * @return the auto-refresh status.
     */
    public boolean isAutoRefresh() {
        return handlerController.isAutoRefreshEnabled();
    }

    /**
     * Checks if the Realm is currently in a transaction.
     *
     * @return {@code true} if inside a transaction, {@code false} otherwise.
     */
    public boolean isInTransaction() {
        checkIfValid();
        return !sharedGroupManager.isImmutable();
    }

    protected void addListener(RealmChangeListener<? extends BaseRealm> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        checkIfValid();
        if (!handlerController.isAutoRefreshEnabled()) {
            throw new IllegalStateException("You can't register a listener from a non-Looper thread ");
        }
        handlerController.addChangeListener(listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener<? extends BaseRealm> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        checkIfValid();
        if (!handlerController.isAutoRefreshEnabled()) {
            throw new IllegalStateException("You can't remove a listener from a non-Looper thread ");
        }
        handlerController.removeChangeListener(listener);
    }

    /**
     * Returns an RxJava Observable that monitors changes to this Realm. It will emit the current state
     * when subscribed to. Items will continually be emitted as the Realm is updated -
     * {@code onComplete} will never be called.
     * <p>
     * If you would like the {@code asObservable()} to stop emitting items, you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     *
     * <pre>
     * {@code
     * realm.asObservable().first().subscribe( ... ) // You only get the results once
     * }
     * </pre>
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    @OptionalAPI(dependencies = {"rx.Observable"})
    public abstract Observable asObservable();

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        checkIfValid();
        if (!handlerController.isAutoRefreshEnabled()) {
            throw new IllegalStateException("You can't remove listeners from a non-Looper thread ");
        }
        handlerController.removeAllChangeListeners();
    }

    // WARNING: If this method is used after calling any async method, the old handler will still be used.
    //          package private, for test purpose only
    void setHandler(Handler handler) {
        // remove the old one
        handlers.remove(this.handler);
        handlers.put(handler, configuration.getPath());
        this.handler = handler;
    }

    /**
     * Removes and stops the current thread handler as gracefully as possible.
     */
    protected void removeHandler() {
        handlers.remove(handler);
        // Warning: This only clears the Looper queue. Handler.Callback is not removed.
        handler.removeCallbacksAndMessages(null);
        this.handler = null;
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
     * @throws java.io.IOException if any write operation fails.
     */
    public void writeCopyTo(File destination) throws java.io.IOException {
        writeEncryptedCopyTo(destination, null);
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
     * @throws java.io.IOException if any write operation fails.
     * @throws IllegalArgumentException if destination argument is null.
     */
    public void writeEncryptedCopyTo(File destination, byte[] key) throws java.io.IOException {
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        sharedGroupManager.copyToFile(destination, key);
    }

    /**
     * Blocks the current thread until new changes to the Realm are available or {@link #stopWaitForChange()}
     * is called from another thread. Once stopWaitForChange is called, all future calls to this method will
     * return false immediately.
     *
     * @return {@code true} if the Realm was updated to the latest version, {@code false} if it was
     * cancelled by calling stopWaitForChange.
     * @throws IllegalStateException if calling this from within a transaction or from a Looper thread.
     */
    public boolean waitForChange() {
        checkIfValid();
        if (isInTransaction()) {
            throw new IllegalStateException("Cannot wait for changes inside of a transaction.");
        }
        if (Looper.myLooper() != null) {
            throw new IllegalStateException("Cannot wait for changes inside a Looper thread. Use RealmChangeListeners instead.");
        }
        boolean hasChanged = sharedGroupManager.getSharedGroup().waitForChange();
        if (hasChanged) {
            // Since this Realm instance has been waiting for change, advance realm & refresh realm.
            sharedGroupManager.advanceRead();
            handlerController.refreshSynchronousTableViews();
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
        RealmCache.invokeWithLock(new RealmCache.Callback0() {
            @Override
            public void onCall() {
                // Check if the Realm instance has been closed
                if (sharedGroupManager == null || !sharedGroupManager.isOpen() || sharedGroupManager.getSharedGroup().isClosed()) {
                    throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
                }
                sharedGroupManager.getSharedGroup().stopWaitForChange();
            }
        });
    }

    /**
     * Starts a transaction which must be closed by {@link io.realm.Realm#commitTransaction()} or aborted by
     * {@link io.realm.Realm#cancelTransaction()}. Transactions are used to atomically create, update and delete objects
     * within a Realm.
     * <p>
     * Before beginning the transaction, {@link io.realm.Realm#beginTransaction()} updates the Realm in the case of
     * pending updates from other threads.
     * <p>
     * Notice: it is not possible to nest transactions. If you start a transaction within a transaction an exception is
     * thrown.
     */
    public void beginTransaction() {
        checkIfValid();
        sharedGroupManager.promoteToWrite();
    }

    /**
     * All changes since {@link io.realm.Realm#beginTransaction()} are persisted to disk and the Realm reverts back to
     * being read-only. An event is sent to notify all other Realm instances that a change has occurred. When the event
     * is received, the other Realms will update their objects and {@link io.realm.RealmResults} to reflect the
     * changes from this commit.
     */
    public void commitTransaction() {
        commitTransaction(true, null);
    }

    /**
     * Commits transaction, runs the given runnable and then sends notifications. The runnable is useful to meet some
     * timing conditions like the async transaction. In async transaction, the background Realm has to be closed before
     * other threads see the changes to majoyly avoid the flaky tests.
     *
     * @param notifyLocalThread set to {@code false} to prevent this commit from triggering thread local change listeners.
     * @param runAfterCommit runnable will run after transaction committed but before notification sent.
     */
    void commitTransaction(boolean notifyLocalThread, Runnable runAfterCommit) {
        checkIfValid();
        sharedGroupManager.commitAndContinueAsRead();

        if (runAfterCommit != null)  {
            runAfterCommit.run();
        }

        for (Map.Entry<Handler, String> handlerIntegerEntry : handlers.entrySet()) {
            Handler handler = handlerIntegerEntry.getKey();
            String realmPath = handlerIntegerEntry.getValue();

            // Sometimes we don't want to notify the local thread about commits, e.g. creating a completely new Realm
            // file will make a commit in order to create the schema. Users should not be notified about that.
            if (!notifyLocalThread && handler.equals(this.handler)) {
                continue;
            }

            // For all other threads, use the Handler
            // Note there is a race condition with handler.hasMessages() and handler.sendEmptyMessage()
            // as the target thread consumes messages at the same time. In this case it is not a problem as worst
            // case we end up with two REALM_CHANGED messages in the queue.
            Looper looper = handler.getLooper();
            if (realmPath.equals(configuration.getPath())          // It's the right realm
                            && looper.getThread().isAlive()) {     // The receiving thread is alive

                boolean messageHandled = true;
                if (looper == Looper.myLooper()) {
                    // Force any updates on the current thread to the front the queue. Doing this is mostly
                    // relevant on the UI thread where it could otherwise process a motion event before the
                    // REALM_CHANGED event. This could in turn cause a UI component like ListView to crash. See
                    // https://github.com/realm/realm-android-adapters/issues/11 for such a case.
                    // Other Looper threads could process similar events. For that reason all looper threads will
                    // prioritize local commits.
                    //
                    // If a user is doing commits inside a RealmChangeListener this can cause the Looper thread to get
                    // event starved as it only starts handling Realm events instead. This is an acceptable risk as
                    // that behaviour indicate a user bug. Previously this would be hidden as the UI would still
                    // be responsive.
                    Message msg = Message.obtain();
                    msg.what = HandlerController.LOCAL_COMMIT;
                    if (!handler.hasMessages(HandlerController.LOCAL_COMMIT)) {
                        messageHandled = handler.sendMessageAtFrontOfQueue(msg);
                    }
                } else {
                    if (!handler.hasMessages(HandlerController.REALM_CHANGED)) {
                        messageHandled = handler.sendEmptyMessage(HandlerController.REALM_CHANGED);
                    }
                }
                if (!messageHandled) {
                    RealmLog.w("Cannot update Looper threads when the Looper has quit. Use realm.setAutoRefresh(false) " +
                            "to prevent this.");
                }
            }
        }
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
        sharedGroupManager.rollbackAndContinueAsRead();
    }

    /**
     * Checks if a Realm's underlying resources are still available or not getting accessed from the wrong thread.
     */
    protected void checkIfValid() {
        // Check if the Realm instance has been closed
        if (sharedGroupManager == null || !sharedGroupManager.isOpen()) {
            throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
        }

        // Check if we are in the right thread
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(BaseRealm.INCORRECT_THREAD_MESSAGE);
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
        if (!sharedGroupManager.hasTable(Table.METADATA_TABLE_NAME)) {
            return UNVERSIONED;
        }
        Table metadataTable = sharedGroupManager.getTable(Table.METADATA_TABLE_NAME);
        return metadataTable.getLong(0, 0);
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

        RealmCache.release(this);
    }

    /**
     * Closes the Realm instances and all its resources without checking the {@link RealmCache}.
     */
    void doClose() {
        if (sharedGroupManager != null) {
            sharedGroupManager.close();
            sharedGroupManager = null;
        }
        if (handler != null) {
            removeHandler();
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

        return sharedGroupManager == null || !sharedGroupManager.isOpen();
    }

    /**
     * Checks if this {@link io.realm.Realm} contains any objects.
     *
     * @return {@code true} if empty, @{code false} otherwise.
     */
    public boolean isEmpty() {
        checkIfValid();
        return sharedGroupManager.getTransaction().isObjectTablesEmpty();
    }

    boolean hasChanged() {
        return sharedGroupManager.hasChanged();
    }

    // package protected so unit tests can access it
    void setVersion(long version) {
        Table metadataTable = sharedGroupManager.getTable(Table.METADATA_TABLE_NAME);
        if (metadataTable.getColumnCount() == 0) {
            metadataTable.addColumn(RealmFieldType.INTEGER, "version");
            metadataTable.addEmptyRow();
        }
        metadataTable.setLong(0, 0, version);
    }

    /**
     * Sort a table using the given field names and sorting directions. If a field name does not
     * exist in the table an {@link IllegalArgumentException} will be thrown.
     */
    protected TableView doMultiFieldSort(String[] fieldNames, Sort sortOrders[], Table table) {
        long columnIndices[] = new long[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            long columnIndex = table.getColumnIndex(fieldName);
            if (columnIndex == -1) {
                throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
            }
            columnIndices[i] = columnIndex;
        }

        return table.getSortedView(columnIndices, sortOrders);
    }

    protected void checkAllObjectsSortedParameters(String[] fieldNames, Sort[] sortOrders) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames must be provided.");
        } else if (sortOrders == null) {
            throw new IllegalArgumentException("sortOrders must be provided.");
        }
    }

    // Return all handlers registered for this Realm
    static Map<Handler, String> getHandlers() {
        return handlers;
    }

    /**
     * Returns the schema for this Realm.
     *
     * @return The {@link RealmSchema} for this Realm.
     */
    public RealmSchema getSchema() {
        return schema;
    }

    <E extends RealmModel> E get(Class<E> clazz, long rowIndex) {
        Table table = schema.getTable(clazz);
        UncheckedRow row = table.getUncheckedRow(rowIndex);
        E result = configuration.getSchemaMediator().newInstance(clazz, schema.getColumnInfo(clazz));
        RealmObjectProxy proxy = (RealmObjectProxy) result;
        proxy.realmGet$proxyState().setRow$realm(row);
        proxy.realmGet$proxyState().setRealm$realm(this);
        proxy.realmGet$proxyState().setTableVersion$realm();

        return result;
    }

    // Used by RealmList/RealmResults
    // Invariant: if dynamicClassName != null -> clazz == DynamicRealmObject
    <E extends RealmModel> E get(Class<E> clazz, String dynamicClassName, long rowIndex) {
        Table table;
        E result;
        if (dynamicClassName != null) {
            table = schema.getTable(dynamicClassName);
            @SuppressWarnings("unchecked")
            E dynamicObj = (E) new DynamicRealmObject();
            result = dynamicObj;
        } else {
            table = schema.getTable(clazz);
            result = configuration.getSchemaMediator().newInstance(clazz, schema.getColumnInfo(clazz));
        }

        RealmObjectProxy proxy = (RealmObjectProxy) result;
        proxy.realmGet$proxyState().setRealm$realm(this);
        if (rowIndex != Table.NO_MATCH) {
            proxy.realmGet$proxyState().setRow$realm(table.getUncheckedRow(rowIndex));
            proxy.realmGet$proxyState().setTableVersion$realm();
        } else {
            proxy.realmGet$proxyState().setRow$realm(InvalidRow.INSTANCE);
        }

        return result;
    }

    /**
     * Deletes all objects from this Realm.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or called from an incorrect thread.
     */
    public void deleteAll() {
        checkIfValid();
        for (RealmObjectSchema objectSchema : schema.getAll()) {
            schema.getTable(objectSchema.getClassName()).clear();
        }
    }

    static private boolean deletes(String canonicalPath, File rootFolder, String realmFileName) {
        final AtomicBoolean realmDeleted = new AtomicBoolean(true);

        List<File> filesToDelete = Arrays.asList(
                new File(rootFolder, realmFileName),
                new File(rootFolder, realmFileName + ".lock"),
                // Old core log file naming styles
                new File(rootFolder, realmFileName + ".log_a"),
                new File(rootFolder, realmFileName + ".log_b"),
                new File(rootFolder, realmFileName + ".log"),
                new File(canonicalPath));
        for (File fileToDelete : filesToDelete) {
            if (fileToDelete.exists()) {
                boolean deleteResult = fileToDelete.delete();
                if (!deleteResult) {
                    realmDeleted.set(false);
                    RealmLog.w("Could not delete the file " + fileToDelete);
                }
            }
        }
        return realmDeleted.get();
    }

    /**
     * Deletes the Realm file defined by the given configuration.
     */
    static boolean deleteRealm(final RealmConfiguration configuration) {
        final String management = ".management";
        final AtomicBoolean realmDeleted = new AtomicBoolean(true);

        RealmCache.invokeWithGlobalRefCount(configuration, new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                if (count != 0) {
                    throw new IllegalStateException("It's not allowed to delete the file associated with an open Realm. " +
                            "Remember to close() all the instances of the Realm before deleting its file: " + configuration.getPath());
                }

                String canonicalPath = configuration.getPath();
                File realmFolder = configuration.getRealmFolder();
                String realmFileName = configuration.getRealmFileName();
                File managementFolder = new File(realmFolder, realmFileName + management);

                // delete files in management folder and the folder
                // there is no subfolders in the management folder
                File[] files = managementFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        realmDeleted.set(realmDeleted.get() && file.delete());
                    }
                }
                realmDeleted.set(realmDeleted.get() && managementFolder.delete());

                // delete specific files in root folder
                realmDeleted.set(realmDeleted.get() && deletes(canonicalPath, realmFolder, realmFileName));
            }
        });

        return realmDeleted.get();
    }

    /**
     * Compacts the Realm file defined by the given configuration.
     *
     * @param configuration configuration for the Realm to compact.
     * @throw IllegalArgumentException if Realm is encrypted.
     * @return {@code true} if compaction succeeded, {@code false} otherwise.
     */
    static boolean compactRealm(final RealmConfiguration configuration) {
        if (configuration.getEncryptionKey() != null) {
            throw new IllegalArgumentException("Cannot currently compact an encrypted Realm.");
        }

        return SharedGroupManager.compact(configuration);
    }

    /**
     * Migrates the Realm file defined by the given configuration using the provided migration block.
     *
     * @param configuration configuration for the Realm that should be migrated.
     * @param migration if set, this migration block will override what is set in {@link RealmConfiguration}.
     * @param callback callback for specific Realm type behaviors.
     * @throws FileNotFoundException if the Realm file doesn't exist.
     */
    protected static void migrateRealm(final RealmConfiguration configuration, final RealmMigration migration,
                                       final MigrationCallback callback) throws FileNotFoundException {
        if (configuration == null) {
            throw new IllegalArgumentException("RealmConfiguration must be provided");
        }
        if (migration == null && configuration.getMigration() == null) {
            throw new RealmMigrationNeededException(configuration.getPath(), "RealmMigration must be provided");
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

                RealmMigration realmMigration = (migration == null) ? configuration.getMigration() : migration;
                DynamicRealm realm = null;
                try {
                    realm = DynamicRealm.getInstance(configuration);
                    realm.beginTransaction();
                    long currentVersion = realm.getVersion();
                    realmMigration.migrate(realm, currentVersion, configuration.getSchemaVersion());
                    realm.setVersion(configuration.getSchemaVersion());
                    realm.commitTransaction();
                } catch (RuntimeException e) {
                    if (realm != null) {
                        realm.cancelTransaction();
                    }
                    throw e;
                } finally {
                    if (realm != null) {
                        realm.close();
                        callback.migrationComplete();
                    }
                }
            }
        });

        if (fileNotFound.get()) {
            throw new FileNotFoundException("Cannot migrate a Realm file which doesn't exist: "
                    + configuration.getPath());
        }
    }

    // Internal delegate for migrations
    protected interface MigrationCallback {
        void migrationComplete();
    }

}
