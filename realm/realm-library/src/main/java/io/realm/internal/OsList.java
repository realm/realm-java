package io.realm.internal;

import java.util.Date;

import javax.annotation.Nullable;


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

    public void addNull() {
        // FIXME zaki50 implement this
    }

    public void insertNull(long pos) {
        // FIXME zaki50 implement this
    }

    public void setNull(long pos) {
        // FIXME zaki50 implement this
    }

    public void addLong(long value) {
        // FIXME zaki50 implement this
    }

    public void insertLong(long pos, long value) {
        // FIXME zaki50 implement this
    }

    public void setLong(long pos, long value) {
        // FIXME zaki50 implement this
    }

    public void addDouble(double value) {
        // FIXME zaki50 implement this
    }

    public void insertDouble(long pos, double value) {
        // FIXME zaki50 implement this
    }

    public void setDouble(long pos, double value) {
        // FIXME zaki50 implement this
    }

    public void addFloat(float value) {
        // FIXME zaki50 implement this
    }

    public void insertFloat(long pos, float value) {
        // FIXME zaki50 implement this
    }

    public void setFloat(long pos, float value) {
        // FIXME zaki50 implement this
    }

    public void addBoolean(boolean value) {
        // FIXME zaki50 implement this
    }

    public void insertBoolean(long pos, boolean value) {
        // FIXME zaki50 implement this
    }

    public void setBoolean(long pos, boolean value) {
        // FIXME zaki50 implement this
    }

    public void addBinary(byte[] value) {
        // FIXME zaki50 implement this
    }

    public void insertBinary(long pos, byte[] value) {
        // FIXME zaki50 implement this
    }

    public void setBinary(long pos, byte[] value) {
        // FIXME zaki50 implement this
    }

    public void addString(String value) {
        // FIXME zaki50 implement this
    }

    public void insertString(long pos, String value) {
        // FIXME zaki50 implement this
    }

    public void setString(long pos, String value) {
        // FIXME zaki50 implement this
    }

    public void addDate(Date value) {
        // FIXME zaki50 implement this
    }

    public void insertDate(long pos, Date value) {
        // FIXME zaki50 implement this
    }

    public void setDate(long pos, Date value) {
        // FIXME zaki50 implement this
    }

    @Nullable
    public Object getValue(long pos) {
        // FIXME zaki50 implement this
        return null;
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
