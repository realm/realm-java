package io.realm.internal;

public class WriteTransaction extends Group {

    private final SharedGroup db;
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

    @Override
    public void close() {
        if (!committed) {
            rollback();
        }
    }

    WriteTransaction(Context context,SharedGroup db, long nativePtr) {
        super(context, nativePtr, false);    // Group is mutable
        this.db = db;
        committed = false;
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}
