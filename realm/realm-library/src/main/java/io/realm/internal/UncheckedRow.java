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

import io.realm.RealmFieldType;

/**
 * Wrapper around a Row in Realm Core.
 *
 * IMPORTANT: All access to methods using this class are non-checking. Safety guarantees are given by the annotation
 * processor and {@link RealmProxyMediator#validateTable(Class, SharedRealm)} which is called before the typed
 * API can be used.
 *
 * For low-level access to Row data where error checking is required, use {@link CheckedRow}.
 */
public class UncheckedRow extends NativeObject implements Row {

    final Context context; // This is only kept because for now it's needed by the constructor of LinkView
    final Table parent;

    protected UncheckedRow(Context context, Table parent, long nativePtr) {
        this.context = context;
        this.parent = parent;
        this.nativePointer = nativePtr;

        context.executeDelayedDisposal();
    }

    /**
     * Gets the row object associated to an index in a Table.
     *
     * @param context the Realm context.
     * @param table the Table that holds the row.
     * @param index the index of the row.
     * @return an instance of Row for the table and index specified.
     */
    public static UncheckedRow getByRowIndex(Context context, Table table, long index) {
        long nativeRowPointer = table.nativeGetRowPtr(table.nativePtr, index);
        UncheckedRow row = new UncheckedRow(context, table, nativeRowPointer);
        context.addReference(NativeObjectReference.TYPE_ROW, row);
        return row;
    }

    /**
     * Gets the row object from a row pointer.
     *
     * @param context the Realm context.
     * @param table the Table that holds the row.
     * @param nativeRowPointer pointer of a row.
     * @return an instance of Row for the table and row specified.
     */
    public static UncheckedRow getByRowPointer(Context context, Table table, long nativeRowPointer) {
        UncheckedRow row = new UncheckedRow(context, table, nativeRowPointer);
        context.addReference(NativeObjectReference.TYPE_ROW, row);
        return row;
    }

    /**
     * Gets the row object associated to an index in a LinkView.
     *
     * @param context the Realm context.
     * @param linkView the LinkView holding the row.
     * @param index the index of the row.
     * @return an instance of Row for the LinkView and index specified.
     */
    public static UncheckedRow getByRowIndex(Context context, LinkView linkView, long index) {
        long nativeRowPointer = linkView.nativeGetRow(linkView.nativePointer, index);
        UncheckedRow row = new UncheckedRow(context, linkView.getTargetTable(),
                nativeRowPointer);
        context.addReference(NativeObjectReference.TYPE_ROW, row);
        return row;
    }

    @Override
    public long getColumnCount() {
        return nativeGetColumnCount(nativePointer);
    }

