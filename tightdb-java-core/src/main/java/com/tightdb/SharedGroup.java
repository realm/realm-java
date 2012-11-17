package com.tightdb;

import com.tightdb.lib.TightDB;

public class SharedGroup {

    static {
        TightDB.loadLibrary();
    }

    public SharedGroup(String databaseFile)
    {
        this.nativePtr = createNative(databaseFile, false);
        checkNativePtr();
    }

    public WriteTransaction beginWrite()
    {
        // FIXME: Can we throw from a native method? If not, we need to have a different way of reporting an error.
        WriteTransaction t = new WriteTransaction(this, nativeBeginWrite(nativePtr));
        activeTransaction = true;
        return t;
    }

    public void close()
    {
        if (activeTransaction) throw new IllegalStateException("Active transaction");
        // Ensure synchronized close
        CloseHandler.getInstance().close(this);
    }



    SharedGroup(String databaseFile, boolean enableReplication)
    {
        this.nativePtr = createNative(databaseFile, enableReplication);
        checkNativePtr();
    }

    void commit()
    {
        nativeCommit(nativePtr);
        activeTransaction = false;
    }

    void rollback()
    {
        nativeRollback(nativePtr);
        activeTransaction = false;
    }

    // Must only be called from CloseHandler
    void doClose()
    {
        if (nativePtr == 0) return;
        nativeClose(nativePtr);
        nativePtr = 0;
    }

    static native String nativeGetDefaultReplicationDatabaseFileName();



    protected void finalize()
    {
        CloseHandler.getInstance().close(this);
    }



    private long nativePtr;
    private boolean activeTransaction;

    private native long nativeBeginWrite(long nativePtr);

    private native void nativeCommit(long nativePtr);

    private native void nativeRollback(long nativePtr);

    private native long createNative(String databaseFile, boolean enableReplication);

    private void checkNativePtr()
    {
        if (this.nativePtr == 0)
            throw new OutOfMemoryError("Out of native memory.");
    }

    private native void nativeClose(long nativePtr);
}
