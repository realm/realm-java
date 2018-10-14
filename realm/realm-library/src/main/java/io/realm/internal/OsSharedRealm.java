/*
 * Copyright 2016 Realm Inc.
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

package io.realm.internal;

import java.io.Closeable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;
import io.realm.internal.annotations.ObjectServer;
import io.realm.sync.permissions.RealmPrivileges;

@Keep
public final class OsSharedRealm implements Closeable, NativeObject {

    public static class VersionID implements Comparable<VersionID> {
        public final long version;
        public final long index;

        VersionID(long version, long index) {
            this.version = version;
            this.index = index;
        }

        @Override
        public int compareTo(@SuppressWarnings("NullableProblems") VersionID another) {
            //noinspection ConstantConditions
            if (another == null) {
                throw new IllegalArgumentException("Version cannot be compared to a null value.");
            }
            if (version > another.version) {
                return 1;
            } else if (version < another.version) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "VersionID{" +
                    "version=" + version +
                    ", index=" + index +
                    '}';
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            VersionID versionID = (VersionID) object;
            return (version == versionID.version && index == versionID.index);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) (version ^ (version >>> 32));
            result = 31 * result + (int) (index ^ (index >>> 32));
            return result;
        }
    }

    /**
     * The migration callback which will be called when manual migration is needed.
     */
    @Keep
    public interface MigrationCallback {

        /**
         * Callback function.
         *
         * @param sharedRealm the same {@link OsSharedRealm} instance which has been created from the same
         *                    {@link OsRealmConfig} instance.
         * @param oldVersion  the schema version of the existing Realm file.
         * @param newVersion  the expected schema version after migration.
         */
        void onMigrationNeeded(OsSharedRealm sharedRealm, long oldVersion, long newVersion);
    }

    /**
     * Callback function to be executed when the schema is created.
     */
    @Keep
    public interface InitializationCallback {
        /**
         * @param sharedRealm a {@link OsSharedRealm} instance which is in transaction state.
         */
        void onInit(OsSharedRealm sharedRealm);
    }

    /**
     * Callback function to be called from JNI by Object Store when the schema is changed.
     */
    @Keep
    public interface SchemaChangedCallback {
        // Called from JNI
        @SuppressWarnings("unused")
        void onSchemaChanged();
    }

    // Const value for RealmFileException conversion
    public static final byte FILE_EXCEPTION_KIND_ACCESS_ERROR = 0;
    public static final byte FILE_EXCEPTION_KIND_BAD_HISTORY = 1;
    public static final byte FILE_EXCEPTION_KIND_PERMISSION_DENIED = 2;
    public static final byte FILE_EXCEPTION_KIND_EXISTS = 3;
    public static final byte FILE_EXCEPTION_KIND_NOT_FOUND = 4;
    public static final byte FILE_EXCEPTION_KIND_INCOMPATIBLE_LOCK_FILE = 5;
    public static final byte FILE_EXCEPTION_KIND_FORMAT_UPGRADE_REQUIRED = 6;
    public static final byte FILE_EXCEPTION_INCOMPATIBLE_SYNC_FILE = 7;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    private final long nativePtr;
    private final OsRealmConfig osRealmConfig;
    final NativeContext context;
    private final OsSchemaInfo schemaInfo;
    private static volatile File temporaryDirectory;
    // JNI will only hold a weak global ref to this.
    public final RealmNotifier realmNotifier;
    public final Capabilities capabilities;
    // For the Java callbacks during constructing in Object Store, some temporary OsSharedRealm objects need to be
    // created as the parameter of the callback. The native pointers of those temp OsSharedRealm objects have to be
    // valid during the whole life cycle of the Java object. The living native pointers still hold a ref-count to the
    // SharedRealm which means the SharedRealm won't be closed automatically if there is any exception throws during
    // construction. GC will clear them later, but that would be too late. So we are tracking the temp OsSharedRealm
    // during the construction stage and manually close them if exception throws.
    private final static List<OsSharedRealm> sharedRealmsUnderConstruction = new CopyOnWriteArrayList<OsSharedRealm>();
    private final List<OsSharedRealm> tempSharedRealmsForCallback = new ArrayList<OsSharedRealm>();

    private final List<WeakReference<PendingRow>> pendingRows = new CopyOnWriteArrayList<>();
    // Package protected for testing
    final List<WeakReference<OsResults.Iterator>> iterators = new ArrayList<>();

    private OsSharedRealm(OsRealmConfig osRealmConfig) {
        Capabilities capabilities = new AndroidCapabilities();
        RealmNotifier realmNotifier = new AndroidRealmNotifier(this, capabilities);

        // SharedRealms under constructions are identified by the Context.
        this.context = osRealmConfig.getContext();
        sharedRealmsUnderConstruction.add(this);
        try {
            this.nativePtr = nativeGetSharedRealm(osRealmConfig.getNativePtr(), realmNotifier);
        } catch (Throwable t) {
            // The SharedRealm instances have to be closed before throw.
            for (OsSharedRealm sharedRealm: tempSharedRealmsForCallback) {
                if (!sharedRealm.isClosed()) {
                    sharedRealm.close();
                }
            }
            throw t;
        } finally {
            tempSharedRealmsForCallback.clear();
            sharedRealmsUnderConstruction.remove(this);
        }
        this.osRealmConfig = osRealmConfig;
        this.schemaInfo = new OsSchemaInfo(nativeGetSchemaInfo(nativePtr), this);
        this.context.addReference(this);

        this.capabilities = capabilities;
        this.realmNotifier = realmNotifier;
        nativeSetAutoRefresh(nativePtr, capabilities.canDeliverNotification());
    }

    /**
     * Creates a {@code OsSharedRealm} instance from a given Object Store's {@code OsSharedRealm} pointer. This is used to
     * create {@code OsSharedRealm} from the callback functions. When this is called, there is another
     * {@code OsSharedRealm} instance with the same {@link OsRealmConfig} which has been created before. Although they
     * are different {@code shared_ptr}, they point to the same {@code SharedGroup} instance. The {@code context} has
     * to be the same one to ensure core's destructor thread safety.
     */
    private OsSharedRealm(long nativeSharedRealmPtr, OsRealmConfig osRealmConfig) {
        this.nativePtr = nativeSharedRealmPtr;
        this.osRealmConfig = osRealmConfig;
        this.schemaInfo = new OsSchemaInfo(nativeGetSchemaInfo(nativePtr), this);
        this.context = osRealmConfig.getContext();
        this.context.addReference(this);

        this.capabilities = new AndroidCapabilities();
        // This instance should never need notifications.
        this.realmNotifier = null;
        nativeSetAutoRefresh(nativePtr, false);

        boolean foundParentSharedRealm = false;
        for (OsSharedRealm sharedRealm : sharedRealmsUnderConstruction) {
            if (sharedRealm.context == osRealmConfig.getContext())  {
                foundParentSharedRealm = true;
                sharedRealm.tempSharedRealmsForCallback.add(this);
                break;
            }
        }
        if (!foundParentSharedRealm) {
            throw new IllegalStateException("Cannot find the parent 'OsSharedRealm' which is under construction.");
        }
    }


    /**
     * Creates a {@code OsSharedRealm} instance in dynamic schema mode.
     */
    public static OsSharedRealm getInstance(RealmConfiguration config) {
        OsRealmConfig.Builder builder = new OsRealmConfig.Builder(config);
        return getInstance(builder);
    }

    /**
     * Creates a {@code ShareRealm} instance from the given {@link OsRealmConfig.Builder}.
     */
    public static OsSharedRealm getInstance(OsRealmConfig.Builder configBuilder) {
        OsRealmConfig osRealmConfig = configBuilder.build();
        ObjectServerFacade.getSyncFacadeIfPossible().wrapObjectStoreSessionIfRequired(osRealmConfig);

        return new OsSharedRealm(osRealmConfig);
    }

    public static void initialize(File tempDirectory) {
        if (OsSharedRealm.temporaryDirectory != null) {
            // already initialized
            return;
        }

        String temporaryDirectoryPath = tempDirectory.getAbsolutePath();
        if (!tempDirectory.isDirectory() && !tempDirectory.mkdirs() && !tempDirectory.isDirectory()) {
            throw new IOException("failed to create temporary directory: " + temporaryDirectoryPath);
        }

        if (!temporaryDirectoryPath.endsWith("/")) {
            temporaryDirectoryPath += "/";
        }
        nativeInit(temporaryDirectoryPath);
        OsSharedRealm.temporaryDirectory = tempDirectory;
    }

    public static File getTemporaryDirectory() {
        return temporaryDirectory;
    }

    public void beginTransaction() {
        detachIterators();
        executePendingRowQueries();
        nativeBeginTransaction(nativePtr);
    }

    public void commitTransaction() {
        nativeCommitTransaction(nativePtr);
    }

    public void cancelTransaction() {
        nativeCancelTransaction(nativePtr);
    }

    public boolean isInTransaction() {
        return nativeIsInTransaction(nativePtr);
    }

    public boolean hasTable(String name) {
        return nativeHasTable(nativePtr, name);
    }

    /**
     * Gets an existing {@link Table} with the given name.
     *
     * @param name the name of table.
     * @return a {@link Table} object.
     * @throws IllegalArgumentException if the table doesn't exist.
     */
    public Table getTable(String name) {
        long tablePtr = nativeGetTable(nativePtr, name);
        return new Table(this, tablePtr);
    }

    /**
     * Creates a {@link Table} with then given name. Native assertion will happen if the table with the same name
     * exists.
     *
     * @param name the name of table.
     * @return a created {@link Table} object.
     */
    public Table createTable(String name) {
        return new Table(this, nativeCreateTable(nativePtr, name));
    }

    /**
     * Creates a {@link Table} and adds a primary key field to it. Native assertion will happen if the table with the
     * same name exists.
     *
     * @param tableName           the name of table.
     * @param primaryKeyFieldName the name of primary key field.
     * @param isStringType        if this is true, the primary key field will be create as a string field. Otherwise it will
     *                            be created as an integer field.
     * @param isNullable          if the primary key field is nullable or not.
     * @return a newly created {@link Table} object.
     */
    public Table createTableWithPrimaryKey(String tableName, String primaryKeyFieldName, boolean isStringType,
                                           boolean isNullable) {
        return new Table(this, nativeCreateTableWithPrimaryKeyField(nativePtr, tableName, primaryKeyFieldName,
                isStringType, isNullable));
    }

    public void renameTable(String oldName, String newName) {
        nativeRenameTable(nativePtr, oldName, newName);
    }

    public String getTableName(int index) {
        return nativeGetTableName(nativePtr, index);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public String getPath() {
        return osRealmConfig.getRealmConfiguration().getPath();
    }

    public boolean isEmpty() {
        return nativeIsEmpty(nativePtr);
    }

    public void refresh() {
        nativeRefresh(nativePtr);
    }

    public OsSharedRealm.VersionID getVersionID() {
        long[] versionId = nativeGetVersionID(nativePtr);
        return new OsSharedRealm.VersionID(versionId[0], versionId[1]);
    }

    @ObjectServer
    public int getPrivileges() {
        return nativeGetRealmPrivileges(nativePtr);
    }

    @ObjectServer
    public int getClassPrivileges(String className) {
        return nativeGetClassPrivileges(nativePtr, className);
    }

    @ObjectServer
    public int getObjectPrivileges(UncheckedRow row) {
        return nativeGetObjectPrivileges(nativePtr, ((UncheckedRow) row).getNativePtr());
    }

    public boolean isClosed() {
        return nativeIsClosed(nativePtr);
    }

    public void writeCopy(File file, @Nullable byte[] key) {
        if (file.isFile() && file.exists()) {
            throw new IllegalArgumentException("The destination file must not exist");
        }
        nativeWriteCopy(nativePtr, file.getAbsolutePath(), key);
    }

    public boolean waitForChange() {
        return nativeWaitForChange(nativePtr);
    }

    public void stopWaitForChange() {
        nativeStopWaitForChange(nativePtr);
    }

    public boolean compact() {
        return nativeCompact(nativePtr);
    }

    public void setAutoRefresh(boolean enabled) {
        capabilities.checkCanDeliverNotification(null);
        nativeSetAutoRefresh(nativePtr, enabled);
    }

    public boolean isAutoRefresh() {
        return nativeIsAutoRefresh(nativePtr);
    }

    public RealmConfiguration getConfiguration() {
        return osRealmConfig.getRealmConfiguration();
    }

    @Override
    public void close() {
        if (realmNotifier != null) {
            realmNotifier.close();
        }
        synchronized (context) {
            nativeCloseSharedRealm(nativePtr);
            // Don't reset the nativePtr since we still rely on Object Store to check if the given OsSharedRealm ptr
            // is closed or not.
        }
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    /**
     * @return the {@link OsSchemaInfo} of this {@code OsSharedRealm}.
     */
    public OsSchemaInfo getSchemaInfo() {
        return schemaInfo;
    }

    /**
     * Registers a {@link SchemaChangedCallback} with JNI {@code BindingContext}.
     *
     * @param callback to be registered. It will be held as a weak ref in the JNI. The caller needs to hold a strong ref
     *                 to the callback to ensure it won't be GCed before calling.
     */
    public void registerSchemaChangedCallback(SchemaChangedCallback callback) {
        nativeRegisterSchemaChangedCallback(nativePtr, callback);
    }

    /**
     * Returns {@code true} if this Realm is a query-based synchronized Realm.
     */
    public boolean isPartial() {
        return nativeIsPartial(nativePtr);
    }

    /**
     * Returns {@code true} if this Realm is a synchronized Realm, either query-based or fully
     * synchronized.
     */
    public boolean isSyncRealm() {
        return osRealmConfig.getResolvedRealmURI() != null;
    }

    // addIterator(), detachIterators() and invalidateIterators() are used to make RealmResults stable iterators work.
    // The iterator will iterate on a snapshot Results if it is accessed inside a transaction.
    // See https://github.com/realm/realm-java/issues/3883 for more information.
    // Should only be called by Iterator's constructor.
    void addIterator(OsResults.Iterator iterator) {
        iterators.add(new WeakReference<>(iterator));
    }

    // The detaching should happen before transaction begins.
    private void detachIterators() {
        for (WeakReference<OsResults.Iterator> iteratorRef : iterators) {
            OsResults.Iterator iterator = iteratorRef.get();
            if (iterator != null) {
                iterator.detach();
            }
        }
        iterators.clear();
    }

    // Invalidates all iterators when a remote change notification is received.
    void invalidateIterators() {
        for (WeakReference<OsResults.Iterator> iteratorRef : iterators) {
            OsResults.Iterator iterator = iteratorRef.get();
            if (iterator != null) {
                iterator.invalidate();
            }
        }
        iterators.clear();
    }

    // addPendingRow, removePendingRow and executePendingRow queries are to solve that the listener cannot be added
    // inside a transaction. For the findFirstAsync(), listener is registered on an Object Store Results first, then move
    // the listeners to the Object when the query for Results returns. When beginTransaction() called, all listeners'
    // on the results will be triggered first, that leads to the registration of listeners on the Object which will
    // throw because of the transaction has already begun. So here we execute all PendingRow queries first before
    // calling the Object Store begin_transaction to avoid the problem.
    // Add pending row to the list when it is created. It should be called in the PendingRow constructor.
    void addPendingRow(PendingRow pendingRow) {
        pendingRows.add(new WeakReference<PendingRow>(pendingRow));
    }

    // Remove pending row from the list. It should be called when pending row's query finished.
    void removePendingRow(PendingRow pendingRow) {
        for (WeakReference<PendingRow> ref : pendingRows) {
            PendingRow row = ref.get();
            if (row == null || row == pendingRow) {
                pendingRows.remove(ref);
            }
        }
    }

    // Execute all pending row queries.
    private void executePendingRowQueries() {
        for (WeakReference<PendingRow> ref : pendingRows) {
            PendingRow row = ref.get();
            if (row != null) {
                row.executeQuery();
            }
        }
        pendingRows.clear();
    }

    /**
     * Called from JNI when the expected schema doesn't match the existing one.
     *
     * @param callback   the {@link MigrationCallback} in the {@link RealmConfiguration}.
     * @param oldVersion the schema version of the existing Realm file.
     */
    @SuppressWarnings("unused")
    private static void runMigrationCallback(long nativeSharedRealmPtr, OsRealmConfig osRealmConfig, MigrationCallback callback,
                                             long oldVersion) {
        callback.onMigrationNeeded(new OsSharedRealm(nativeSharedRealmPtr, osRealmConfig), oldVersion,
                osRealmConfig.getRealmConfiguration().getSchemaVersion());
    }

    /**
     * Called from JNI when the schema is created the first time.
     *
     * @param callback to be executed with a given in-transact {@link OsSharedRealm}.
     */
    @SuppressWarnings("unused")
    private static void runInitializationCallback(long nativeSharedRealmPtr, OsRealmConfig osRealmConfig, InitializationCallback callback) {
        callback.onInit(new OsSharedRealm(nativeSharedRealmPtr, osRealmConfig));
    }

    private static native void nativeInit(String temporaryDirectoryPath);

    private static native long nativeGetSharedRealm(long nativeConfigPtr, RealmNotifier notifier);

    private static native void nativeCloseSharedRealm(long nativeSharedRealmPtr);

    private static native boolean nativeIsClosed(long nativeSharedRealmPtr);

    private static native void nativeBeginTransaction(long nativeSharedRealmPtr);

    private static native void nativeCommitTransaction(long nativeSharedRealmPtr);

    private static native void nativeCancelTransaction(long nativeSharedRealmPtr);

    private static native boolean nativeIsInTransaction(long nativeSharedRealmPtr);

    private static native boolean nativeIsEmpty(long nativeSharedRealmPtr);

    private static native void nativeRefresh(long nativeSharedRealmPtr);

    private static native long[] nativeGetVersionID(long nativeSharedRealmPtr);

    // Throw IAE if the table doesn't exist.
    private static native long nativeGetTable(long nativeSharedRealmPtr, String tableName);

    // Throw IAE if the table exists already.
    private static native long nativeCreateTable(long nativeSharedRealmPtr, String tableName);

    // Throw IAE if the table exists already.
    // If isStringType is false, the PK field will be created as an integer PK field.
    private static native long nativeCreateTableWithPrimaryKeyField(long nativeSharedRealmPtr, String tableName,
                                                                    String primaryKeyFieldName,
                                                                    boolean isStringType, boolean isNullable);

    private static native String nativeGetTableName(long nativeSharedRealmPtr, int index);

    private static native boolean nativeHasTable(long nativeSharedRealmPtr, String tableName);

    private static native void nativeRenameTable(long nativeSharedRealmPtr, String oldTableName, String newTableName);

    private static native long nativeSize(long nativeSharedRealmPtr);

    private static native void nativeWriteCopy(long nativeSharedRealmPtr, String path, @Nullable byte[] key);

    private static native boolean nativeWaitForChange(long nativeSharedRealmPtr);

    private static native void nativeStopWaitForChange(long nativeSharedRealmPtr);

    private static native boolean nativeCompact(long nativeSharedRealmPtr);

    private static native void nativeSetAutoRefresh(long nativePtr, boolean enabled);

    private static native boolean nativeIsAutoRefresh(long nativePtr);

    private static native long nativeGetFinalizerPtr();

    // Return the pointer to the Realm::m_schema.
    private static native long nativeGetSchemaInfo(long nativePtr);

    private static native void nativeRegisterSchemaChangedCallback(long nativePtr, SchemaChangedCallback callback);

    private static native int nativeGetRealmPrivileges(long nativePtr);

    private static native int nativeGetClassPrivileges(long nativePtr, String className);

    private static native int nativeGetObjectPrivileges(long nativePtr, long rowNativePtr);
    private static native boolean nativeIsPartial(long nativePtr);

}
