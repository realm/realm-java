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

import io.realm.RealmConfiguration;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;


public final class SharedRealm implements Closeable, NativeObject {

    // Const value for RealmFileException conversion
    public static final byte FILE_EXCEPTION_KIND_ACCESS_ERROR = 0;
    public static final byte FILE_EXCEPTION_KIND_BAD_HISTORY = 1;
    public static final byte FILE_EXCEPTION_KIND_PERMISSION_DENIED = 2;
    public static final byte FILE_EXCEPTION_KIND_EXISTS = 3;
    public static final byte FILE_EXCEPTION_KIND_NOT_FOUND = 4;
    public static final byte FILE_EXCEPTION_KIND_INCOMPATIBLE_LOCK_FILE = 5;
    public static final byte FILE_EXCEPTION_KIND_FORMAT_UPGRADE_REQUIRED = 6;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    public static void initialize(File tempDirectory) {
        if (SharedRealm.temporaryDirectory != null) {
            // already initialized
            return;
        }
        if (tempDirectory == null) {
            throw new IllegalArgumentException("'tempDirectory' must not be null.");
        }

        String temporaryDirectoryPath = tempDirectory.getAbsolutePath();
        if (!tempDirectory.isDirectory() && !tempDirectory.mkdirs() && !tempDirectory.isDirectory()) {
            throw new IOException("failed to create temporary directory: " + temporaryDirectoryPath);
        }

        if (!temporaryDirectoryPath.endsWith("/")) {
            temporaryDirectoryPath += "/";
        }
        nativeInit(temporaryDirectoryPath);
        SharedRealm.temporaryDirectory = tempDirectory;
    }

    public static File getTemporaryDirectory() {
        return temporaryDirectory;
    }

    private static volatile File temporaryDirectory;

    public enum Durability {
        FULL(0),
        MEM_ONLY(1);

        final int value;

        Durability(int value) {
            this.value = value;
        }
    }

    // Public for static checking in JNI
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_AUTOMATIC = 0;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_READONLY = 1;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_RESET_FILE = 2;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_ADDITIVE = 3;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_MANUAL = 4;

    @SuppressWarnings("WeakerAccess")
    public enum SchemaMode {
        SCHEMA_MODE_AUTOMATIC(SCHEMA_MODE_VALUE_AUTOMATIC),
        SCHEMA_MODE_READONLY(SCHEMA_MODE_VALUE_READONLY),
        SCHEMA_MODE_RESET_FILE(SCHEMA_MODE_VALUE_RESET_FILE),
        SCHEMA_MODE_ADDITIVE(SCHEMA_MODE_VALUE_ADDITIVE),
        SCHEMA_MODE_MANUAL(SCHEMA_MODE_VALUE_MANUAL);

        final byte value;

        SchemaMode(byte value) {
            this.value = value;
        }

        public byte getNativeValue() {
            return value;
        }
    }

    private final List<WeakReference<PendingRow>> pendingRows = new CopyOnWriteArrayList<>();
    public final List<WeakReference<Collection>> collections = new CopyOnWriteArrayList<>();
    public final List<WeakReference<Collection.Iterator>> iterators = new ArrayList<>();

    // JNI will only hold a weak global ref to this.
    public final RealmNotifier realmNotifier;
    public final Capabilities capabilities;

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

    public interface SchemaVersionListener {
        void onSchemaVersionChanged(long currentVersion);
    }

    @KeepMember
    public abstract class MigrationCallback {
        abstract void migrate(SharedRealm oldRealm, SharedRealm realm);

        @KeepMember
        private void doMigrate(long oldRealmPtr, long realmPtr) {
            migrate(new SharedRealm(oldRealmPtr, SharedRealm.this.configuration),
                    new SharedRealm(realmPtr, SharedRealm.this.configuration));
        }
    }

    private final SchemaVersionListener schemaChangeListener;
    private final RealmConfiguration configuration;
    private final long nativePtr;

    private long lastSchemaVersion;

    final NativeContext context;

