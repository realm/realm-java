package com.tightdb;

import com.tightdb.internal.CloseMutex;
import com.tightdb.typed.TightDB;

public class SharedGroup {

    static {
        TightDB.loadLibrary();
    }

    public SharedGroup(String databaseFile) {
        this.nativePtr = createNative(databaseFile, false);
        checkNativePtr();
    }

    public WriteTransaction beginWrite() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't beginWrite() during another active transaction");
        // FIXME: throw from nativeMethod in case of error
        WriteTransaction t = new WriteTransaction(this,
                nativeBeginWrite(nativePtr));
        activeTransaction = true;
        return t;
    }

    long beginReadGroup() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't beginReadGroup() during another active transaction");
        activeTransaction = true;
        return nativeBeginRead(nativePtr);
    }

    public ReadTransaction beginRead() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't beginRead() during another active transaction");
        // FIXME: throw from nativeMethod in case of error
        ReadTransaction t = new ReadTransaction(this, nativeBeginRead(nativePtr));
        activeTransaction = true;
        return t;
    }

    void endRead() {
        nativeEndRead(nativePtr);
        activeTransaction = false;
    }

    public void close() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't close() SharedGroup during an active transaction");

        doClose();
    }

    SharedGroup(String databaseFile, boolean enableReplication) {
        this.nativePtr = createNative(databaseFile, enableReplication);
        checkNativePtr();
    }

    void commit() {
        nativeCommit(nativePtr);
        activeTransaction = false;
    }

    void rollback() {
        nativeRollback(nativePtr);
        activeTransaction = false;
    }

    private void doClose() {
        synchronized (CloseMutex.getInstance()) {
            if (nativePtr == 0)
                return;
            nativeClose(nativePtr);
            nativePtr = 0;
        }
    }

    static native String nativeGetDefaultReplicationDatabaseFileName();

    protected void finalize() {
        doClose();
    }

    public boolean hasChanged() {
        return nativeHasChanged(nativePtr);
    }

    protected native boolean nativeHasChanged(long nativePtr);

    private long nativePtr;
    private boolean activeTransaction;

    private native long nativeBeginRead(long nativePtr);

    private native void nativeEndRead(long nativePtr);

    private native long nativeBeginWrite(long nativePtr);

    private native void nativeCommit(long nativePtr);

    private native void nativeRollback(long nativePtr);

    private native long createNative(String databaseFile,
            boolean enableReplication);

    private void checkNativePtr() {
        if (this.nativePtr == 0)
            throw new OutOfMemoryError("Out of native memory.");
    }

    private native void nativeClose(long nativePtr);
}
