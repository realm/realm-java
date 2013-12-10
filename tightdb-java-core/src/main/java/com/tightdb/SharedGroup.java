package com.tightdb;

//import com.tightdb.internal.CloseMutex;
import com.tightdb.typed.TightDB;

public class SharedGroup {

    private long nativePtr;
    private boolean activeTransaction;
    private Context context = null;

    static {
        TightDB.loadLibrary();
    }

    public enum Durability {
        FULL(0),
        MEM_ONLY(1),
        ASYNC(2);
        
        final int value;
        
        private Durability(int value) {
            this.value = value;
        }
    }

    public SharedGroup(String databaseFile) {
        this.nativePtr = createNative(databaseFile, Durability.FULL.value, false, false);
        checkNativePtr();
        context = new Context();
    }
    public SharedGroup(String databaseFile, Durability durability) {
        this.nativePtr = createNative(databaseFile, durability.value, false, false);
        checkNativePtr();
        context = new Context();
    }
    public SharedGroup(String databaseFile, Durability durability, boolean fileMustExist) {
        this.nativePtr = createNative(databaseFile, durability.value, fileMustExist, false);
        checkNativePtr();
        context = new Context();
    }
/*
    SharedGroup(String databaseFile, Durability durability, boolean no_create, boolean enableReplication) {
        this.nativePtr = createNative(databaseFile, durability.value, no_create, enableReplication);
        checkNativePtr();
        context = new Context();
    }
*/
    public WriteTransaction beginWrite() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't beginWrite() during another active transaction");
        // FIXME: throw from nativeMethod in case of error
        WriteTransaction t = new WriteTransaction(context, this, nativeBeginWrite(nativePtr));
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
        long nativeGroupPointer = nativeBeginRead(nativePtr);
        ReadTransaction t = new ReadTransaction(context, this, nativeGroupPointer);
        activeTransaction = true;
        return t;
    }

    void endRead() {
        if (isClosed())
            throw new IllegalStateException(
                    "Can't endRead() on closed group. ReadTransaction is invalid.");
        nativeEndRead(nativePtr);
        activeTransaction = false;
    }

    public void close() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't close() SharedGroup during an active transaction");

        doClose();
    }

    void commit() {
        if (isClosed())
            throw new IllegalStateException(
                    "Can't commit() on closed group. WriteTransaction is invalid.");
        nativeCommit(nativePtr);
        activeTransaction = false;
    }

    void rollback() {
        if (isClosed())
            throw new IllegalStateException(
                    "Can't rollback() on closed group. WriteTransaction is invalid.");
        nativeRollback(nativePtr);
        activeTransaction = false;
    }

    private void doClose() {
       // synchronized (CloseMutex.getInstance()) {
            if (nativePtr == 0)
                return;
            nativeClose(nativePtr);
            nativePtr = 0;
       // }
    }

    private boolean isClosed() {
        return nativePtr == 0;
    }

    static native String nativeGetDefaultReplicationDatabaseFileName();

    protected void finalize() {
        doClose();
    }

    public boolean hasChanged() {
        return nativeHasChanged(nativePtr);
    }

    public void reserve(long bytes) {
        nativeReserve(nativePtr, bytes);
    }

    private native void nativeReserve(long nativePtr, long bytes);

    private native boolean nativeHasChanged(long nativePtr);

    private native long nativeBeginRead(long nativePtr);

    private native void nativeEndRead(long nativePtr);

    private native long nativeBeginWrite(long nativePtr);

    private native void nativeCommit(long nativePtr);

    private native void nativeRollback(long nativePtr);

    private native long createNative(String databaseFile,
            int durabilityValue,
            boolean no_create,
            boolean enableReplication);

    private void checkNativePtr() {
        if (this.nativePtr == 0)
            throw new OutOfMemoryError("Out of native memory.");
    }

    private native void nativeClose(long nativePtr);
}
