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

public class Row {

    private final Context context;
    private final Table parent;
    protected long nativePtr;

    Row(Context context, Table parent, long nativePtr) {
        this.context = context;
        this.parent = parent;
        this.nativePtr = nativePtr;
    }


    public long getColumnCount() {
        return nativeGetColumnCount(nativePtr);
    }

    protected native long nativeGetColumnCount(long nativeTablePtr);

    /**
     * Returns the name of a column identified by columnIndex. Notice that the
     * index is zero based.
     *
     * @param columnIndex the column index
     * @return the name of the column
     */
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePtr, columnIndex);
    }

    protected native String nativeGetColumnName(long nativeTablePtr, long columnIndex);

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name
     * @return the index, -1 if not found
     */
    public long getColumnIndex(String columnName) {
        if (columnName == null)
            throw new IllegalArgumentException("Column name can not be null.");
        return nativeGetColumnIndex(nativePtr, columnName);
    }

    protected native long nativeGetColumnIndex(long nativeTablePtr, String columnName);


    /**
     * Get the type of a column identified by the columnIdex.
     *
     * @param columnIndex index of the column.
     * @return Type of the particular column.
     */
    public ColumnType getColumnType(long columnIndex) {
        return ColumnType.fromNativeValue(nativeGetColumnType(nativePtr, columnIndex));
    }

    protected native int nativeGetColumnType(long nativeTablePtr, long columnIndex);



    // Getters

    public Table getTable() {
        return parent;
    }

    public long getIndex() {
        return nativeGetIndex(nativePtr);
    }

    protected native long nativeGetIndex(long nativeRowPtr);

    public long getLong(long columnIndex) {
        return nativeGetLong(nativePtr, columnIndex);
    }

    protected native long nativeGetLong(long nativeRowPtr, long columnIndex);

    public boolean getBoolean(long columnIndex) {
        return nativeGetBoolean(nativePtr, columnIndex);
    }

    protected native boolean nativeGetBoolean(long nativeRowPtr, long columnIndex);

    public float getFloat(long columnIndex) {
        return nativeGetFloat(nativePtr, columnIndex);
    }

    protected native float nativeGetFloat(long nativeRowPtr, long columnIndex);

    public double getDouble(long columnIndex) {
        return nativeGetDouble(nativePtr, columnIndex);
    }

    protected native double nativeGetDouble(long nativeRowPtr, long columnIndex);

    public Date getDate(long columnIndex) {
        return new Date(nativeGetDateTime(nativePtr, columnIndex)*1000);
    }

    protected native long nativeGetDateTime(long nativeRowPtr, long columnIndex);


    public String getString(long columnIndex) {
        return nativeGetString(nativePtr, columnIndex);
    }

    protected native String nativeGetString(long nativePtr, long columnIndex);


    public byte[] getBinaryByteArray(long columnIndex) {
        return nativeGetByteArray(nativePtr, columnIndex);
    }

    protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex);


    public Mixed getMixed(long columnIndex) {
        return nativeGetMixed(nativePtr, columnIndex);
    }

    public ColumnType getMixedType(long columnIndex) {
        return ColumnType.fromNativeValue(nativeGetMixedType(nativePtr, columnIndex));
    }

    protected native int nativeGetMixedType(long nativePtr, long columnIndex);

    protected native Mixed nativeGetMixed(long nativeRowPtr, long columnIndex);

    public long getLink(long columnIndex) {
        return nativeGetLink(nativePtr, columnIndex);
    }

    protected native long nativeGetLink(long nativeRowPtr, long columnIndex);

    public boolean isNullLink(long columnIndex) {
        return nativeIsNullLink(nativePtr, columnIndex);
    }

    protected native boolean nativeIsNullLink(long nativeRowPtr, long columnIndex);

    public LinkView getLinkList(long columnIndex) {
        long nativeLinkViewPtr = nativeGetLinkView(nativePtr, columnIndex);
        return new LinkView(context, parent, columnIndex, nativeLinkViewPtr);
    }

    private native long nativeGetLinkView(long nativePtr, long columnIndex);


    // Setters

    public void setLong(long columnIndex, long value) {
        parent.checkImmutable();
        nativeSetLong(nativePtr, columnIndex, value);
    }

    protected native void nativeSetLong(long nativeRowPtr, long columnIndex, long value);

    public void setBoolean(long columnIndex, boolean value) {
        parent.checkImmutable();
        nativeSetBoolean(nativePtr, columnIndex, value);
    }

    protected native void nativeSetBoolean(long nativeRowPtr, long columnIndex, boolean value);

    public void setFloat(long columnIndex, float value) {
        parent.checkImmutable();
        nativeSetFloat(nativePtr, columnIndex, value);
    }

    protected native void nativeSetFloat(long nativeRowPtr, long columnIndex, float value);

    public void setDouble(long columnIndex, double value) {
        parent.checkImmutable();
        nativeSetDouble(nativePtr, columnIndex, value);
    }

    protected native void nativeSetDouble(long nativeRowPtr, long columnIndex, double value);

    public void setDate(long columnIndex, Date date) {
        parent.checkImmutable();
        if (date == null)
            throw new IllegalArgumentException("Null Date is not allowed.");
        long timestamp = date.getTime() / 1000;
        if (timestamp >= Integer.MAX_VALUE || timestamp <= Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Date/timestamp is outside valid range");
        }
        nativeSetDate(nativePtr, columnIndex, timestamp);
    }

    protected native void nativeSetDate(long nativeRowPtr, long columnIndex, long dateTimeValue);

    public void setString(long columnIndex, String value) {
        parent.checkImmutable();
        if (value == null)
            throw new IllegalArgumentException("Null String is not allowed.");
        nativeSetString(nativePtr, columnIndex, value);
    }

    protected native void nativeSetString(long nativeRowPtr, long columnIndex, String value);

    public void setBinaryByteArray(long columnIndex, byte[] data) {
        parent.checkImmutable();
        if (data == null)
            throw new IllegalArgumentException("Null Array");
        nativeSetByteArray(nativePtr, columnIndex, data);
    }

    protected native void nativeSetByteArray(long nativePtr, long columnIndex, byte[] data);


    public void setMixed(long columnIndex, Mixed data) {
        parent.checkImmutable();
        if (data == null)
            throw new IllegalArgumentException();
        nativeSetMixed(nativePtr, columnIndex, data);
    }

    protected native void nativeSetMixed(long nativeRowPtr, long columnIndex, Mixed data);

    public void setLink(long columnIndex, long value) {
        parent.checkImmutable();
        nativeSetLink(nativePtr, columnIndex, value);
    }

    protected native void nativeSetLink(long nativeRowPtr, long columnIndex, long value);

    public void nullifyLink(long columnIndex) {
        parent.checkImmutable();
        nativeNullifyLink(nativePtr, columnIndex);
    }

    protected native void nativeNullifyLink(long nativeRowPtr, long columnIndex);

    protected static native void nativeClose(long nativeRowPtr);

    @Override
    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                context.asyncDisposeRow(nativePtr);
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
            }
        }
        nativeClose(nativePtr);
        nativePtr = 0;
    }

}
