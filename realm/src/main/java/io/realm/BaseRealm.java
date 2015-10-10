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
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import io.realm.exceptions.RealmEncryptionNotSupportedException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedGroup;
import io.realm.internal.SharedGroupManager;
import io.realm.internal.Table;
import io.realm.internal.android.DebugAndroidLogger;
import io.realm.internal.android.ReleaseAndroidLogger;
import io.realm.internal.async.QueryUpdateTask;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.log.RealmLog;

/**
 * Base class for all Realm instances.
 *
 * @see io.realm.Realm
 */
abstract class BaseRealm implements Closeable {

    static final int REALM_CHANGED = 14930352; // Hopefully it won't clash with other message IDs.
    static final int REALM_UPDATE_ASYNC_QUERIES = 24157817;
    static final int REALM_COMPLETED_ASYNC_QUERY = 39088169;
    static final int REALM_COMPLETED_ASYNC_FIND_FIRST = 63245986;

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

    // List of Realm files that has already been validated
    static final Set<String> validatedRealmFiles = new HashSet<String>();

    // keep a WeakReference list to RealmResults obtained asynchronously in order to update them
    // RealmQuery is not WeakReferenced to prevent it from being GC'd. RealmQuery should be
    // cleaned if RealmResults is cleaned. we need to keep RealmQuery because it contains the query
    // pointer (to handover for each update) + all the arguments necessary to rerun the query:
    // sorting orders, soring columns, type (findAll, findFirst, findAllSorted etc.)
    final Map<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<? extends RealmObject>> asyncRealmResults =
            new IdentityHashMap<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<? extends RealmObject>>();
    final ReferenceQueue<RealmResults<? extends RealmObject>> referenceQueue = new ReferenceQueue<RealmResults<? extends RealmObject>>();
    final Map<WeakReference<RealmObject>, RealmQuery<? extends RealmObject>> asyncRealmObjects =
            new IdentityHashMap<WeakReference<RealmObject>, RealmQuery<? extends RealmObject>>();

    // thread pool for all async operations (Query & Write transaction)
    static final RealmThreadPoolExecutor asyncQueryExecutor = RealmThreadPoolExecutor.getInstance();

    // pending update of async queries
    protected Future updateAsyncQueriesTask;

    protected final List<WeakReference<RealmChangeListener>> changeListeners =
            new CopyOnWriteArrayList<WeakReference<RealmChangeListener>>();

    protected long threadId;
    protected RealmConfiguration configuration;
    protected SharedGroupManager sharedGroupManager;
    protected boolean autoRefresh;
    Handler handler;

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
     *     <li>Immediately if a change was committed by the local thread</li>
     *     <li>On every loop of a Handler thread if changes were committed by another thread</li>
     *     <li>On every call to {@link io.realm.Realm#refresh()}</li>
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

    void setHandler (Handler handler) {
        // remove the old one
        handlers.remove(this.handler);
        handlers.put(handler, configuration.getPath());
        this.handler = handler;
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
     * @throws java.lang.IllegalStateException If already in a write transaction or incorrect thread.
     *
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
     * @throws java.lang.IllegalStateException    If the write transaction is an invalid state,
     *                                             not in a write transaction or incorrect thread.
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
     * @return The canonical path to the Realm file.
     * @see File#getCanonicalPath()
     */
    public String getPath() {
        return configuration.getPath();
    }

    /**
     * Returns the {@link RealmConfiguration} for this Realm.
     * @return {@link RealmConfiguration} for this Realm.
     */
    public RealmConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the schema version for this Realm.
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
     * one it was created on.
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
     * Check if the {@link io.realm.Realm} instance has already been closed.
     *
     * @return {@code true} if closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }

        return sharedGroupManager == null || !sharedGroupManager.isOpen();
    }

    /**
     * Returns the ThreadLocal reference counter for this Realm.
     */
    protected abstract Map<RealmConfiguration,Integer> getLocalReferenceCount();

    /**
     * Callback when the last ThreadLocal instance of this Realm type has been closed.
     */
    protected abstract void lastLocalInstanceClosed();

