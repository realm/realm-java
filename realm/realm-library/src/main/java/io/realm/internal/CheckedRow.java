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

import java.util.Locale;

import io.realm.RealmFieldType;


/**
 * Checked wrapper for Row data in Realm Core. All methods called through this will check that input parameters are
 * valid or throw an appropriate exception.
 * <p>
 * For low-level access to a Realm where safety checks were already performed, use {@link UncheckedRow} instead for
 * improved performance.
 */
public class CheckedRow extends UncheckedRow {

    // Used if created from other row. This keeps a strong reference to avoid GC'ing the original object, and its
    // underlying native data.
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private UncheckedRow originalRow;

    private CheckedRow(NativeContext context, Table parent, long nativePtr) {
        super(context, parent, nativePtr);
    }

    public CheckedRow(UncheckedRow row) {
        super(row);
        this.originalRow = row;
    }

    /**
     * Gets the row object associated to an index in a {@link Table}.
     *
     * @param context the Realm context.
     * @param table the {@link Table} that holds the row.
     * @param objKey the object key.
     * @return an instance of Row for the table and index specified.
     */
    public static CheckedRow get(NativeContext context, Table table, long objKey) {
        long nativeRowPointer = table.nativeGetRowPtr(table.getNativePtr(), objKey);
        return new CheckedRow(context, table, nativeRowPointer);
    }

    /**
     * Converts a {@link UncheckedRow} to a {@link CheckedRow}.
     *
     * @return an checked instance of {@link Row}.
     */
    public static CheckedRow getFromRow(UncheckedRow row) {
        return new CheckedRow(row);
    }

    @Override
    public boolean isNullLink(long columnIndex) {
        RealmFieldType columnType = getColumnType(columnIndex);
        if (columnType == RealmFieldType.OBJECT || columnType == RealmFieldType.LIST) {
            return super.isNullLink(columnIndex);
        } else {
            return false; // Unsupported types always return false
        }
    }

    @Override
    public boolean isNull(long columnIndex) {
        return super.isNull(columnIndex);
    }

    /**
     * Sets null to a row pointer with checking if a column is nullable, except when the column type
     * is binary.
     *
     * @param columnIndex 0 based index value of the cell column.
     */
    @Override
    public void setNull(long columnIndex) {
        RealmFieldType columnType = getColumnType(columnIndex);
        if (columnType == RealmFieldType.BINARY) {
            super.setBinaryByteArray(columnIndex, null);
        } else {
            super.setNull(columnIndex);
        }
    }

