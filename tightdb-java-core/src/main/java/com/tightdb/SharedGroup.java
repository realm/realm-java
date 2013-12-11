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
        context = new Context();
        this.nativePtr = createNative(databaseFile, Durability.FULL.value, false, false);
        checkNativePtr();
    }
    public SharedGroup(String databaseFile, Durability durability) {
        context = new Context();
        this.nativePtr = createNative(databaseFile, durability.value, false, false);
        checkNativePtr();
    }
    public SharedGroup(String databaseFile, Durability durability, boolean fileMustExist) {
        context = new Context();
        this.nativePtr = createNative(databaseFile, durability.value, fileMustExist, false);
        checkNativePtr();
    }
/*
    SharedGroup(String databaseFile, Durability durability, boolean no_create, boolean enableReplication) {
        context = new Context();
        this.nativePtr = createNative(databaseFile, durability.value, no_create, enableReplication);
        checkNativePtr();
    }
*/
    public WriteTransaction beginWrite() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't beginWrite() during another active transaction");
        // FIXME: throw from nativeMethod in case of error
       
        long nativeWritePtr = nativeBeginWrite(nativePtr);
        try {
            // Copy context reference from parent
            WriteTransaction t = new WriteTransaction(context, this, nativeWritePtr);
            activeTransaction = true;
            return t;
        }
        catch (RuntimeException e) {
            Group.nativeClose(nativeWritePtr);
            throw e;
        }
    }

    public ReadTransaction beginRead() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't beginRead() during another active transaction");
        // FIXME: throw from nativeMethod in case of error
        
        long nativeReadPtr = nativeBeginRead(nativePtr);
        try {
            // Copy context reference from parent
            ReadTransaction t = new ReadTransaction(context, this, nativeReadPtr);
            activeTransaction = true;
            return t;
        }
        catch (RuntimeException e) {
            Group.nativeClose(nativeReadPtr);
            throw e;
        }
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
                    "Can't close the SharedGroup during an active transaction");

        if (nativePtr == 0)
            return;
        nativeClose(nativePtr);
        nativePtr = 0;
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


    private boolean isClosed() {
        return nativePtr == 0;
    }

    static native String nativeGetDefaultReplicationDatabaseFileName();

    protected void finalize() {
        if (nativePtr != 0)
            context.asyncDisposeGroup(nativePtr);
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
