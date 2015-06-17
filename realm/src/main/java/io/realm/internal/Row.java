/*
 * Copyright 2014 Realm Inc.
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

/**
 * Wrapper around a Row in Realm Core.
 *
 * IMPORTANT: All access to methods using this class are non-checking. Safety guarantees are given by the the annotation
 * processor and {@link RealmProxyMediator#validateTable(Class, ImplicitTransaction)} which is called before the typed
 * API can be used.
 *
 * For low-level access to Row data where error checking is required, use {@link CheckedRow}.
 */
public class Row extends NativeObject {

    final Context context; // This is only kept because for now it's needed by the constructor of LinkView
    final Table parent;

    protected Row(Context context, Table parent, long nativePtr) {
        this.context = context;
        this.parent = parent;
        this.nativePointer = nativePtr;
    }

    /**
     * Get the row object associated to an index in a Table
     * @param context the Realm context
     * @param table the Table that holds the row
     * @param index the index of the row
     * @return an instance of Row for the table and index specified
     */
    public static Row get(Context context, Table table, long index) {
        long nativeRowPointer = table.nativeGetRowPtr(table.nativePtr, index);
        Row row = new Row(context, table, nativeRowPointer);
        FinalizerRunnable.references.put(new NativeObjectReference(row, FinalizerRunnable.referenceQueue), Boolean.TRUE);
        return row;
    }

    /**
     * Get the row object associated to an index in a LinkView
     * @param context the Realm context
     * @param linkView the LinkView holding the row
     * @param index the index of the row
     * @return an instance of Row for the LinkView and index specified
     */
    public static Row get(Context context, LinkView linkView, long index) {
        long nativeRowPointer = linkView.nativeGetRow(linkView.nativeLinkViewPtr, index);
        Row row = new Row(context, linkView.parent.getLinkTarget(linkView.columnIndexInParent), nativeRowPointer);
        FinalizerRunnable.references.put(new NativeObjectReference(row, FinalizerRunnable.referenceQueue), Boolean.TRUE);
        return row;
    }

    public long getColumnCount() {
        return nativeGetColumnCount(nativePointer);
    }

