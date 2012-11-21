package com.tightdb;

public class ReadTransaction extends Group {

    private SharedGroup db;

    ReadTransaction(SharedGroup db, long nativePtr)
    {
        super(nativePtr);
        this.db = db;
    }

    public void close()
    {
        db.endRead();
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}