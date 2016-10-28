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

import io.realm.RealmConfiguration;
import io.realm.RealmSchema;
import io.realm.internal.async.BadVersionException;

public final class SharedRealm implements Closeable {

    // Const value for RealmFileException conversion
    public static final byte FILE_EXCEPTION_KIND_ACCESS_ERROR = 0;
    public static final byte FILE_EXCEPTION_KIND_PERMISSION_DENIED = 1;
    public static final byte FILE_EXCEPTION_KIND_EXISTS = 2;
    public static final byte FILE_EXCEPTION_KIND_NOT_FOUND = 3;
    public static final byte FILE_EXCEPTION_KIND_INCOMPATIBLE_LOCK_FILE = 4;
    public static final byte FILE_EXCEPTION_KIND_FORMAT_UPGRADE_REQUIRED = 5;

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

    private volatile static File temporaryDirectory;

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
            this .value = value;
        }

        public byte getNativeValue() {
            return value;
        }
    }

    // JNI will only hold a weak global ref to this.
    public final RealmNotifier realmNotifier;
    public final ObjectServerFacade objectServerFacade;

    public static class VersionID implements Comparable<VersionID> {
        public final long version;
        public final long index;

        VersionID(long version, long index) {
            this.version = version;
            this.index = index;
        }

        @Override
        public int compareTo(@SuppressWarnings("NullableProblems") VersionID another) {
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

    private long nativePtr;
    private RealmConfiguration configuration;
    final Context context;
    private long lastSchemaVersion;
    private final SchemaVersionListener schemaChangeListener;

    private SharedRealm(long nativePtr, RealmConfiguration configuration, RealmNotifier notifier,
                        SchemaVersionListener schemaVersionListener) {
        this.nativePtr = nativePtr;
        this.configuration = configuration;
        this.realmNotifier = notifier;
        this.schemaChangeListener = schemaVersionListener;
        context = new Context();
        this.lastSchemaVersion = schemaVersionListener == null ? -1L : getSchemaVersion();
        objectServerFacade = null;
    }

    public static SharedRealm getInstance(RealmConfiguration config) {
        return getInstance(config, null, null);
    }

    public static SharedRealm getInstance(RealmConfiguration config, RealmNotifier realmNotifier,
                                          SchemaVersionListener schemaVersionListener) {
        String[] userAndServer = ObjectServerFacade.getSyncFacadeIfPossible().getUserAndServerUrl(config);
        String rosServerUrl = userAndServer[0];
        String rosUserToken = userAndServer[1];
        boolean enable_caching = false; // Handled in Java currently
        boolean disableFormatUpgrade = false; // TODO Double negatives :/
        boolean autoChangeNotifications = true;
        long nativeConfigPtr = nativeCreateConfig(
                config.getPath(),
                config.getEncryptionKey(),
                rosServerUrl != null ? SchemaMode.SCHEMA_MODE_ADDITIVE.getNativeValue() : SchemaMode.SCHEMA_MODE_MANUAL.getNativeValue(),
                config.getDurability() == Durability.MEM_ONLY,
                enable_caching,
                disableFormatUpgrade,
                autoChangeNotifications,
                rosServerUrl,
                rosUserToken);
        try {
            return new SharedRealm(
                    nativeGetSharedRealm(nativeConfigPtr, realmNotifier),
                    config,
                    realmNotifier,
                    schemaVersionListener);
        } finally {
            nativeCloseConfig(nativeConfigPtr);
        }
    }

    long getNativePtr() {
        return nativePtr;
    }

    public void beginTransaction() {
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

    public Table getTable(String name) {
        return new Table(this, nativeGetTable(nativePtr, name));
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

    public void refresh(SharedRealm.VersionID version) throws BadVersionException {
        // FIXME: This will have a different behaviour compared to refresh to the latest version.
        // In the JNI this will just advance read the corresponding SharedGroup to the specific version without notifier
        // or transact log observer involved. Before we use notification & fine grained notification from OS, it is not
        // a problem.
        nativeRefresh(nativePtr, version.version, version.index);
        invokeSchemaChangeListenerIfSchemaChanged();
    }

    public SharedRealm.VersionID getVersionID() {
        long[] versionId = nativeGetVersionID (nativePtr);
        return new SharedRealm.VersionID(versionId[0], versionId[1]);
    }

    public long getLastSnapshotVersion() {
        return nativeGetSnapshotVersion(nativePtr);
    }

    public boolean isClosed() {
        return nativePtr == 0 || nativeIsClosed(nativePtr);
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

    public void updateSchema(RealmSchema schema, long version) {
        nativeUpdateSchema(nativePtr, schema.getNativePtr(), version);
    }

    @Override
    public void close() {
        if (realmNotifier != null) {
            realmNotifier.close();
        }
        synchronized (context) {
            if (nativePtr != 0) {
                nativeCloseSharedRealm(nativePtr);
                nativePtr = 0;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        synchronized (context) {
            close();
            // FIXME: Below is the original implementation of SharedGroup.finalize().
            // And actually Context.asyncDisposeSharedGroup will simply call nativeClose which is not asyc at all.
            // IMO since this implemented Closeable already, it makes no sense to implement finalize.
            // Just keep the logic the same for now and make nativeClose private. Rethink about this when cleaning
            // up finalizers.
            //context.asyncDisposeSharedRealm(nativePtr);
        }
        super.finalize();
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

    private static native void nativeInit(String temporaryDirectoryPath);
    private static native long nativeCreateConfig(String realmPath, byte[] key, byte schemaMode, boolean inMemory,
                                                  boolean cache, boolean disableFormatUpgrade,
                                                  boolean autoChangeNotification,
                                                  String syncServerURL, String syncUserToken);
    private static native void nativeCloseConfig(long nativeConfigPtr);
    private static native long nativeGetSharedRealm(long nativeConfigPtr, RealmNotifier notifier);
    private static native void nativeCloseSharedRealm(long nativeSharedRealmPtr);
    private static native boolean nativeIsClosed(long nativeSharedRealmPtr);
    private static native void nativeBeginTransaction(long nativeSharedRealmPtr);
    private static native void nativeCommitTransaction(long nativeSharedRealmPtr);
    private static native void nativeCancelTransaction(long nativeSharedRealmPtr);
    private static native boolean nativeIsInTransaction(long nativeSharedRealmPtr);
    private static native long nativeGetVersion(long nativeSharedRealmPtr);
    private static native long nativeGetSnapshotVersion(long nativeSharedRealmPtr);
    private static native void nativeSetVersion(long nativeSharedRealmPtr, long version);
    private static native long nativeReadGroup(long nativeSharedRealmPtr);
    private static native boolean nativeIsEmpty(long nativeSharedRealmPtr);
    private static native void nativeRefresh(long nativeSharedRealmPtr);
    private static native void nativeRefresh(long nativeSharedRealmPtr, long version, long index);
    private static native long[]  nativeGetVersionID(long nativeSharedRealmPtr);
    private static native long nativeGetTable(long nativeSharedRealmPtr, String tableName);
    private static native String nativeGetTableName(long nativeSharedRealmPtr, int index);
    private static native boolean nativeHasTable(long nativeSharedRealmPtr, String tableName);
    private static native void nativeRenameTable(long nativeSharedRealmPtr, String oldTableName, String newTableName);
    private static native void nativeRemoveTable(long nativeSharedRealmPtr, String tableName);
    private static native long nativeSize(long nativeSharedRealmPtr);
    private static native void nativeWriteCopy(long nativeSharedRealmPtr, String path, byte[] key);
    private static native boolean nativeWaitForChange(long nativeSharedRealmPtr);
    private static native void nativeStopWaitForChange(long nativeSharedRealmPtr);
    private static native boolean nativeCompact(long nativeSharedRealmPtr);
    private static native void nativeUpdateSchema(long nativePtr, long nativeSchemaPtr, long version);
}
