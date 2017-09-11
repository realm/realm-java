package io.realm.internal;

/**
 * Java wrapper of Object Store List class. This backs managed versions of RealmList.
 */
public class OsList implements NativeObject {

    private final long nativePtr;
    private final NativeContext context;
    private final Table targetTable;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    public OsList(UncheckedRow row, long columnIndex) {
        SharedRealm sharedRealm = row.getTable().getSharedRealm();
        long[] ptrs = nativeCreate(sharedRealm.getNativePtr(), row.getNativePtr(), columnIndex);

        this.nativePtr = ptrs[0];
        this.context = sharedRealm.context;
        context.addReference(this);

        targetTable = new Table(sharedRealm, ptrs[1]);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public UncheckedRow getUncheckedRow(long index) {
        return targetTable.getUncheckedRowByPointer(nativeGetRow(nativePtr, index));
    }

    public void addRow(long targetRowIndex) {
        nativeAddRow(nativePtr, targetRowIndex);
    }

    public void insertRow(long pos, long targetRowIndex) {
        nativeInsertRow(nativePtr, pos, targetRowIndex);
    }

    public void setRow(long pos, long targetRowIndex) {
        nativeSetRow(nativePtr, pos, targetRowIndex);
    }

    public void move(long sourceIndex, long targetIndex) {
        nativeMove(nativePtr, sourceIndex, targetIndex);
    }

    public void remove(long index) {
        nativeRemove(nativePtr, index);
    }

    public void removeAll() {
        nativeRemoveAll(nativePtr);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public boolean isEmpty() {
        return nativeSize(nativePtr) <= 0;
    }

    /**
     * @return a {@link TableQuery} based on this list.
     */
    public TableQuery getQuery() {
        return new TableQuery(context, targetTable, nativeGetQuery(nativePtr));
    }

    public boolean isValid() {
        return nativeIsValid(nativePtr);
    }

    public void delete(long index) {
        nativeDelete(nativePtr, index);
    }

    public void deleteAll() {
        nativeDeleteAll(nativePtr);
    }

    public Table getTargetTable() {
        return targetTable;
    }

    private static native long nativeGetFinalizerPtr();

    // TODO: nativeTablePtr is not necessary. It is used to create FieldDescriptor which should be generated from
    // OsSchemaInfo.
    // Returns {nativeListPtr, nativeTablePtr}
    private static native long[] nativeCreate(long nativeSharedRealmPtr, long nativeRowPtr, long columnIndex);

    private static native long nativeGetRow(long nativePtr, long index);

    private static native void nativeAddRow(long nativePtr, long targetRowIndex);

    private static native void nativeInsertRow(long nativePtr, long pos, long targetRowIndex);

    private static native void nativeSetRow(long nativePtr, long pos, long targetRowIndex);

    private static native void nativeMove(long nativePtr, long sourceIndex, long targetIndex);

    private static native void nativeRemove(long nativePtr, long index);

    private static native void nativeRemoveAll(long nativePtr);

    private static native long nativeSize(long nativePtr);

    private static native long nativeGetQuery(long nativePtr);

    private static native boolean nativeIsValid(long nativePtr);

    private static native void nativeDelete(long nativePtr, long index);

    private static native void nativeDeleteAll(long nativePtr);
}
