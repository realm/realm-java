package com.tightdb;

public class WriteTransaction extends Group {

    private SharedGroup db;
    private boolean committed;

    public void commit() {
    	if (!committed) {
    		db.commit();
        	committed = true;
    	} 
    	else {
    		throw new IllegalStateException("You can only commit once after a WriteTransaction has been made.");
    	}
    }

    public void rollback() {
        db.rollback();
    }

    public void close() {
        if (!committed) {        	
        	rollback();
        }
    }

    WriteTransaction(SharedGroup db, long nativePtr) {
        super(nativePtr, false);    // Group is mutable
        this.db = db;
        committed = false;
    }

    protected void finalize() {
    } // Nullify the actions of Group.finalize()
}
