package com.tightdb;

// ARM only works in Java 1.7
// public class ReadTransaction extends Group implements AutoCloseable {

public class ReadTransaction extends Group {

    private SharedGroup db;
    
    ReadTransaction(Context context, SharedGroup db, long nativePointer) {
        super(context, nativePointer, true); // make Group immutable
        this.db = db;
    }

    public void endRead() {
        db.endRead();
    }

    @Override
    public void close() {
        db.endRead();
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}
