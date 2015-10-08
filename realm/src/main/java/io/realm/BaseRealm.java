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
 *
 */

package io.realm;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.Closeable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.exceptions.RealmEncryptionNotSupportedException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedGroup;
import io.realm.internal.SharedGroupManager;
import io.realm.internal.Table;
import io.realm.internal.TableView;
import io.realm.internal.UncheckedRow;
import io.realm.internal.Util;
import io.realm.internal.android.DebugAndroidLogger;
import io.realm.internal.android.ReleaseAndroidLogger;
import io.realm.internal.log.RealmLog;

/**
 * Base class for all Realm instances.
 *
 * @see io.realm.Realm
 */
abstract class BaseRealm implements Closeable {

    private static final int REALM_CHANGED = 14930352; // Hopefully it won't clash with other message IDs.
    protected static final long UNVERSIONED = -1;
    private static final String INCORRECT_THREAD_CLOSE_MESSAGE = "Realm access from incorrect thread. Realm instance can only be closed on the thread it was created.";
    private static final String INCORRECT_THREAD_MESSAGE = "Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.";
    private static final String CLOSED_REALM_MESSAGE = "This Realm instance has already been closed, making it unusable.";
    private static final String DIFFERENT_KEY_MESSAGE = "Wrong key used to decrypt Realm.";

    // Map between all Realm file paths and all known configurations pointing to that file.
    protected static final Map<String, List<RealmConfiguration>> globalPathConfigurationCache =
            new HashMap<String, List<RealmConfiguration>>();

    // Reference count on currently open Realm instances (both normal and dynamic).
    protected static final Map<String, Integer> globalRealmFileReferenceCounter = new HashMap<String, Integer>();

    // Map between a Handler and the canonical path to a Realm file
    protected static final Map<Handler, String> handlers = new ConcurrentHashMap<Handler, String>();

    // Caches Dynamic Class objects given as Strings (both model classes and proxy classes) to Realm Tables
    private final Map<String, Table> dynamicClassToTable = new HashMap<String, Table>();

    // Caches Class objects (both model classes and proxy classes) to Realm Tables
    private final Map<Class<? extends RealmObject>, Table> classToTable = new HashMap<Class<? extends RealmObject>, Table>();

    protected final List<WeakReference<RealmChangeListener>> changeListeners =
            new CopyOnWriteArrayList<WeakReference<RealmChangeListener>>();

    protected long threadId;
    protected RealmConfiguration configuration;
    protected SharedGroupManager sharedGroupManager;
    protected boolean autoRefresh;
    Handler handler;
    ColumnIndices columnIndices = new ColumnIndices();

    static {
        RealmLog.add(BuildConfig.DEBUG ? new DebugAndroidLogger() : new ReleaseAndroidLogger());
    }

