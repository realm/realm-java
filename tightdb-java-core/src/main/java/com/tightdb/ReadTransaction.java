package com.tightdb;

// ARM only works in Java 1.7
// public class ReadTransaction extends Group implements AutoCloseable {

public class ReadTransaction extends Group {

    private SharedGroup db;

    ReadTransaction(SharedGroup db) {
        super(db.beginReadGroup(), true);
        this.db = db;
    }

    ReadTransaction(SharedGroup db, long nativePtr) {
        super(nativePtr, true); // make Group immutable
        this.db = db;
    }

    public void endRead() {
        db.endRead();
    }

//    @Override
    @Deprecated
    public void close() {
        //System.out.println("read-close");
        db.endRead();
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}