    @Override
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePointer, columnIndex);
    }


    @Override
    public long getColumnIndex(String columnName) {
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        return nativeGetColumnIndex(nativePointer, columnName);
    }

    @Override
    public RealmFieldType getColumnType(long columnIndex) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativePointer, columnIndex));
    }

    // Getters

    @Override
    public Table getTable() {
        return parent;
    }

    @Override
    public long getIndex() {
        return nativeGetIndex(nativePointer);
    }

    @Override
    public long getLong(long columnIndex) {
        return nativeGetLong(nativePointer, columnIndex);
    }

    @Override
    public boolean getBoolean(long columnIndex) {
        return nativeGetBoolean(nativePointer, columnIndex);
    }

    @Override
    public float getFloat(long columnIndex) {
        return nativeGetFloat(nativePointer, columnIndex);
    }

    @Override
    public double getDouble(long columnIndex) {
        return nativeGetDouble(nativePointer, columnIndex);
    }

    @Override
    public Date getDate(long columnIndex) {
        return new Date(nativeGetTimestamp(nativePointer, columnIndex));
    }

    @Override
    public String getString(long columnIndex) {
        return nativeGetString(nativePointer, columnIndex);
    }

    @Override
    public byte[] getBinaryByteArray(long columnIndex) {
        return nativeGetByteArray(nativePointer, columnIndex);
    }

    @Override
    public long getLink(long columnIndex) {
        return nativeGetLink(nativePointer, columnIndex);
    }

    @Override
    public boolean isNullLink(long columnIndex) {
        return nativeIsNullLink(nativePointer, columnIndex);
    }

    @Override
    public LinkView getLinkList(long columnIndex) {
        long nativeLinkViewPtr = nativeGetLinkView(nativePointer, columnIndex);
        return new LinkView(context, parent, columnIndex, nativeLinkViewPtr);
    }

    // Setters

    @Override
    public void setLong(long columnIndex, long value) {
        parent.checkImmutable();
        getTable().checkIntValueIsLegal(columnIndex, getIndex(), value);
        nativeSetLong(nativePointer, columnIndex, value);
    }

    @Override
    public void setBoolean(long columnIndex, boolean value) {
        parent.checkImmutable();
        nativeSetBoolean(nativePointer, columnIndex, value);
    }

    @Override
    public void setFloat(long columnIndex, float value) {
        parent.checkImmutable();
        nativeSetFloat(nativePointer, columnIndex, value);
    }

    @Override
    public void setDouble(long columnIndex, double value) {
        parent.checkImmutable();
        nativeSetDouble(nativePointer, columnIndex, value);
    }

    @Override
    public void setDate(long columnIndex, Date date) {
        parent.checkImmutable();
        if (date == null) {
            throw new IllegalArgumentException("Null Date is not allowed.");
        }
        long timestamp = date.getTime();
        nativeSetTimestamp(nativePointer, columnIndex, timestamp);
    }

    /**
     * Set a string value to a row pointer.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param value the value to to a row
     */
    @Override
    public void setString(long columnIndex, String value) {
        parent.checkImmutable();
        if (value == null) {
            getTable().checkDuplicatedNullForPrimaryKeyValue(columnIndex, getIndex());
            nativeSetNull(nativePointer, columnIndex);
        } else {
            getTable().checkStringValueIsLegal(columnIndex, getIndex(), value);
            nativeSetString(nativePointer, columnIndex, value);
        }
    }

    @Override
    public void setBinaryByteArray(long columnIndex, byte[] data) {
        parent.checkImmutable();
        nativeSetByteArray(nativePointer, columnIndex, data);
    }

    @Override
    public void setLink(long columnIndex, long value) {
        parent.checkImmutable();
        nativeSetLink(nativePointer, columnIndex, value);
    }

    @Override
    public void nullifyLink(long columnIndex) {
        parent.checkImmutable();
        nativeNullifyLink(nativePointer, columnIndex);
    }

    @Override
    public boolean isNull(long columnIndex) {
        return nativeIsNull(nativePointer, columnIndex);
    }

    /**
     * Set null to a row pointer.
     *
     * @param columnIndex 0 based index value of the cell column.
     */
    @Override
    public void setNull(long columnIndex) {
        parent.checkImmutable();
        getTable().checkDuplicatedNullForPrimaryKeyValue(columnIndex, getIndex());
        nativeSetNull(nativePointer, columnIndex);
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
        return nativePointer != 0 && nativeIsAttached(nativePointer);
    }

    @Override
    public boolean hasColumn(String fieldName) {
        return nativeHasColumn(nativePointer, fieldName);
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
    public static native long nativeGetLinkView(long nativePtr, long columnIndex);
    protected native void nativeSetLong(long nativeRowPtr, long columnIndex, long value);
    protected native void nativeSetBoolean(long nativeRowPtr, long columnIndex, boolean value);
    protected native void nativeSetFloat(long nativeRowPtr, long columnIndex, float value);
    protected native long nativeGetLink(long nativeRowPtr, long columnIndex);
    protected native void nativeSetDouble(long nativeRowPtr, long columnIndex, double value);
    protected native void nativeSetTimestamp(long nativeRowPtr, long columnIndex, long dateTimeValue);
    protected native void nativeSetString(long nativeRowPtr, long columnIndex, String value);
    protected native void nativeSetByteArray(long nativePtr, long columnIndex, byte[] data);
    protected native void nativeSetLink(long nativeRowPtr, long columnIndex, long value);
    protected native void nativeNullifyLink(long nativeRowPtr, long columnIndex);
    static native void nativeClose(long nativeRowPtr);
    protected native boolean nativeIsAttached(long nativeRowPtr);
    protected native boolean nativeHasColumn(long nativeRowPtr, String columnName);
    protected native boolean nativeIsNull(long nativeRowPtr, long columnIndex);
    protected native void nativeSetNull(long nativeRowPtr, long columnIndex);
}
