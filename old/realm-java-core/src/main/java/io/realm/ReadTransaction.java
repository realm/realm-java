package io.realm;

public class ReadTransaction extends Group {

    private final SharedGroup db;
    
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