    protected BaseRealm(RealmConfiguration configuration, boolean autoRefresh) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = configuration;
        this.sharedGroupManager = new SharedGroupManager(configuration);
        setAutoRefresh(autoRefresh);
    }

    /**
     * Set the auto-refresh status of the Realm instance.
     * <p>
     * Auto-refresh is a feature that enables automatic update of the current Realm instance and all its derived objects
     * (RealmResults and RealmObjects instances) when a commit is performed on a Realm acting on the same file in another thread.
     * This feature is only available if the Realm instance lives is a {@link android.os.Looper} enabled thread.
     *
     * @param autoRefresh true will turn auto-refresh on, false will turn it off.
     * @throws java.lang.IllegalStateException if trying to enable auto-refresh in a thread without Looper.
     */
    public void setAutoRefresh(boolean autoRefresh) {
        if (autoRefresh && Looper.myLooper() == null) {
            throw new IllegalStateException("Cannot set auto-refresh in a Thread without a Looper");
        }

        if (autoRefresh && !this.autoRefresh) { // Switch it on
            handler = new Handler(new RealmCallback());
            handlers.put(handler, configuration.getPath());
        } else if (!autoRefresh && this.autoRefresh && handler != null) { // Switch it off
            removeHandler(handler);
        }
        this.autoRefresh = autoRefresh;
    }

    /**
     * Retrieve the auto-refresh status of the Realm instance.
     *
     * @return the auto-refresh status
     */
    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    /**
     * Add a change listener to the Realm.
     * <p>
     * The listeners will be executed:
     * <ul>
     * <li>Immediately if a change was committed by the local thread</li>
     * <li>On every loop of a Handler thread if changes were committed by another thread</li>
     * <li>On every call to {@link io.realm.Realm#refresh()}</li>
     * </ul>
     *
     * @param listener the change listener
     * @see io.realm.RealmChangeListener
     */
    public void addChangeListener(RealmChangeListener listener) {
        checkIfValid();
        for (WeakReference<RealmChangeListener> ref : changeListeners) {
            if (ref.get() == listener) {
                // It has already been added before
                return;
            }
        }

        changeListeners.add(new WeakReference<RealmChangeListener>(listener));
    }

    /**
     * Remove the specified change listener
     *
     * @param listener the change listener to be removed
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener listener) {
        checkIfValid();
        WeakReference<RealmChangeListener> weakRefToRemove = null;
        for (WeakReference<RealmChangeListener> weakRef : changeListeners) {
            if (listener == weakRef.get()) {
                weakRefToRemove = weakRef;
                // There won't be duplicated entries, checking is done when adding
                break;
            }
        }
        if (weakRefToRemove != null) {
            changeListeners.remove(weakRefToRemove);
        }
    }

    /**
     * Remove all user-defined change listeners
     *
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        checkIfValid();
        changeListeners.clear();
    }

    protected void removeHandler(Handler handler) {
        handler.removeCallbacksAndMessages(null);
        handlers.remove(handler);
        this.handler = null;
    }

    private void sendNotifications() {
        Iterator<WeakReference<RealmChangeListener>> iterator = changeListeners.iterator();
        List<WeakReference<RealmChangeListener>> toRemoveList = null;
        while (iterator.hasNext()) {
            WeakReference<RealmChangeListener> weakRef = iterator.next();
            RealmChangeListener listener = weakRef.get();
            if (listener == null) {
                if (toRemoveList == null) {
                    toRemoveList = new ArrayList<WeakReference<RealmChangeListener>>(changeListeners.size());
                }
                toRemoveList.add(weakRef);
            } else {
                listener.onChange();
            }
        }
        if (toRemoveList != null) {
            changeListeners.removeAll(toRemoveList);
        }
    }

    /**
     * Checks if any open Realm instances are still referencing this file.
     */
    protected static boolean isFileOpen(RealmConfiguration configuration) {
        Integer refCount = globalRealmFileReferenceCounter.get(configuration.getPath());
        return refCount != null && refCount > 0;
    }

    /**
     * Write a compacted copy of the Realm to the given destination File.
     * <p>
     * The destination file cannot already exist.
     * <p>
     * Note that if this is called from within a write transaction it writes the
     * current data, and not the data as it was when the last write transaction was committed.
     *
     * @param destination File to save the Realm to
     * @throws java.io.IOException if any write operation fails
     */
    public void writeCopyTo(File destination) throws java.io.IOException {
        writeEncryptedCopyTo(destination, null);
    }

    /**
     * Write a compacted and encrypted copy of the Realm to the given destination File.
     * <p>
     * The destination file cannot already exist.
     * <p>
     * Note that if this is called from within a write transaction it writes the
     * current data, and not the data as it was when the last write transaction was committed.
     * <p>
     *
     * @param destination File to save the Realm to
     * @param key a 64-byte encryption key
     * @throws java.io.IOException if any write operation fails
     * @throws RealmEncryptionNotSupportedException if the device doesn't support Realm encryption.
     */
    public void writeEncryptedCopyTo(File destination, byte[] key) throws java.io.IOException {
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        sharedGroupManager.copyToFile(destination, key);
    }

    /**
     * Refresh the Realm instance and all the RealmResults and RealmObjects instances coming from it.
     * It also calls the listeners associated to the Realm instance.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void refresh() {
        checkIfValid();
        sharedGroupManager.advanceRead();
        sendNotifications();
    }

    /**
     * Starts a write transaction, this must be closed with {@link io.realm.Realm#commitTransaction()}
     * or aborted by {@link io.realm.Realm#cancelTransaction()}. Write transactions are used to
     * atomically create, update and delete objects within a realm.
     * <br>
     * Before beginning the write transaction, {@link io.realm.Realm#beginTransaction()} updates the
     * realm in the case of pending updates from other threads.
     * <br>
     * Notice: it is not possible to nest write transactions. If you start a write
     * transaction within a write transaction an exception is thrown.
     * <br>
     *
     * @throws java.lang.IllegalStateException If already in a write transaction or incorrect thread.
     */
    public void beginTransaction() {
        checkIfValid();
        sharedGroupManager.promoteToWrite();
    }

    /**
     * All changes since {@link io.realm.Realm#beginTransaction()} are persisted to disk and the
     * Realm reverts back to being read-only. An event is sent to notify all other realm instances
     * that a change has occurred. When the event is received, the other Realms will get their
     * objects and {@link io.realm.RealmResults} updated to reflect
     * the changes from this commit.
     *
     * @throws java.lang.IllegalStateException If the write transaction is in an invalid state or incorrect thread.
     */
    public void commitTransaction() {
        checkIfValid();
        sharedGroupManager.commitAndContinueAsRead();

        for (Map.Entry<Handler, String> handlerIntegerEntry : handlers.entrySet()) {
            Handler handler = handlerIntegerEntry.getKey();
            String realmPath = handlerIntegerEntry.getValue();

            // Notify at once on thread doing the commit
            if (handler.equals(this.handler)) {
                sendNotifications();
                continue;
            }

            // For all other threads, use the Handler
            if (
                    realmPath.equals(configuration.getPath())    // It's the right realm
                            && !handler.hasMessages(REALM_CHANGED)       // The right message
                            && handler.getLooper().getThread().isAlive() // The receiving thread is alive
                    ) {
                if (!handler.sendEmptyMessage(REALM_CHANGED)) {
                    RealmLog.w("Cannot update Looper threads when the Looper has quit. Use realm.setAutoRefresh(false) " +
                            "to prevent this.");
                }
            }
        }
    }

    /**
     * Revert all writes (created, updated, or deleted objects) made in the current write
     * transaction and end the transaction.
     * <br>
     * The Realm reverts back to read-only.
     * <br>
     * Calling this when not in a write transaction will throw an exception.
     *
     * @throws java.lang.IllegalStateException If the write transaction is an invalid state,
     *                                         not in a write transaction or incorrect thread.
     */
    public void cancelTransaction() {
        checkIfValid();
        sharedGroupManager.rollbackAndContinueAsRead();
    }

    /**
     * Checks if a Realm's underlying resources are still available or not getting accessed from
     * the wrong thread.
     */
    protected void checkIfValid() {
        // Check if the Realm instance has been closed
        if (sharedGroupManager != null && !sharedGroupManager.isOpen()) {
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
     * @return The canonical path to the Realm file.
     * @see File#getCanonicalPath()
     */
    public String getPath() {
        return configuration.getPath();
    }

    /**
     * Returns the {@link RealmConfiguration} for this Realm.
     *
     * @return {@link RealmConfiguration} for this Realm.
     */
    public RealmConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the schema version for this Realm.
     *
     * @return The schema version for the Realm file backing this Realm.
     */
    public long getVersion() {
        if (!sharedGroupManager.hasTable("metadata")) {
            return UNVERSIONED;
        }
        Table metadataTable = sharedGroupManager.getTable("metadata");
        return metadataTable.getLong(0, 0);
    }

    /**
     * Closes the Realm instance and all its resources.
     * <p>
     * It's important to always remember to close Realm instances when you're done with it in order
     * not to leak memory, file descriptors or grow the size of Realm file out of measure.
     *
     * @throws java.lang.IllegalStateException if trying to close Realm on a different thread than the
     *                                         one it was created on.
     */
    @Override
    public void close() {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_CLOSE_MESSAGE);
        }

        Map<RealmConfiguration, Integer> localRefCount = getLocalReferenceCount();
        String canonicalPath = configuration.getPath();
        Integer references = localRefCount.get(configuration);
        if (references == null) {
            references = 0;
        }
        if (sharedGroupManager != null && references == 1) {
            lastLocalInstanceClosed();
            sharedGroupManager.close();
            sharedGroupManager = null;
            releaseFileReference(configuration);
        }

        int refCount = references - 1;
        if (refCount < 0) {
            RealmLog.w("Calling close() on a Realm that is already closed: " + canonicalPath);
        }
        localRefCount.put(configuration, Math.max(0, refCount));

        if (handler != null && refCount <= 0) {
            removeHandler(handler);
        }
    }

    /**
     * Returns the ThreadLocal reference counter for this Realm type.
     */
    protected abstract Map<RealmConfiguration, Integer> getLocalReferenceCount();

    /**
     * Callback when the last ThreadLocal instance of this Realm type has been closed.
     */
    protected abstract void lastLocalInstanceClosed();

    /**
     * Acquire a reference to the Realm file referenced in the given configuration.
     */
    static synchronized void acquireFileReference(RealmConfiguration configuration) {
        String path = configuration.getPath();
        Integer refCount = globalRealmFileReferenceCounter.get(path);
        if (refCount == null) {
            refCount = 0;
        }
        globalRealmFileReferenceCounter.put(path, refCount + 1);
    }

    /**
     * Releases a reference to the Realm file. If reference count reaches 0 any cached configurations
     * will be removed.
     */
    static synchronized void releaseFileReference(RealmConfiguration configuration) {
        String canonicalPath = configuration.getPath();
        List<RealmConfiguration> pathConfigurationCache = globalPathConfigurationCache.get(canonicalPath);
        pathConfigurationCache.remove(configuration);
        if (pathConfigurationCache.isEmpty()) {
            globalPathConfigurationCache.remove(canonicalPath);
        }

        Integer refCount = globalRealmFileReferenceCounter.get(canonicalPath);
        if (refCount == null || refCount == 0) {
            throw new IllegalStateException("Trying to release a Realm file that is already closed");
        }
        globalRealmFileReferenceCounter.put(canonicalPath, refCount - 1);
    }

    // Public because of migrations
    @Deprecated
    public Table getTable(String className) {
        className = Table.TABLE_PREFIX + className;
        Table table = dynamicClassToTable.get(className);
        if (table == null) {
            table = sharedGroupManager.getTable(className);
            dynamicClassToTable.put(className, table);
        }
        return table;
    }

    // Public because of migrations
    @Deprecated
    public Table getTable(Class<? extends RealmObject> clazz) {
        Table table = classToTable.get(clazz);
        if (table == null) {
            clazz = Util.getOriginalModelClass(clazz);
            table = sharedGroupManager.getTable(configuration.getSchemaMediator().getTableName(clazz));
            classToTable.put(clazz, table);
        }
        return table;
    }

    boolean hasChanged() {
        return sharedGroupManager.hasChanged();
    }

    // package protected so unit tests can access it
    void setVersion(long version) {
        Table metadataTable = sharedGroupManager.getTable("metadata");
        if (metadataTable.getColumnCount() == 0) {
            metadataTable.addColumn(RealmFieldType.INTEGER, "version");
            metadataTable.addEmptyRow();
        }
        metadataTable.setLong(0, 0, version);
    }

    /**
     * Sort a table using the given field names and sorting directions. If a field name doesn not
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

    /**
     * Make sure that the new configuration doesn't clash with any existing configurations for the
     * Realm.
     *
     * @throws IllegalArgumentException If the new configuration isn't valid.
     */
    protected static synchronized void validateAgainstExistingConfigurations(RealmConfiguration newConfiguration) {

        String realmPath = newConfiguration.getPath();
        List<RealmConfiguration> pathConfigurationCache = globalPathConfigurationCache.get(realmPath);

        if (pathConfigurationCache != null && pathConfigurationCache.size() > 0) {

            // For the current restrictions, it is enough to just check one of the existing configurations.
            RealmConfiguration cachedConfiguration = pathConfigurationCache.get(0);

            // Check that encryption keys aren't different
            if (!Arrays.equals(cachedConfiguration.getEncryptionKey(), newConfiguration.getEncryptionKey())) {
                throw new IllegalArgumentException(DIFFERENT_KEY_MESSAGE);
            }

            // Check schema versions are the same
            if (cachedConfiguration.getSchemaVersion() != newConfiguration.getSchemaVersion()) {
                throw new IllegalArgumentException(String.format("Configurations cannot have different schema versions " +
                                "if used to open the same file. %d vs. %d", cachedConfiguration.getSchemaVersion(),
                        newConfiguration.getSchemaVersion()));
            }

            // Check that schema is the same
            RealmProxyMediator cachedSchema = cachedConfiguration.getSchemaMediator();
            RealmProxyMediator schema = newConfiguration.getSchemaMediator();
            if (!cachedSchema.equals(schema)) {
                throw new IllegalArgumentException("Two configurations with different schemas are trying to open " +
                        "the same Realm file. Their schema must be the same: " + newConfiguration.getPath());
            }

            // Check if the durability is the same
            SharedGroup.Durability cachedDurability = cachedConfiguration.getDurability();
            SharedGroup.Durability newDurability = newConfiguration.getDurability();
            if (!cachedDurability.equals(newDurability)) {
                throw new IllegalArgumentException("A Realm cannot be both in-memory and persisted. Two conflicting " +
                        "configurations pointing to " + newConfiguration.getPath() + " are being used.");
            }
        }
    }

    // Used by proxy classes
    <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {
        Table table = getTable(clazz);
        UncheckedRow row = table.getUncheckedRow(rowIndex);
        E result = configuration.getSchemaMediator().newInstance(clazz);
        result.row = row;
        result.realm = this;
        return result;
    }

    // Used by RealmList/RealmResults
    // Invariant: if dynamicClassName != null -> clazz == DynamicRealmObject
    <E extends RealmObject> E get(Class<E> clazz, String dynamicClassName, long rowIndex) {
        Table table;
        E result;
        if (dynamicClassName != null) {
            table = getTable(dynamicClassName);
            result = (E) new DynamicRealmObject();
        } else {
            table = getTable(clazz);
            result = configuration.getSchemaMediator().newInstance(clazz);
        }
        UncheckedRow row = table.getUncheckedRow(rowIndex); // TODO Checked row for dynamic object
        result.row = row;
        result.realm = this;
        return result;
    }


    /**
     * Deletes the Realm file defined by the given configuration.
     */
    protected static synchronized boolean deleteRealm(RealmConfiguration configuration) {
        if (isFileOpen(configuration)) {
            throw new IllegalStateException("It's not allowed to delete the file associated with an open Realm. " +
                    "Remember to close() all the instances of the Realm before deleting its file.");
        }

        boolean realmDeleted = true;
        String canonicalPath = configuration.getPath();
        File realmFolder = configuration.getRealmFolder();
        String realmFileName = configuration.getRealmFileName();
        List<File> filesToDelete = Arrays.asList(new File(canonicalPath),
                new File(realmFolder, realmFileName + ".lock"),
                new File(realmFolder, realmFileName + ".lock_a"),
                new File(realmFolder, realmFileName + ".lock_b"),
                new File(realmFolder, realmFileName + ".log"));
        for (File fileToDelete : filesToDelete) {
            if (fileToDelete.exists()) {
                boolean deleteResult = fileToDelete.delete();
                if (!deleteResult) {
                    realmDeleted = false;
                    RealmLog.w("Could not delete the file " + fileToDelete);
                }
            }
        }

        return realmDeleted;
    }

    /**
     * Compacts the Realm file defined by the given configuration.
     */
    public static synchronized boolean compactRealm(RealmConfiguration configuration) {
        if (configuration.getEncryptionKey() != null) {
            throw new IllegalArgumentException("Cannot currently compact an encrypted Realm.");
        }

        if (isFileOpen(configuration)) {
            throw new IllegalStateException("Cannot compact an open Realm");
        }

        return SharedGroupManager.compact(configuration);
    }

    /**
     * Migrates the Realm file defined by the given configuration using the provided migration block.
     */
    public static synchronized void migrateRealm(RealmConfiguration configuration, RealmMigration migration, MigrationCallback callback) {
        if (configuration == null) {
            throw new IllegalArgumentException("RealmConfiguration must be provided");
        }
        if (migration == null && configuration.getMigration() == null) {
            throw new RealmMigrationNeededException(configuration.getPath(), "RealmMigration must be provided");
        }

        RealmMigration realmMigration = (migration == null) ? configuration.getMigration() : migration;
        BaseRealm realm = null;
        try {
            realm = callback.getRealm(configuration);
            realm.beginTransaction();
            realm.setVersion(realmMigration.execute((Realm) realm, realm.getVersion())); // FIXME Remove cast with new migration API
            realm.commitTransaction();
        } finally {
            if (realm != null) {
                realm.close();
                callback.migrationComplete();
            }
        }
    }

    // Internal Handler callback for Realm messages
    private class RealmCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == REALM_CHANGED) {
                sharedGroupManager.advanceRead();
                sendNotifications();
            }
            return true;
        }
    }

    // Internal delegate for migrations
    protected interface MigrationCallback {
        BaseRealm getRealm(RealmConfiguration configuration);

        void migrationComplete();
    }
}