    @Override
    public OsList getModelList(long columnIndex) {
        RealmFieldType fieldType = getTable().getColumnType(columnIndex);
        if (fieldType != RealmFieldType.LIST) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Field '%s' is not a 'RealmList'.",
                            getTable().getColumnName(columnIndex)));
        }
        return super.getModelList(columnIndex);
    }

    @Override
    public OsList getValueList(long columnIndex, RealmFieldType fieldType) {
        final RealmFieldType actualFieldType = getTable().getColumnType(columnIndex);
        if (fieldType != actualFieldType) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "The type of field '%1$s' is not 'RealmFieldType.%2$s'.",
                            getTable().getColumnName(columnIndex), fieldType.name()));
        }
        return super.getValueList(columnIndex, fieldType);
    }

    @Override
    public OsMap getRealmAnyMap(long columnIndex) {
        RealmFieldType fieldType = getTable().getColumnType(columnIndex);
        if (fieldType != RealmFieldType.STRING_TO_MIXED_MAP) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Field '%s' is not a 'RealmDictionary'.",
                            getTable().getColumnName(columnIndex)));
        }
        return super.getRealmAnyMap(columnIndex);
    }

    @Override
    public OsMap getModelMap(long columnIndex) {
        RealmFieldType fieldType = getTable().getColumnType(columnIndex);
        if (fieldType != RealmFieldType.STRING_TO_LINK_MAP) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Field '%s' is not a 'RealmDictionary'.",
                            getTable().getColumnName(columnIndex)));
        }
        return super.getRealmAnyMap(columnIndex);
    }

    @Override
    public OsMap getValueMap(long columnIndex, RealmFieldType fieldType) {
        final RealmFieldType actualFieldType = getTable().getColumnType(columnIndex);
        if (fieldType != actualFieldType) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "The type of field '%1$s' is not 'RealmFieldType.%2$s'.",
                            getTable().getColumnName(columnIndex), fieldType.name()));
        }
        return super.getValueMap(columnIndex, fieldType);
    }

    @Override
    public OsSet getRealmAnySet(long columnIndex) {
        return super.getModelSet(columnIndex);
    }

    @Override
    public OsSet getModelSet(long columnIndex) {
        return super.getModelSet(columnIndex);
    }

    @Override
    public OsSet getValueSet(long columnIndex, RealmFieldType fieldType) {
        final RealmFieldType actualFieldType = getTable().getColumnType(columnIndex);
        if (fieldType != actualFieldType) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "The type of field '%1$s' is not 'RealmFieldType.%2$s'.",
                            getTable().getColumnName(columnIndex), fieldType.name()));
        }
        return super.getValueSet(columnIndex, fieldType);
    }

    @Override
    public Row freeze(OsSharedRealm frozenRealm) {
        if (!isValid()) {
            return InvalidRow.INSTANCE;
        }
        return new CheckedRow(context, parent.freeze(frozenRealm), nativeFreeze(getNativePtr(), frozenRealm.getNativePtr()));
    }

    @Override
    protected native long nativeGetColumnCount(long nativeTablePtr);

    @Override
    protected native long nativeGetColumnKey(long nativeTablePtr, String columnName);

    @Override
    protected native int nativeGetColumnType(long nativeTablePtr, long columnIndex);

    @Override
    protected native long nativeGetLong(long nativeRowPtr, long columnIndex);

    @Override
    protected native boolean nativeGetBoolean(long nativeRowPtr, long columnIndex);

    @Override
    protected native float nativeGetFloat(long nativeRowPtr, long columnIndex);

    @Override
    protected native double nativeGetDouble(long nativeRowPtr, long columnIndex);

    @Override
    protected native long nativeGetTimestamp(long nativeRowPtr, long columnIndex);

    @Override
    protected native String nativeGetString(long nativePtr, long columnIndex);

    @Override
    protected native boolean nativeIsNullLink(long nativeRowPtr, long columnIndex);

    @Override
    protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex);

    @Override
    protected native void nativeSetLong(long nativeRowPtr, long columnIndex, long value);

    @Override
    protected native void nativeSetBoolean(long nativeRowPtr, long columnIndex, boolean value);

    @Override
    protected native void nativeSetFloat(long nativeRowPtr, long columnIndex, float value);

    @Override
    protected native long nativeGetLink(long nativeRowPtr, long columnIndex);

    @Override
    protected native void nativeSetDouble(long nativeRowPtr, long columnIndex, double value);

    @Override
    protected native void nativeSetTimestamp(long nativeRowPtr, long columnIndex, long dateTimeValue);

    @Override
    protected native void nativeSetString(long nativeRowPtr, long columnIndex, String value);

    @Override
    protected native void nativeSetByteArray(long nativePtr, long columnIndex, byte[] data);

    @Override
    protected native void nativeSetLink(long nativeRowPtr, long columnIndex, long value);

    @Override
    protected native void nativeNullifyLink(long nativeRowPtr, long columnIndex);

    @Override
    protected native long[] nativeGetDecimal128(long nativePtr, long columnKey);

    @Override
    protected native String nativeGetObjectId(long nativePtr, long columnKey);

    @Override
    protected native void nativeSetDecimal128(long nativePtr, long columnKey, long low, long high);

    @Override
    protected native void nativeSetObjectId(long nativePtr, long columnKey, String value);
}