    private SharedRealm(long nativeConfigPtr,
            RealmConfiguration configuration,
            SchemaVersionListener schemaVersionListener) {
        Capabilities capabilities = new AndroidCapabilities();
        RealmNotifier realmNotifier = new AndroidRealmNotifier(this, capabilities);

        this.nativePtr = nativeGetSharedRealm(nativeConfigPtr, realmNotifier);
        this.configuration = configuration;

        this.capabilities = capabilities;
        this.realmNotifier = realmNotifier;
        this.schemaChangeListener = schemaVersionListener;
        context = new NativeContext();
        context.addReference(this);
        this.lastSchemaVersion = schemaVersionListener == null ? -1L : getSchemaVersion();
        nativeSetAutoRefresh(nativePtr, capabilities.canDeliverNotification());
    }

    private SharedRealm(long nativePtr,
            RealmConfiguration configuration) {
        Capabilities capabilities = new AndroidCapabilities();
        RealmNotifier realmNotifier = new AndroidRealmNotifier(this, capabilities);

        this.nativePtr = nativePtr;
        this.configuration = configuration;

        this.capabilities = capabilities;
        this.realmNotifier = realmNotifier;
        this.schemaChangeListener = null;
        context = new NativeContext();
        context.addReference(this);
        this.lastSchemaVersion = -1L;
        nativeSetAutoRefresh(nativePtr, capabilities.canDeliverNotification());
    }

    // This will create a SharedRealm where autoChangeNotifications is false,
    // If autoChangeNotifications is true, an additional SharedGroup might be created in the OS's external commit helper.
    // That is not needed for some cases: eg.: An extra opened SharedGroup will cause a compact failure.
    public static SharedRealm getInstance(RealmConfiguration config) {
        return getInstance(config, null, false);
    }


    public static SharedRealm getInstance(RealmConfiguration config, SchemaVersionListener schemaVersionListener,
            boolean autoChangeNotifications) {
        Object[] syncUserConf = ObjectServerFacade.getSyncFacadeIfPossible().getUserAndServerUrl(config);
        String syncUserIdentifier = (String) syncUserConf[0];
        String syncRealmUrl = (String) syncUserConf[1];
        String syncRealmAuthUrl = (String) syncUserConf[2];
        String syncRefreshToken = (String) syncUserConf[3];
        boolean syncClientValidateSsl = (Boolean.TRUE.equals(syncUserConf[4]));
        String syncSslTrustCertificatePath = (String) syncUserConf[5];

        final boolean enableCaching = false; // Handled in Java currently
        final boolean enableFormatUpgrade = true;

        long nativeConfigPtr = nativeCreateConfig(
                config.getPath(),
                config.getEncryptionKey(),
                syncRealmUrl != null ? SchemaMode.SCHEMA_MODE_ADDITIVE.getNativeValue() : SchemaMode.SCHEMA_MODE_MANUAL.getNativeValue(),
                config.getDurability() == Durability.MEM_ONLY,
                enableCaching,
                config.getSchemaVersion(),
                enableFormatUpgrade,
                autoChangeNotifications,
                syncRealmUrl,
                syncRealmAuthUrl,
                syncUserIdentifier,
                syncRefreshToken,
                syncClientValidateSsl,
                syncSslTrustCertificatePath);

        try {
            ObjectServerFacade.getSyncFacadeIfPossible().wrapObjectStoreSessionIfRequired(config);

            return new SharedRealm(nativeConfigPtr, config, schemaVersionListener);
        } finally {
            nativeCloseConfig(nativeConfigPtr);
        }
    }

    public void beginTransaction() {
        beginTransaction(false);
    }

