package io.realm.internal;

import java.io.*;

import io.realm.RealmConfiguration;
import io.realm.internal.async.BadVersionException;

public final class SharedRealm implements Closeable {

    public enum Durability {
        FULL(0),
        MEM_ONLY(1);

        final int value;

        Durability(int value) {
            this.value = value;
        }
    }

    public static class VersionID implements Comparable<VersionID> {
        final long version;
        final long index;

        VersionID(long version, long index) {
            this.version = version;
            this.index = index;
        }

        @Override
        public int compareTo(VersionID another) {
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
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            if (!super.equals(object)) return false;

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

    private long nativePtr;
    private RealmConfiguration configuration;
    final Context context;

    private SharedRealm(long nativePtr, RealmConfiguration configuration) {
        this.nativePtr = nativePtr;
        this.configuration = configuration;
        context = new Context();
    }

    public static SharedRealm getInstance(RealmConfiguration config) {
        long nativeConfigPtr = nativeCreateConfig(
                config.getPath(),
                config.getEncryptionKey(),
                false,
                config.getDurability() == Durability.MEM_ONLY,
                false,
                false,
                false);
        SharedRealm sharedRealm = new SharedRealm(nativeGetSharedRealm(nativeConfigPtr), config);
        nativeCloseConfig(nativeConfigPtr);
        return sharedRealm;
    }

    public void beginTransaction() {
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

    public long getSchemaVersion() {
        return nativeGetVersion(nativePtr);
    }

    // FIXME: This should be removed, or at least should be private
    public long getGroupNative() {
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

    public long getSharedGroupNative() {
        return nativeGetSharedGroup(nativePtr);
    }

    public void refresh() {
        nativeRefresh(nativePtr);
    }

    public void refresh(SharedRealm.VersionID version) throws BadVersionException {
        nativeRefresh(nativePtr, version.version, version.index);
    }

    public SharedRealm.VersionID getVersionID() {
        long[] versionId = nativeGetVersionID (nativePtr);
        return new SharedRealm.VersionID(versionId[0], versionId[1]);
    }

    public boolean isClosed() {
        return nativePtr == 0 || nativeIsClosed(nativePtr);
    }

    public void writeCopy(File file, byte[] key) {
        if (file.isFile() && file.exists()) {
            throw new IllegalArgumentException("The destination file must not exist");
        }
        if (key != null && key.length != 64) {
            throw new IllegalArgumentException("Realm AES keys must be 64 bytes long");
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

    @Override
    public void close() {
        if (nativePtr != 0) {
            nativeCloseSharedRealm(nativePtr);
            nativePtr = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        synchronized (context) {
            if (nativePtr != 0) {
                context.asyncDisposeSharedRealm(nativePtr);
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
            }
        }
    }

    private static native long nativeCreateConfig(String realmPath, byte[] key, boolean readonly, boolean inMemory,
                                                  boolean cache, boolean disableFormatUpgrade, boolean autoChangeNotification);
    private static native void nativeCloseConfig(long nativeConfigPtr);
    private static native long nativeGetSharedRealm(long nativeConfigPtr);
    // FIXME: Should be private
    static native void nativeCloseSharedRealm(long nativeSharedRealmPtr);
    private static native boolean nativeIsClosed(long nativeSharedRealmPtr);
    private static native void nativeBeginTransaction(long nativeSharedRealmPtr);
    private static native void nativeCommitTransaction(long nativeSharedRealmPtr);
    private static native void nativeCancelTransaction(long nativeSharedRealmPtr);
    private static native boolean nativeIsInTransaction(long nativeSharedRealmPtr);
    private static native long nativeGetVersion(long nativeSharedRealmPtr);
    static native long nativeReadGroup(long nativeSharedRealmPtr);
    private static native boolean nativeIsEmpty(long nativeSharedRealmPtr);
    private static native void nativeRefresh(long nativeSharedRealmPtr);
    private static native void nativeRefresh(long nativeSharedRealmPtr, long version, long index);

    // FIXME: Below APIs may not be needed
    // Implement the relevant API in a better way without exposing the pointers.
    private static native long nativeGetSharedGroup(long nativeSharedRealmPtr);
    private static native long[]  nativeGetVersionID(long nativeSharedRealmPtr);

    private static native long nativeGetTable(long nativeSharedRealmPtr, String tableName);
    private static native String nativeGetTableName(long nativeSharedRealmPtr, int index);
    private static native boolean nativeHasTable(long nativeSharedRealmPtr, String tableName);
    private static native void nativeRenameTable(long nativeSharedRealmPtr, String oldTableName, String newTableName);
    private static native void nativeRemoveTable(long nativeSharedRealmPtr, String tableName);
    private static native long nativeSize(long nativeSharedRealmPtr);
    private static native long nativeWriteCopy(long nativeSharedRealmPtr, String path, byte[] key);
    private static native boolean nativeWaitForChange(long nativeSharedRealmPtr);
    private static native boolean nativeStopWaitForChange(long nativeSharedRealmPtr);
    private static native boolean nativeCompact(long nativeSharedRealmPtr);
}
