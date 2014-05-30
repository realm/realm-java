package io.realm;

import java.util.Date;

public class Row {

    protected long nativePtr;

    Row(long nativePtr) {
        this.nativePtr = nativePtr;
    }


    // Getters

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



    // Setters

    public void setLong(long columnIndex, long value) {
        nativeSetLong(nativePtr, columnIndex, value);
    }

    protected native void nativeSetLong(long nativeRowPtr, long columnIndex, long value);

    public void setBoolean(long columnIndex, boolean value) {
        nativeSetBoolean(nativePtr, columnIndex, value);
    }

    protected native void nativeSetBoolean(long nativeRowPtr, long columnIndex, boolean value);

    public void setFloat(long columnIndex, float value) {
        nativeSetFloat(nativePtr, columnIndex, value);
    }

    protected native void nativeSetFloat(long nativeRowPtr, long columnIndex, float value);

    public void setDouble(long columnIndex, double value) {
        nativeSetDouble(nativePtr, columnIndex, value);
    }

    protected native void nativeSetDouble(long nativeRowPtr, long columnIndex, double value);

    public void setDate(long columnIndex, Date date) {
        if (date == null)
            throw new IllegalArgumentException("Null Date is not allowed.");
        nativeSetDate(nativePtr, columnIndex, date.getTime() / 1000);
    }

    protected native void nativeSetDate(long nativeRowPtr, long columnIndex, long dateTimeValue);

    public void setString(long columnIndex, String value) {
        if (value == null)
            throw new IllegalArgumentException("Null String is not allowed.");
        nativeSetString(nativePtr, columnIndex, value);
    }

    protected native void nativeSetString(long nativeRowPtr, long columnIndex, String value);

    public void setBinaryByteArray(long columnIndex, byte[] data) {
        if (data == null)
            throw new IllegalArgumentException("Null Array");
        nativeSetByteArray(nativePtr, columnIndex, data);
    }

    protected native void nativeSetByteArray(long nativePtr, long columnIndex, byte[] data);


    public void setMixed(long columnIndex, Mixed data) {
        if (data == null)
            throw new IllegalArgumentException();
        nativeSetMixed(nativePtr, columnIndex, data);
    }

    protected native void nativeSetMixed(long nativeRowPtr, long columnIndex, Mixed data);

    private void throwImmutable() {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }

    protected static native void nativeClose(long nativeRowPtr);

    @Override
    protected void finalize() {
        nativeClose(nativePtr);
        nativePtr = 0;
    }

}