    public void beginTransaction(boolean ignoreReadOnly) {
        // TODO ReadOnly is also supported by the Object Store Schema, but until we support that we need to enforce it
        // ourselves.
        if (!ignoreReadOnly && configuration.isReadOnly()) {
            throw new IllegalStateException("Write transactions cannot be used when a Realm is marked as read-only.");
        }
        detachIterators();
        executePendingRowQueries();
        nativeBeginTransaction(nativePtr);
        invokeSchemaChangeListenerIfSchemaChanged();
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

    public void setSchemaVersion(long schemaVersion) {
        nativeSetVersion(nativePtr, schemaVersion);
    }

    public long getSchemaVersion() {
        return nativeGetVersion(nativePtr);
    }

    // FIXME: This should be removed, migratePrimaryKeyTableIfNeeded is using it which should be in Object Store instead?
    long getGroupNative() {
        return nativeReadGroup(nativePtr);
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

    public void renameTable(String oldName, String newName) {
        nativeRenameTable(nativePtr, oldName, newName);
    }

    public void removeTable(String name) {
        nativeRemoveTable(nativePtr, name);
    }

    public String getTableName(int index) {
        return nativeGetTableName(nativePtr, index);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public String getPath() {
        return configuration.getPath();
    }

    public boolean isEmpty() {
        return nativeIsEmpty(nativePtr);
    }

    public void refresh() {
        nativeRefresh(nativePtr);
        invokeSchemaChangeListenerIfSchemaChanged();
    }

    public SharedRealm.VersionID getVersionID() {
        long[] versionId = nativeGetVersionID(nativePtr);
        return new SharedRealm.VersionID(versionId[0], versionId[1]);
    }

    public boolean isClosed() {
        return nativeIsClosed(nativePtr);
    }

    public void writeCopy(File file, byte[] key) {
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

    /**
     * Updates the underlying schema based on the schema description.
     * If migration is required, the migration object is used.
     */
    public void updateSchema(OsSchemaInfo schemaInfo, long version, MigrationCallback migrationCallback) {
        nativeUpdateSchema(nativePtr, schemaInfo.getNativePtr(), version, migrationCallback);
    }

    /**
     * Updates the underlying schema based on the schema description.
     * Calling this method must be done from inside a write transaction.
     * <p>
     * TODO: This method should not require the caller to get the native pointer.
     * Instead, the signature should be something like:
     * public <T extends RealmSchema & NativeObject> </T>void updateSchema(T schema, long version)
     * ... that is: something that is a schema and that wraps a native object.
     *
     * @param schemaNativePtr the pointer to a native schema object.
     * @param version the target version.
     */
    public void updateSchema(OsSchemaInfo schemaInfo, long version) {
        nativeUpdateSchema(nativePtr, schemaInfo.getNativePtr(), version, null);
    }

    public void setAutoRefresh(boolean enabled) {
        capabilities.checkCanDeliverNotification(null);
        nativeSetAutoRefresh(nativePtr, enabled);
    }

    public boolean isAutoRefresh() {
        return nativeIsAutoRefresh(nativePtr);
    }

    /**
     * Determine whether the passed schema needs to be updated.
     * <p>
     * TODO: This method should not require the caller to get the native pointer.
     * Instead, the signature should be something like:
     * public <T extends RealmSchema & NativeObject> </T>void updateSchema(T schema, long version)
     * ... that is, something that is a schema and that wraps a native object.
     *
     * @param schemaNativePtr the pointer to a native schema object.
     * @return true if it will be necessary to call {@code updateSchema}
     */
    public boolean requiresMigration(long schemaNativePtr) {
        return nativeRequiresMigration(nativePtr, schemaNativePtr);
    }

    @Override
    public void close() {
        if (realmNotifier != null) {
            realmNotifier.close();
        }
        synchronized (context) {
            nativeCloseSharedRealm(nativePtr);
            // Don't reset the nativePtr since we still rely on Object Store to check if the given SharedRealm ptr
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

    public void invokeSchemaChangeListenerIfSchemaChanged() {
        if (schemaChangeListener == null) {
            return;
        }

        final long before = lastSchemaVersion;
        final long current = getSchemaVersion();
        if (current != before) {
            lastSchemaVersion = current;
            schemaChangeListener.onSchemaVersionChanged(current);
        }
    }

    // addIterator(), detachIterators() and invalidateIterators() are used to make RealmResults stable iterators work.
    // The iterator will iterate on a snapshot Results if it is accessed inside a transaction.
    // See https://github.com/realm/realm-java/issues/3883 for more information.
    // Should only be called by Iterator's constructor.
    void addIterator(Collection.Iterator iterator) {
        iterators.add(new WeakReference<>(iterator));
    }

    // The detaching should happen before transaction begins.
    void detachIterators() {
        for (WeakReference<Collection.Iterator> iteratorRef : iterators) {
            Collection.Iterator iterator = iteratorRef.get();
            if (iterator != null) {
                iterator.detach();
            }
        }
        iterators.clear();
    }

    // Invalidates all iterators when a remote change notification is received.
    void invalidateIterators() {
        for (WeakReference<Collection.Iterator> iteratorRef : iterators) {
            Collection.Iterator iterator = iteratorRef.get();
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

    private static native void nativeInit(String temporaryDirectoryPath);

    // Keep last session as an 'object' to avoid any reference to sync code
    private static native long nativeCreateConfig(String realmPath, byte[] key, byte schemaMode, boolean inMemory,
            boolean cache,
            long schemaVersion,
            boolean enabledFormatUpgrade,
            boolean autoChangeNotification,
            String syncServerURL,
            String syncServerAuthURL,
            String syncUserIdentity,
            String syncRefreshToken,
            boolean syncClientValidateSsl,
            String syncSslTrustCertificatePath);

    private static native void nativeCloseConfig(long nativeConfigPtr);

    private static native long nativeGetSharedRealm(long nativeConfigPtr, RealmNotifier notifier);

    private static native void nativeCloseSharedRealm(long nativeSharedRealmPtr);

    private static native boolean nativeIsClosed(long nativeSharedRealmPtr);

    private static native void nativeBeginTransaction(long nativeSharedRealmPtr);

    private static native void nativeCommitTransaction(long nativeSharedRealmPtr);

    private static native void nativeCancelTransaction(long nativeSharedRealmPtr);

    private static native boolean nativeIsInTransaction(long nativeSharedRealmPtr);

    private static native long nativeGetVersion(long nativeSharedRealmPtr);

    private static native void nativeSetVersion(long nativeSharedRealmPtr, long version);

    private static native long nativeReadGroup(long nativeSharedRealmPtr);

    private static native boolean nativeIsEmpty(long nativeSharedRealmPtr);

    private static native void nativeRefresh(long nativeSharedRealmPtr);

    private static native long[] nativeGetVersionID(long nativeSharedRealmPtr);

    // Throw IAE if the table doesn't exist.
    private static native long nativeGetTable(long nativeSharedRealmPtr, String tableName);

    // Throw IAE if the table exists already.
    private static native long nativeCreateTable(long nativeSharedRealmPtr, String tableName);

    private static native String nativeGetTableName(long nativeSharedRealmPtr, int index);

    private static native boolean nativeHasTable(long nativeSharedRealmPtr, String tableName);

    private static native void nativeRenameTable(long nativeSharedRealmPtr, String oldTableName, String newTableName);

    private static native void nativeRemoveTable(long nativeSharedRealmPtr, String tableName);

    private static native long nativeSize(long nativeSharedRealmPtr);

    private static native void nativeWriteCopy(long nativeSharedRealmPtr, String path, byte[] key);

    private static native boolean nativeWaitForChange(long nativeSharedRealmPtr);

    private static native void nativeStopWaitForChange(long nativeSharedRealmPtr);

    private static native boolean nativeCompact(long nativeSharedRealmPtr);

    private static native void nativeUpdateSchema(long nativePtr, long nativeSchemaPtr, long version,
            Object migrationCallback);

    private static native void nativeSetAutoRefresh(long nativePtr, boolean enabled);

    private static native boolean nativeIsAutoRefresh(long nativePtr);

    private static native boolean nativeRequiresMigration(long nativePtr, long nativeSchemaPtr);

    private static native long nativeGetFinalizerPtr();
}
