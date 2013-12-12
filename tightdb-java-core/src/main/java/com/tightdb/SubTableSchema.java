package com.tightdb;


public class SubtableSchema implements TableSchema {

    private long[] path;
    private long parentNativePtr;

    SubtableSchema(long parentNativePtr, long[] path) {
        this.parentNativePtr = parentNativePtr;
        this.path = path;
    }

    @Override
    public SubtableSchema getSubtableSchema(long columnIndex) {
        long[] newPath = new long[this.path.length+1];
        for (int i = 0; i < this.path.length; i++) {
            newPath[i] = path[i];
        }
        newPath[this.path.length] = columnIndex;
        return new SubtableSchema(this.parentNativePtr, newPath);
    }

    private void verifyColumnName(String name) {
        if (name.length() > 63) {
            throw new IllegalArgumentException("Column names are currently limited to max 63 characters.");
        }
    }

    @Override
    public long addColumn(ColumnType type, String name) {
        verifyColumnName(name);
        return nativeAddColumn(parentNativePtr, path, type.getValue(), name);
    }

    protected native long nativeAddColumn(long nativeTablePtr, long[] path, int type, String name);

    /**
     * Remove a column in the table dynamically.
     */
    @Override
    public void removeColumn(long columnIndex) {
        nativeRemoveColumn(parentNativePtr, path, columnIndex);
    }

    protected native void nativeRemoveColumn(long nativeTablePtr, long[] path, long columnIndex);

    /**
     * Rename a column in the table.
     */
    @Override
    public void renameColumn(long columnIndex, String newName) {
        verifyColumnName(newName);
        nativeRenameColumn(parentNativePtr, path, columnIndex, newName);
    }

    protected native void nativeRenameColumn(long nativeTablePtr, long[] path, long columnIndex, String name);
}
