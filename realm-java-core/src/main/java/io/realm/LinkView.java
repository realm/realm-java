package io.realm;

public class LinkView {

    private final Context context;
    private final long nativeLinkViewPtr;
    private final Table parent;
    private final long columnIndexInParent;

    public LinkView(Context context, Table parent, long columnIndexInParent, long nativeLinkViewPtr) {
        this.context = context;
        this.parent = parent;
        this.columnIndexInParent = columnIndexInParent;
        this.nativeLinkViewPtr = nativeLinkViewPtr;
    }

    protected static native void nativeClose(long nativeLinkViewPtr);

    public Row get(long pos) {
        long nativeRowPtr = nativeGetRow(nativeLinkViewPtr, pos);
        return new Row(context, parent.getLinkTarget(columnIndexInParent), nativeRowPtr);
    }
    private native long nativeGetRow(long nativeLinkViewPtr, long pos);

    public long getTargetRowIndex(long pos) {
        return nativeGetTargetRowIndex(nativeLinkViewPtr, pos);
    }
    private native long nativeGetTargetRowIndex(long nativeLinkViewPtr, long pos);

    public void add(long rowIndex) {
        nativeAdd(nativeLinkViewPtr, rowIndex);
    }
    private native void nativeAdd(long nativeLinkViewPtr, long rowIndex);

    public void insert(long pos, long rowIndex) {
        nativeInsert(nativeLinkViewPtr, pos, rowIndex);
    }
    private native void nativeInsert(long nativeLinkViewPtr, long pos, long rowIndex);

    public void set(long pos, long rowIndex) {
        nativeSet(nativeLinkViewPtr, pos, rowIndex);
    }
    private native void nativeSet(long nativeLinkViewPtr, long pos, long rowIndex);

    public void move(long oldPos, long newPos) {
        nativeMove(nativeLinkViewPtr, oldPos, newPos);
    }
    private native void nativeMove(long nativeLinkViewPtr, long oldPos, long newPos);

    public void remove(long pos) {
        nativeRemove(nativeLinkViewPtr, pos);
    }
    private native void nativeRemove(long nativeLinkViewPtr, long pos);

    public void clear() {
        nativeClear(nativeLinkViewPtr);
    }
    private native void nativeClear(long nativeLinkViewPtr);

    public long size() {
        return nativeSize(nativeLinkViewPtr);
    }
    private native long nativeSize(long nativeLinkViewPtr);

    public boolean isEmpty() {
        System.out.println(nativeLinkViewPtr);
        return nativeIsEmpty(nativeLinkViewPtr);
    }
    private native boolean nativeIsEmpty(long nativeLinkViewPtr);
}
