package io.realm.internal;

public class ImplicitTransaction extends Group {

    private final SharedGroup parent;

    public ImplicitTransaction(Context context, SharedGroup sharedGroup, long nativePtr) {
        super(context, nativePtr, true);
        parent = sharedGroup;
    }

    public void advanceRead() {
        parent.advanceRead();
    }

    public void promoteToWrite() {
        parent.promoteToWrite();
        immutable = false;
    }

    public void commitAndContinueAsRead() {
        parent.commitAndContinueAsRead();
        immutable = true;
    }

    public void endRead() {
        parent.endRead();
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()

}
