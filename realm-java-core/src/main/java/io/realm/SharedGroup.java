package io.realm;

import io.realm.typed.TightDB;

import java.io.Closeable;

public class SharedGroup implements Closeable {

    private long nativePtr;
    private long nativeReplicationPtr;
    private long nativeTransactLogRegistryPtr;
    private boolean implicistTransactionsEnabled = false;
    private boolean activeTransaction;
    private final Context context;

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
        checkNativePtrNotZero();
    }
    public SharedGroup(String databaseFile, boolean enableImplicitTransactions) {
        if (enableImplicitTransactions) {
            nativeTransactLogRegistryPtr = nativeCreateTransactLogRegistry(databaseFile);
            nativeReplicationPtr = nativeCreateReplication(databaseFile);
            nativePtr = createNativeWithImplicitTransactions(nativeReplicationPtr);
            implicistTransactionsEnabled = true;
        } else {
            nativePtr = createNative(databaseFile, Durability.FULL.value, false, false);
        }
        context = new Context();
        checkNativePtrNotZero();
    }

    private native long createNativeWithImplicitTransactions(long nativeReplicationPtr);

    private native long nativeCreateReplication(String databaseFile);

    private native long nativeCreateTransactLogRegistry(String databaseFile);

    public SharedGroup(String databaseFile, Durability durability) {
        context = new Context();
        this.nativePtr = createNative(databaseFile, durability.value, false, false);
        checkNativePtrNotZero();
    }
    public SharedGroup(String databaseFile, Durability durability, boolean fileMustExist) {
        context = new Context();
        this.nativePtr = createNative(databaseFile, durability.value, fileMustExist, false);
        checkNativePtrNotZero();
    }
/*
    SharedGroup(String databaseFile, Durability durability, boolean no_create, boolean enableReplication) {
        context = new Context();
        this.nativePtr = createNative(databaseFile, durability.value, no_create, enableReplication);
        checkNativePtr();
    }
*/
    void advanceRead() {
    nativeAdvanceRead(nativePtr, nativeTransactLogRegistryPtr);
}

    private native void nativeAdvanceRead(long nativePtr, long nativeTransactLogRegistryPtr);

    void promoteToWrite() {
        nativePromoteToWrite(nativePtr, nativeTransactLogRegistryPtr);
    }

    private native void nativePromoteToWrite(long nativePtr, long nativeTransactLogRegistryPtr);

    void commitAndContinueAsRead() {
        nativeCommitAndContinueAsRead(nativePtr);
    }

    private native void nativeCommitAndContinueAsRead(long nativePtr);

    public ImplicitTransaction beginImplicitTransaction() {
        if (activeTransaction) {
            throw new IllegalStateException(
                    "Can't beginImplicitTransaction() during another active transaction");
        }
        long nativeGroupPtr = nativeBeginImplicit(nativePtr);
        ImplicitTransaction transaction = new ImplicitTransaction(context, this, nativeGroupPtr);
        activeTransaction = true;
        return transaction;
    }

    private native long nativeBeginImplicit(long nativePtr);

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

            synchronized (context) {
            if (nativePtr != 0) {
                nativeClose(nativePtr);
                nativePtr = 0;
                if (implicistTransactionsEnabled) {
                    if (nativeTransactLogRegistryPtr != 0) {
                        nativeCloseTransactRegistryLog(nativeTransactLogRegistryPtr);
                        nativeTransactLogRegistryPtr = 0;
                    }
                    if (nativeReplicationPtr != 0) {
                        nativeCloseReplication(nativeReplicationPtr);
                        nativeReplicationPtr = 0;
                    }
                }
            }
        }
    }

    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                context.asyncDisposeSharedGroup(nativePtr); 
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
                if (implicistTransactionsEnabled) {
                    if (nativeTransactLogRegistryPtr != 0) {
                        nativeCloseTransactRegistryLog(nativeTransactLogRegistryPtr);
                        nativeTransactLogRegistryPtr = 0;
                    }
                    if (nativeReplicationPtr != 0) {
                        nativeCloseReplication(nativeReplicationPtr);
                        nativeReplicationPtr = 0;
                    }
                }
            }
        }
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

    private void checkNativePtrNotZero() {
        if (this.nativePtr == 0)
            throw new OutOfMemoryError("Out of native memory.");
    }

    protected static native void nativeClose(long nativePtr);
    private native void nativeCloseTransactRegistryLog(long nativeTransactLogRegistryPtr);
    private native void nativeCloseReplication(long nativeReplicationPtr);

}