    /**
     * Acquire a reference to the given Realm file.
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

    // package protected so unit tests can access it
    protected void setVersion(long version) {
        Table metadataTable = sharedGroupManager.getTable("metadata");
        if (metadataTable.getColumnCount() == 0) {
            metadataTable.addColumn(ColumnType.INTEGER, "version");
            metadataTable.addEmptyRow();
        }
        metadataTable.setLong(0, 0, version);
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
            switch (message.what) {
                case REALM_CHANGED: {
                    if (threadContainsAsyncQueries()) {
                        updateAsyncQueries();

                    } else {
                        RealmLog.d("REALM_CHANGED realm:"+ BaseRealm.this + " no async queries, advance_read");
                        sharedGroupManager.advanceRead();
                        sendNotifications();
                    }
                    break;
                }
                case REALM_COMPLETED_ASYNC_QUERY: {
                    // one async query has completed
                    QueryUpdateTask.Result result = (QueryUpdateTask.Result) message.obj;
                    completedAsyncQueryUpdate(result);
                    break;
                }
                case REALM_UPDATE_ASYNC_QUERIES: {
                    // this is called once the background thread completed the update of the async queries
                    QueryUpdateTask.Result result = (QueryUpdateTask.Result) message.obj;
                    completedAsyncQueriesUpdate(result);
                    break;
                }
                case REALM_COMPLETED_ASYNC_FIND_FIRST: {
                    QueryUpdateTask.Result result = (QueryUpdateTask.Result) message.obj;
                    completedAsyncFindFirst(result);
                    break;
                }
            }
            return true;
        }
    }

    private void updateAsyncQueries () {
        if (updateAsyncQueriesTask != null && !updateAsyncQueriesTask.isDone()) {
            // try to cancel any pending update since we're submitting a new one anyway
            updateAsyncQueriesTask.cancel(true);
            asyncQueryExecutor.getQueue().remove(updateAsyncQueriesTask);
            RealmLog.d("REALM_CHANGED realm:"+ BaseRealm.this + " cancelling pending REALM_UPDATE_ASYNC_QUERIES updates");
        }
        RealmLog.d("REALM_CHANGED realm:"+ BaseRealm.this + " updating async queries, total: " + asyncRealmResults.size());
        // prepare a QueryUpdateTask to current async queries in this thread
        QueryUpdateTask.Builder.UpdateQueryStep updateQueryStep = QueryUpdateTask.newBuilder()
                .realmConfiguration(getConfiguration());
        QueryUpdateTask.Builder.RealmResultsQueryStep realmResultsQueryStep = null;

        // we iterate over non GC'd async RealmResults then add them to the list to be updated (in a batch)
        Iterator<Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>>> iterator = asyncRealmResults.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>> entry = iterator.next();
            WeakReference<RealmResults<? extends RealmObject>> weakReference = entry.getKey();
            RealmResults<? extends RealmObject> realmResults = weakReference.get();
            if (realmResults == null) {
                // GC'd instance remove from the list
                iterator.remove();

            } else {
                realmResultsQueryStep = updateQueryStep.add(weakReference,
                        entry.getValue().handoverQueryPointer(),
                        entry.getValue().getArgument());
            }

            // Note: we're passing an WeakRef of a RealmResults to another thread
            //       this is safe as long as we don't invoke any of the RealmResults methods.
            //       we're just using it as a Key in an IdentityHashMap (i.e doesn't call
            //       AbstractList's hashCode, that require accessing objects from another thread)
            //
            //       watch out when you debug, as you're IDE try to evaluate RealmResults
            //       which break the Thread confinement constraints.
        }
        if (realmResultsQueryStep != null) {
            QueryUpdateTask queryUpdateTask = realmResultsQueryStep
                    .sendToHandler(handler, REALM_UPDATE_ASYNC_QUERIES)
                    .build();
            updateAsyncQueriesTask = asyncQueryExecutor.submit(queryUpdateTask);
        }
    }

    private void completedAsyncQueryUpdate(QueryUpdateTask.Result result) {
        Set<WeakReference<RealmResults<? extends RealmObject>>> updatedTableViewsKeys = result.updatedTableViews.keySet();
        if (updatedTableViewsKeys.size() > 0) {
            WeakReference<RealmResults<? extends RealmObject>> weakRealmResults = updatedTableViewsKeys.iterator().next();

            RealmResults<? extends RealmObject> realmResults = weakRealmResults.get();
            if (realmResults == null) {
                asyncRealmResults.remove(weakRealmResults);
                RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] realm:"+ BaseRealm.this + " RealmResults GC'd ignore results");

            } else {
                SharedGroup.VersionID callerVersionID = sharedGroupManager.getVersion();
                int compare = callerVersionID.compareTo(result.versionID);
                if (compare == 0) {
                    // if the RealmResults is empty (has not completed yet) then use the value
                    // otherwise a task (grouped update) has already updated this RealmResults
                    if (!realmResults.isLoaded()) {
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ BaseRealm.this + " same versions, using results (RealmResults is not loaded)");
                        // swap pointer
                        realmResults.swapTableViewPointer(result.updatedTableViews.get(weakRealmResults));
                        // notify callbacks
                        realmResults.notifyChangeListeners();
                    } else {
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ BaseRealm.this + " ignoring result the RealmResults (is already loaded)");
                    }

                } else if (compare > 0) {
                    // we have two use cases:
                    // 1- this RealmResults is not empty, this means that after we started the async
                    //    query, we received a REALM_CHANGE that triggered an update of all async queries
                    //    including the last async submitted, so no need to use the provided TableView pointer
                    //    (or the user forced the sync behaviour .load())
                    // 2- This RealmResults is still empty but this caller thread is advanced than the worker thread
                    //    this could happen if the current thread advanced the shared_group (via a write or refresh)
                    //    this means that we need to rerun the query against a newer worker thread.

                    if (!realmResults.isLoaded()) { // UC2
                        // UC covered by this test: RealmAsyncQueryTests#testFindAllAsyncRetry
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY " + weakRealmResults + "] , realm:"+ BaseRealm.this + " caller is more advanced & RealmResults is not loaded, rerunning the query against the latest version");

                        RealmQuery<?> query = asyncRealmResults.get(weakRealmResults);
                        QueryUpdateTask queryUpdateTask = QueryUpdateTask.newBuilder()
                                .realmConfiguration(getConfiguration())
                                .add(weakRealmResults,
                                        query.handoverQueryPointer(),
                                        query.getArgument())
                                .sendToHandler(handler, REALM_COMPLETED_ASYNC_QUERY)
                                .build();

                        asyncQueryExecutor.submit(queryUpdateTask);

                    } else {
                        // UC covered by this test: RealmAsyncQueryTests#testFindAllCallerIsAdvanced
                        RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ BaseRealm.this + " caller is more advanced & RealmResults is loaded ignore the outdated result");
                    }

                } else {
                    // the caller thread is behind the worker thread,
                    // no need to rerun the query, since we're going to receive the update signal
                    // & batch update all async queries including this one
                    // UC covered by this test: RealmAsyncQueryTests#testFindAllCallerThreadBehind
                    RealmLog.d("[REALM_COMPLETED_ASYNC_QUERY "+ weakRealmResults + "] , realm:"+ BaseRealm.this + " caller thread behind worker thread, ignore results (a batch update will update everything including this query)");
                }
            }
        }
    }

    private void completedAsyncQueriesUpdate(QueryUpdateTask.Result result) {
        SharedGroup.VersionID callerVersionID = sharedGroupManager.getVersion();
        int compare = callerVersionID.compareTo(result.versionID);
        if (compare > 0) {
            RealmLog.d("REALM_UPDATE_ASYNC_QUERIES realm:" + BaseRealm.this + " caller is more advanced, rerun updates");
            // The caller is more advance than the updated queries ==>
            // need to refresh them again (if there is still queries)
            handler.sendEmptyMessage(REALM_CHANGED);

        } else {
            // We're behind or on the same version as the worker thread

            // only advance if we're behind
            if (compare != 0) {
                // no need to remove old pointers from TableView, since they're
                // imperative TV, they will not rerun if the SharedGroup advance

                // UC covered by this test: RealmAsyncQueryTests#testFindAllCallerThreadBehind
                RealmLog.d("REALM_UPDATE_ASYNC_QUERIES realm:"+ BaseRealm.this + " caller is behind  advance_read");
                // refresh the Realm to the version provided by the worker thread
                // (advanceRead to the latest version may cause a version mismatch error) preventing us
                // from importing correctly the handover table view
                sharedGroupManager.advanceRead(result.versionID);
            }

            ArrayList<RealmResults<? extends RealmObject>> callbacksToNotify = new ArrayList<RealmResults<? extends RealmObject>>(result.updatedTableViews.size());
            // use updated TableViews pointers for the existing async RealmResults
            for (Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, Long> query : result.updatedTableViews.entrySet()) {
                WeakReference<RealmResults<? extends RealmObject>> weakRealmResults = query.getKey();
                RealmResults<? extends RealmObject> realmResults = weakRealmResults.get();
                if (realmResults == null) {
                    // don't update GC'd instance
                    asyncRealmResults.remove(weakRealmResults);

                } else {
                    // it's dangerous to notify the callback about new results before updating
                    // the pointers, because the callback may use another RealmResults not updated yet
                    // this is why we defer the notification until we're done updating all pointers

                    // TODO find a way to only notify callbacks if the underlying data changed compared
                    //      to the existing value(s) for this RealmResults (use a hashCode?)
                    callbacksToNotify.add(realmResults);

                    RealmLog.d("REALM_UPDATE_ASYNC_QUERIES realm:"+ BaseRealm.this + " updating RealmResults " + weakRealmResults);
                    // update the instance with the new pointer
                    realmResults.swapTableViewPointer(query.getValue());
                }
            }

            for (RealmResults<? extends RealmObject> query : callbacksToNotify) {
                query.notifyChangeListeners();
            }

            // notify listeners only when we advanced
            if (compare != 0) {
                sendNotifications();
            }

            updateAsyncQueriesTask = null;
        }
    }

    private void completedAsyncFindFirst(QueryUpdateTask.Result result) {
        Set<WeakReference<RealmObject>> updatedRowKey = result.updatedRow.keySet();
        if (updatedRowKey.size() > 0) {
            WeakReference<RealmObject> realmObjectWeakReference = updatedRowKey.iterator().next();
            RealmObject realmObject = realmObjectWeakReference.get();

            if (realmObject != null) {
                SharedGroup.VersionID callerVersionID = sharedGroupManager.getVersion();
                int compare = callerVersionID.compareTo(result.versionID);
                // we always query on the same version
                // only two use cases could happen 1. we're on the same version or 2. the caller has advanced in the meanwhile
                if (compare == 0) { //same version import the handover
                    realmObject.onCompleted(result.updatedRow.get(realmObjectWeakReference));
                    asyncRealmObjects.remove(realmObjectWeakReference);

                } else if (compare > 0) {
                    // the caller has advanced we need to
                    // retry against the current version of the caller
                    RealmQuery<?> realmQuery = asyncRealmObjects.get(realmObjectWeakReference);

                    QueryUpdateTask queryUpdateTask = QueryUpdateTask.newBuilder()
                            .realmConfiguration(getConfiguration())
                            .addObject(realmObjectWeakReference,
                                    realmQuery.handoverQueryPointer(),
                                    realmQuery.getArgument())
                            .sendToHandler(handler, REALM_COMPLETED_ASYNC_FIND_FIRST)
                            .build();

                    asyncQueryExecutor.submit(queryUpdateTask);
                } else {
                    // should not happen, since the the background thread position itself against the provided version
                    // and the caller thread can only go forward (advance_read)
                    throw new IllegalStateException("Caller thread behind the worker thread");
                }
            } // else: element GC'd in the meanwhile
        }
    }

    /**
     * This will prevent advanceReading from accidentally advancing the thread and potentially re-run the queries in this thread.
     * @return {@code true} if there is at least one (non GC'd) instance of {@link RealmResults} {@code false} otherwise
     */
    private boolean threadContainsAsyncQueries () {
        deleteWeakReferences();
        boolean isEmpty = true;
        Iterator<Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>>> iterator = asyncRealmResults.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WeakReference<RealmResults<? extends RealmObject>>, RealmQuery<?>> next = iterator.next();
            if (next.getKey().get() == null) {
                // clean the GC'ed instances
                // we could've avoided this if we had a 'WeakIdentityHashmap' data structure. miss Guava :(
                iterator.remove();
            } else {
                isEmpty = false;
            }
        }

        return !isEmpty;
    }

    private void deleteWeakReferences() {
        // From the AOSP FinalizationTest:
        // https://android.googlesource.com/platform/libcore/+/master/support/src/test/java/libcore/
        // java/lang/ref/FinalizationTester.java
        // System.gc() does not garbage collect every time. Runtime.gc() is
        // more likely to perform a gc.
        Runtime.getRuntime().gc();
        Reference<? extends RealmResults<? extends RealmObject>> weakReference;
        while ((weakReference = referenceQueue.poll()) != null ) { // Does not wait for a reference to become available.
            asyncRealmResults.remove(weakReference);
        }
    }

    // Internal delegate for migrations
    protected interface MigrationCallback {
        BaseRealm getRealm(RealmConfiguration configuration);
        void migrationComplete();
    }
}
