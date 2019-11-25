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

    protected final NativeContext context; // This is only kept because for now it's needed by the constructor of LinkView
    protected final Table parent;
    private final long nativePtr;

    public UncheckedRow(NativeContext context, Table parent, long nativePtr) {
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
     * Gets the row object associated with a row key in a Table.
     *
     * @param context the Realm context.
     * @param table the Table that holds the row.
     * @param rowKey Row key.
     * @return an instance of Row for the table and row key specified.
     */
    static UncheckedRow getByRowKey(NativeContext context, Table table, long rowKey) {
        long nativeRowPointer = table.nativeGetRowPtr(table.getNativePtr(), rowKey);
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
    public String[] getColumnNames() {
        return nativeGetColumnNames(nativePtr);
    }

    @Override
    public long getColumnKey(String columnName) {
        //noinspection ConstantConditions
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        return nativeGetColumnKey(nativePtr, columnName);
    }

    @Override
    public RealmFieldType getColumnType(long columnKey) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativePtr, columnKey));
    }

    // Getters

    @Override
    public Table getTable() {
        return parent;
    }

    @Override
    public long getObjectKey() {
        return nativeGetObjectKey(nativePtr);
    }

    @Override
    public long getLong(long columnKey) {
        return nativeGetLong(nativePtr, columnKey);
    }

    @Override
    public boolean getBoolean(long columnKey) {
        return nativeGetBoolean(nativePtr, columnKey);
    }

    @Override
    public float getFloat(long columnKey) {
        return nativeGetFloat(nativePtr, columnKey);
    }

    @Override
    public double getDouble(long columnKey) {
        return nativeGetDouble(nativePtr, columnKey);
    }

    @Override
    public Date getDate(long columnKey) {
        return new Date(nativeGetTimestamp(nativePtr, columnKey));
    }

    @Override
    public String getString(long columnKey) {
        return nativeGetString(nativePtr, columnKey);
    }

    @Override
    public byte[] getBinaryByteArray(long columnKey) {
        return nativeGetByteArray(nativePtr, columnKey);
    }

    @Override
    public long getLink(long columnKey) {
        return nativeGetLink(nativePtr, columnKey);
    }

    @Override
    public boolean isNullLink(long columnKey) {
        return nativeIsNullLink(nativePtr, columnKey);
    }

    @Override
    public OsList getModelList(long columnKey) {
        return new OsList(this, columnKey);
    }

    @Override
    public OsList getValueList(long columnKey, RealmFieldType fieldType) {
        return new OsList(this, columnKey);
    }

    // Setters

    @Override
    public void setLong(long columnKey, long value) {
        parent.checkImmutable();
        nativeSetLong(nativePtr, columnKey, value);
    }

    @Override
    public void setBoolean(long columnKey, boolean value) {
        parent.checkImmutable();
        nativeSetBoolean(nativePtr, columnKey, value);
    }

    @Override
    public void setFloat(long columnKey, float value) {
        parent.checkImmutable();
        nativeSetFloat(nativePtr, columnKey, value);
    }

    @Override
    public void setDouble(long columnKey, double value) {
        parent.checkImmutable();
        nativeSetDouble(nativePtr, columnKey, value);
    }

    @Override
    public void setDate(long columnKey, Date date) {
        parent.checkImmutable();
        //noinspection ConstantConditions
        if (date == null) {
            throw new IllegalArgumentException("Null Date is not allowed.");
        }
        long timestamp = date.getTime();
        nativeSetTimestamp(nativePtr, columnKey, timestamp);
    }

    /**
     * Sets a string value to a row pointer.
     *
     * @param columnKey column key.
     * @param value the value to to a row
     */
    @Override
    public void setString(long columnKey, @Nullable String value) {
        parent.checkImmutable();
        if (value == null) {
            nativeSetNull(nativePtr, columnKey);
        } else {
            nativeSetString(nativePtr, columnKey, value);
        }
    }

    @Override
    public void setBinaryByteArray(long columnKey, @Nullable byte[] data) {
        parent.checkImmutable();
        nativeSetByteArray(nativePtr, columnKey, data);
    }

    @Override
    public void setLink(long columnKey, long value) {
        parent.checkImmutable();
        nativeSetLink(nativePtr, columnKey, value);
    }

    @Override
    public void nullifyLink(long columnKey) {
        parent.checkImmutable();
        nativeNullifyLink(nativePtr, columnKey);
    }

    @Override
    public boolean isNull(long columnKey) {
        return nativeIsNull(nativePtr, columnKey);
    }

    /**
     * Sets null to a row pointer.
     *
     * @param columnKey column key.
     */
    @Override
    public void setNull(long columnKey) {
        parent.checkImmutable();
        nativeSetNull(nativePtr, columnKey);
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
    public boolean isValid() {
        return nativePtr != 0 && nativeIsValid(nativePtr);
    }

    @Override
    public void checkIfAttached() {
        if (!isValid()) {
            throw new IllegalStateException("Object is no longer managed by Realm. Has it been deleted?");
        }
    }

    @Override
    public boolean hasColumn(String fieldName) {
        return nativeHasColumn(nativePtr, fieldName);
    }

    @Override
    public Row freeze(OsSharedRealm frozenRealm) {
        if (!isValid()) {
            return InvalidRow.INSTANCE;
        }
        return new UncheckedRow(context, parent.freeze(frozenRealm), nativeFreeze(nativePtr, frozenRealm.getNativePtr()));
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    protected native long nativeGetColumnCount(long nativeTablePtr);

    protected native long nativeGetColumnKey(long nativeTablePtr, String columnName);

    protected native String[] nativeGetColumnNames(long nativeTablePtr);

    protected native int nativeGetColumnType(long nativeTablePtr, long columnKey);

    protected native long nativeGetObjectKey(long nativeRowPtr);

    protected native long nativeGetLong(long nativeRowPtr, long columnKey);

    protected native boolean nativeGetBoolean(long nativeRowPtr, long columnKey);

    protected native float nativeGetFloat(long nativeRowPtr, long columnKey);

    protected native double nativeGetDouble(long nativeRowPtr, long columnKey);

    protected native long nativeGetTimestamp(long nativeRowPtr, long columnKey);

    protected native String nativeGetString(long nativePtr, long columnKey);

    protected native boolean nativeIsNullLink(long nativeRowPtr, long columnKey);

    protected native byte[] nativeGetByteArray(long nativePtr, long columnKey);

    protected native void nativeSetLong(long nativeRowPtr, long columnKey, long value);

    protected native void nativeSetBoolean(long nativeRowPtr, long columnKey, boolean value);

    protected native void nativeSetFloat(long nativeRowPtr, long columnKey, float value);

    protected native long nativeGetLink(long nativeRowPtr, long columnKey);

    protected native void nativeSetDouble(long nativeRowPtr, long columnKey, double value);

    protected native void nativeSetTimestamp(long nativeRowPtr, long columnKey, long dateTimeValue);

    protected native void nativeSetString(long nativeRowPtr, long columnKey, String value);

    protected native void nativeSetByteArray(long nativePtr, long columnKey, @Nullable byte[] data);

    protected native void nativeSetLink(long nativeRowPtr, long columnKey, long value);

    protected native void nativeNullifyLink(long nativeRowPtr, long columnKey);

    protected native boolean nativeIsValid(long nativeRowPtr);

    protected native boolean nativeHasColumn(long nativeRowPtr, String columnName);

    protected native boolean nativeIsNull(long nativeRowPtr, long columnKey);

    protected native void nativeSetNull(long nativeRowPtr, long columnKey);

    protected native long nativeFreeze(long nativeRowPtr, long frozenRealmNativePtr);

    private static native long nativeGetFinalizerPtr();
}
