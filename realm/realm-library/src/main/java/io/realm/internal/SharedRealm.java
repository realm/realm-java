package io.realm.internal;

import java.io.*;

import io.realm.RealmConfiguration;
import io.realm.internal.async.BadVersionException;

public final class SharedRealm implements Closeable {

    private long nativePtr;
    private RealmConfiguration configuration;
    private Context context;

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
                config.getDurability() == SharedGroup.Durability.MEM_ONLY,
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

    private Group readGroup() {
        return new Group(context, nativeReadGroup(nativePtr));
    }

    public boolean hasTable(String name) {
        return readGroup().hasTable(name);
    }

    public Table getTable(String name) {
        return readGroup().getTable(name);
    }

    public void renameTable(String oldName, String newName) {
        readGroup().renameTable(oldName, newName);
    }

    public void removeTable(String name) {
        readGroup().removeTable(name);
    }

    public String getTableName(int index) {
        return readGroup().getTableName(index);
    }

    public long size() {
        return readGroup().size();
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

    public void refresh(SharedGroup.VersionID version) throws BadVersionException {
        nativeRefresh(nativePtr, version.version, version.index);
    }

    public SharedGroup.VersionID getVersionID() {
        long[] versionId = nativeGetVersionID (nativePtr);
        return new SharedGroup.VersionID(versionId[0], versionId[1]);
    }

    public boolean isClosed() {
        return nativePtr == 0 || nativeIsClosed(nativePtr);
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
                context.asyncDisposeSharedGroup(nativePtr);
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
    private static native long nativeReadGroup(long nativeSharedRealmPtr);
    private static native boolean nativeIsEmpty(long nativeSharedRealmPtr);
    private static native void nativeRefresh(long nativeSharedRealmPtr);
    private static native void nativeRefresh(long nativeSharedRealmPtr, long version, long index);

    // FIXME: Below APIs may not be needed
    // Implement the relevant API in a better way without exposing the pointers.
    private static native long nativeGetSharedGroup(long nativeSharedRealmPtr);
    private static native long[]  nativeGetVersionID(long nativeSharedRealmPtr);
}