    /**
     * Returns the name of a column identified by columnIndex. Notice that the
     * index is zero based.
     *
     * @param columnIndex the column index
     * @return the name of the column
     */
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePointer, columnIndex);
    }


    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name
     * @return the index, -1 if not found
     */
    public long getColumnIndex(String columnName) {
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        return nativeGetColumnIndex(nativePointer, columnName);
    }

    /**
     * Get the type of a column identified by the columnIndex.
     *
     * @param columnIndex index of the column.
     * @return Type of the particular column.
     */
    public ColumnType getColumnType(long columnIndex) {
        return ColumnType.fromNativeValue(nativeGetColumnType(nativePointer, columnIndex));
    }

    // Getters

    public Table getTable() {
        return parent;
    }

    public long getIndex() {
        return nativeGetIndex(nativePointer);
    }

    public long getLong(long columnIndex) {
        return nativeGetLong(nativePointer, columnIndex);
    }

    public boolean getBoolean(long columnIndex) {
        return nativeGetBoolean(nativePointer, columnIndex);
    }

    public float getFloat(long columnIndex) {
        return nativeGetFloat(nativePointer, columnIndex);
    }

    public double getDouble(long columnIndex) {
        return nativeGetDouble(nativePointer, columnIndex);
    }

    public Date getDate(long columnIndex) {
        return new Date(nativeGetDateTime(nativePointer, columnIndex)*1000);
    }

    public String getString(long columnIndex) {
        return nativeGetString(nativePointer, columnIndex);
    }

    public byte[] getBinaryByteArray(long columnIndex) {
        return nativeGetByteArray(nativePointer, columnIndex);
    }

    public Mixed getMixed(long columnIndex) {
        return nativeGetMixed(nativePointer, columnIndex);
    }

    public ColumnType getMixedType(long columnIndex) {
        return ColumnType.fromNativeValue(nativeGetMixedType(nativePointer, columnIndex));
    }

    public long getLink(long columnIndex) {
        return nativeGetLink(nativePointer, columnIndex);
    }

    public boolean isNullLink(long columnIndex) {
        return nativeIsNullLink(nativePointer, columnIndex);
    }

    public LinkView getLinkList(long columnIndex) {
        long nativeLinkViewPtr = nativeGetLinkView(nativePointer, columnIndex);
        return new LinkView(context, parent, columnIndex, nativeLinkViewPtr);
    }

    // Setters

    public void setLong(long columnIndex, long value) {
        parent.checkImmutable();
        getTable().checkIntValueIsLegal(columnIndex, getIndex(), value);
        nativeSetLong(nativePointer, columnIndex, value);
    }

    public void setBoolean(long columnIndex, boolean value) {
        parent.checkImmutable();
        nativeSetBoolean(nativePointer, columnIndex, value);
    }

    public void setFloat(long columnIndex, float value) {
        parent.checkImmutable();
        nativeSetFloat(nativePointer, columnIndex, value);
    }

    public void setDouble(long columnIndex, double value) {
        parent.checkImmutable();
        nativeSetDouble(nativePointer, columnIndex, value);
    }

    public void setDate(long columnIndex, Date date) {
        parent.checkImmutable();
        if (date == null) {
            throw new IllegalArgumentException("Null Date is not allowed.");
        }
        long timestamp = date.getTime() / 1000;
        if (timestamp >= Integer.MAX_VALUE || timestamp <= Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Date/timestamp is outside valid range");
        }
        nativeSetDate(nativePointer, columnIndex, timestamp);
    }

    public void setString(long columnIndex, String value) {
        parent.checkImmutable();
        getTable().checkStringValueIsLegal(columnIndex, getIndex(), value);
        nativeSetString(nativePointer, columnIndex, value);
    }

    public void setBinaryByteArray(long columnIndex, byte[] data) {
        parent.checkImmutable();
        if (data == null) {
            throw new IllegalArgumentException("Null array is not allowed");
        }
        nativeSetByteArray(nativePointer, columnIndex, data);
    }

    public void setMixed(long columnIndex, Mixed data) {
        parent.checkImmutable();
        if (data == null) {
            throw new IllegalArgumentException("Null data is not allowed");
        }
        nativeSetMixed(nativePointer, columnIndex, data);
    }

    public void setLink(long columnIndex, long value) {
        parent.checkImmutable();
        nativeSetLink(nativePointer, columnIndex, value);
    }

    public void nullifyLink(long columnIndex) {
        parent.checkImmutable();
        nativeNullifyLink(nativePointer, columnIndex);
    }

    /**
     * Converts the unchecked Row to a checked variant.
     *
     * @return CheckedRow wrapping the same Realm data as the original Row.
     */
    public CheckedRow convertToChecked() {
        return CheckedRow.getFromRow(this);
    }

    /**
     * Checks if the row is still valid.
     * @return Returns true {@code true} if the row is still valid and attached to the underlying
     * data. {@code false} otherwise.
     */
    public boolean isAttached() {
        return nativePointer != 0 && nativeIsAttached(nativePointer);
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
    protected native long nativeGetDateTime(long nativeRowPtr, long columnIndex);
    protected native String nativeGetString(long nativePtr, long columnIndex);
    protected native boolean nativeIsNullLink(long nativeRowPtr, long columnIndex);
    protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex);
    protected native int nativeGetMixedType(long nativePtr, long columnIndex);
    protected native Mixed nativeGetMixed(long nativeRowPtr, long columnIndex);
    protected native long nativeGetLinkView(long nativePtr, long columnIndex);
    protected native void nativeSetLong(long nativeRowPtr, long columnIndex, long value);
    protected native void nativeSetBoolean(long nativeRowPtr, long columnIndex, boolean value);
    protected native void nativeSetFloat(long nativeRowPtr, long columnIndex, float value);
    protected native long nativeGetLink(long nativeRowPtr, long columnIndex);
    protected native void nativeSetDouble(long nativeRowPtr, long columnIndex, double value);
    protected native void nativeSetDate(long nativeRowPtr, long columnIndex, long dateTimeValue);
    protected native void nativeSetString(long nativeRowPtr, long columnIndex, String value);
    protected native void nativeSetByteArray(long nativePtr, long columnIndex, byte[] data);
    protected native void nativeSetMixed(long nativeRowPtr, long columnIndex, Mixed data);
    protected native void nativeSetLink(long nativeRowPtr, long columnIndex, long value);
    protected native void nativeNullifyLink(long nativeRowPtr, long columnIndex);
    protected static native void nativeClose(long nativeRowPtr);
    protected native boolean nativeIsAttached(long nativeRowPtr);
}
