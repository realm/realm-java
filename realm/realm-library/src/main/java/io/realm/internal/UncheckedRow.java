/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.RealmFieldType;


/**
 * Wrapper around a Row in Realm Core.
 * <p>
 * IMPORTANT: All access to methods using this class are non-checking. Safety guarantees are given by the
 * annotation processor and Object Store's typed Realm schema validation which is called before the typed API can be
 * used.
 * <p>
 * For low-level access to Row data where error checking is required, use {@link CheckedRow}.
 */
public class UncheckedRow implements NativeObject, Row {
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final NativeContext context; // This is only kept because for now it's needed by the constructor of LinkView
    private final Table parent;
    private final long nativePtr;

    UncheckedRow(NativeContext context, Table parent, long nativePtr) {
        this.context = context;
        this.parent = parent;
        this.nativePtr = nativePtr;
        context.addReference(this);
    }

    // This is called by the CheckedRow constructor. The caller should hold a reference to the
    // source UncheckedRow since the native destruction is handled by the source UncheckedRow.
    UncheckedRow(UncheckedRow row) {
        this.context = row.context;
        this.parent = row.parent;
        this.nativePtr = row.nativePtr;
        // The destruction is handled by the source UncheckedRow. No need to add to the ref pool.
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    /**
     * Gets the row object associated to an index in a Table.
     *
     * @param context the Realm context.
     * @param table the Table that holds the row.
     * @param index the index of the row.
     * @return an instance of Row for the table and index specified.
     */
    static UncheckedRow getByRowIndex(NativeContext context, Table table, long index) {
        long nativeRowPointer = table.nativeGetRowPtr(table.getNativePtr(), index);
        return new UncheckedRow(context, table, nativeRowPointer);
    }

    /**
     * Gets the row object from a row pointer.
     *
     * @param context the Realm context.
     * @param table the Table that holds the row.
     * @param nativeRowPointer pointer of a row.
     * @return an instance of Row for the table and row specified.
     */
    static UncheckedRow getByRowPointer(NativeContext context, Table table, long nativeRowPointer) {
        return new UncheckedRow(context, table, nativeRowPointer);
    }

    @Override
    public long getColumnCount() {
        return nativeGetColumnCount(nativePtr);
    }

    @Override
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePtr, columnIndex);
    }


    @Override
    public long getColumnIndex(String columnName) {
        //noinspection ConstantConditions
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        return nativeGetColumnIndex(nativePtr, columnName);
    }

    @Override
    public RealmFieldType getColumnType(long columnIndex) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativePtr, columnIndex));
    }

    // Getters

    @Override
    public Table getTable() {
        return parent;
    }

    @Override
    public long getIndex() {
        return nativeGetIndex(nativePtr);
    }

    @Override
    public long getLong(long columnIndex) {
        return nativeGetLong(nativePtr, columnIndex);
    }

    @Override
    public boolean getBoolean(long columnIndex) {
        return nativeGetBoolean(nativePtr, columnIndex);
    }

    @Override
    public float getFloat(long columnIndex) {
        return nativeGetFloat(nativePtr, columnIndex);
    }

    @Override
    public double getDouble(long columnIndex) {
        return nativeGetDouble(nativePtr, columnIndex);
    }

    @Override
    public Date getDate(long columnIndex) {
        return new Date(nativeGetTimestamp(nativePtr, columnIndex));
    }

    @Override
    public String getString(long columnIndex) {
        return nativeGetString(nativePtr, columnIndex);
    }

    @Override
    public byte[] getBinaryByteArray(long columnIndex) {
        return nativeGetByteArray(nativePtr, columnIndex);
    }

    @Override
    public long getLink(long columnIndex) {
        return nativeGetLink(nativePtr, columnIndex);
    }

    @Override
    public boolean isNullLink(long columnIndex) {
        return nativeIsNullLink(nativePtr, columnIndex);
    }

    @Override
    public OsList getModelList(long columnIndex) {
        return new OsList(this, columnIndex);
    }

    @Override
    public OsList getValueList(long columnIndex, RealmFieldType fieldType) {
        return new OsList(this, columnIndex);
    }

    // Setters

    @Override
    public void setLong(long columnIndex, long value) {
        parent.checkImmutable();
        nativeSetLong(nativePtr, columnIndex, value);
    }

    @Override
    public void setBoolean(long columnIndex, boolean value) {
        parent.checkImmutable();
        nativeSetBoolean(nativePtr, columnIndex, value);
    }

    @Override
    public void setFloat(long columnIndex, float value) {
        parent.checkImmutable();
        nativeSetFloat(nativePtr, columnIndex, value);
    }

    @Override
    public void setDouble(long columnIndex, double value) {
        parent.checkImmutable();
        nativeSetDouble(nativePtr, columnIndex, value);
    }

    @Override
    public void setDate(long columnIndex, Date date) {
        parent.checkImmutable();
        //noinspection ConstantConditions
        if (date == null) {
            throw new IllegalArgumentException("Null Date is not allowed.");
        }
        long timestamp = date.getTime();
        nativeSetTimestamp(nativePtr, columnIndex, timestamp);
    }

    /**
     * Sets a string value to a row pointer.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param value the value to to a row
     */
    @Override
    public void setString(long columnIndex, @Nullable String value) {
        parent.checkImmutable();
        if (value == null) {
            nativeSetNull(nativePtr, columnIndex);
        } else {
            nativeSetString(nativePtr, columnIndex, value);
        }
    }

    @Override
    public void setBinaryByteArray(long columnIndex, @Nullable byte[] data) {
        parent.checkImmutable();
        nativeSetByteArray(nativePtr, columnIndex, data);
    }

    @Override
    public void setLink(long columnIndex, long value) {
        parent.checkImmutable();
        nativeSetLink(nativePtr, columnIndex, value);
    }

    @Override
    public void nullifyLink(long columnIndex) {
        parent.checkImmutable();
        nativeNullifyLink(nativePtr, columnIndex);
    }

    @Override
    public boolean isNull(long columnIndex) {
        return nativeIsNull(nativePtr, columnIndex);
    }

    /**
     * Sets null to a row pointer.
     *
     * @param columnIndex 0 based index value of the cell column.
     */
    @Override
    public void setNull(long columnIndex) {
        parent.checkImmutable();
        nativeSetNull(nativePtr, columnIndex);
    }

    /**
     * Converts the unchecked Row to a checked variant.
     *
     * @return the {@link CheckedRow} wrapping the same Realm data as the original {@link Row}.
     */
    public CheckedRow convertToChecked() {
        return CheckedRow.getFromRow(this);
    }

    @Override
    public boolean isAttached() {
        return nativePtr != 0 && nativeIsAttached(nativePtr);
    }

    @Override
    public void checkIfAttached() {
        if (!isAttached()) {
            throw new IllegalStateException("Object is no longer managed by Realm. Has it been deleted?");
        }
    }

    @Override
    public boolean hasColumn(String fieldName) {
        return nativeHasColumn(nativePtr, fieldName);
    }

    protected native long nativeGetColumnCount(long nativeTablePtr);

    protected native String nativeGetColumnName(long nativeTablePtr, long columnIndex);

    protected native long nativeGetColumnIndex(long nativeTablePtr, String columnName);

    protected native int nativeGetColumnType(long nativeTablePtr, long columnIndex);

    protected native long nativeGetIndex(long nativeRowPtr);

    protected native long nativeGetLong(long nativeRowPtr, long columnIndex);

    protected native boolean nativeGetBoolean(long nativeRowPtr, long columnIndex);

    protected native float nativeGetFloat(long nativeRowPtr, long columnIndex);

    protected native double nativeGetDouble(long nativeRowPtr, long columnIndex);

    protected native long nativeGetTimestamp(long nativeRowPtr, long columnIndex);

    protected native String nativeGetString(long nativePtr, long columnIndex);

    protected native boolean nativeIsNullLink(long nativeRowPtr, long columnIndex);

    protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex);

    protected native void nativeSetLong(long nativeRowPtr, long columnIndex, long value);

    protected native void nativeSetBoolean(long nativeRowPtr, long columnIndex, boolean value);

    protected native void nativeSetFloat(long nativeRowPtr, long columnIndex, float value);

    protected native long nativeGetLink(long nativeRowPtr, long columnIndex);

    protected native void nativeSetDouble(long nativeRowPtr, long columnIndex, double value);

    protected native void nativeSetTimestamp(long nativeRowPtr, long columnIndex, long dateTimeValue);

    protected native void nativeSetString(long nativeRowPtr, long columnIndex, String value);

    protected native void nativeSetByteArray(long nativePtr, long columnIndex, @Nullable byte[] data);

    protected native void nativeSetLink(long nativeRowPtr, long columnIndex, long value);

    protected native void nativeNullifyLink(long nativeRowPtr, long columnIndex);

    protected native boolean nativeIsAttached(long nativeRowPtr);

    protected native boolean nativeHasColumn(long nativeRowPtr, String columnName);

    protected native boolean nativeIsNull(long nativeRowPtr, long columnIndex);

    protected native void nativeSetNull(long nativeRowPtr, long columnIndex);

    private static native long nativeGetFinalizerPtr();
}
