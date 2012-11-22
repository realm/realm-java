package com.tightdb;

public class ReadTransaction extends Group {

    private SharedGroup db;

    ReadTransaction(SharedGroup db, long nativePtr)
    {
        super(nativePtr, true); // make Group immutable
        this.db = db;
    }

    public void endRead()
    {
        db.endRead();
    }

    public void close()
    {
        db.endRead();
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}