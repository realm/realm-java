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

import io.realm.RealmFieldType;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;


/**
 * This class is a base class for all Realm tables. The class supports all low level methods
 * (define/insert/delete/update) a table has. All the native communications to the Realm C++ library are also handled by
 * this class.
 */
public class Table implements TableSchema, NativeObject {

    enum PivotType {
        COUNT(0),
        SUM(1),
        AVG(2),
        MIN(3),
        MAX(4);

        final int value; // Package protected, accessible from Table

        PivotType(int value) {
            this.value = value;
        }
    }

    public static final int TABLE_MAX_LENGTH = 56; // Max length of class names without prefix
    public static final String TABLE_PREFIX = Util.getTablePrefix();
    public static final long INFINITE = -1;
    public static final boolean NULLABLE = true;
    public static final boolean NOT_NULLABLE = false;
    public static final int NO_MATCH = -1;

    private static final String PRIMARY_KEY_TABLE_NAME = "pk";
    private static final String PRIMARY_KEY_CLASS_COLUMN_NAME = "pk_table";
    private static final long PRIMARY_KEY_CLASS_COLUMN_INDEX = 0;
    private static final String PRIMARY_KEY_FIELD_COLUMN_NAME = "pk_property";
    private static final long PRIMARY_KEY_FIELD_COLUMN_INDEX = 1;
    private static final long NO_PRIMARY_KEY = -2;

    private long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    final Context context;
    private final SharedRealm sharedRealm;
    private long cachedPrimaryKeyColumnIndex = NO_MATCH;

    /**
     * Constructs a Table base object. It can be used to register columns in this table. Registering into table is
     * allowed only for empty tables. It creates a native reference of the object and keeps a reference to it.
     */
    public Table() {
        this.context = new Context();
        // Native methods work will be initialized here. Generated classes will
        // have nothing to do with the native functions. Generated Java Table
        // classes will work as a wrapper on top of table.
        this.nativePtr = createNative();
        if (nativePtr == 0) {
            throw new java.lang.OutOfMemoryError("Out of native memory.");
        }
        this.sharedRealm = null;
        context.addReference(this);
    }

    Table(Table parent, long nativePointer) {
        this(parent.sharedRealm, nativePointer);
    }

    Table(SharedRealm sharedRealm, long nativePointer) {
        this.context = sharedRealm.context;
        this.sharedRealm = sharedRealm;
        this.nativePtr = nativePointer;
        context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public long getNativeTablePointer() {
        return nativePtr;
    }

    /*
     * Checks if the Table is valid.
     * Whenever a Table/subtable is changed/updated all it's subtables are invalidated.
     * You can no longer perform any actions on the table, and if done anyway, an exception is thrown.
     * The only method you can call is 'isValid()'.
     */
    public boolean isValid() {
        return nativePtr != 0 && nativeIsValid(nativePtr);
    }

    private void verifyColumnName(String name) {
        if (name.length() > 63) {
            throw new IllegalArgumentException("Column names are currently limited to max 63 characters.");
        }
    }

    /**
     * Adds a column to the table dynamically.
     *
     * @param type the column type.
     * @param name the field/column name.
     * @param isNullable {@code true} if column can contain null values, {@code false} otherwise.
     * @return the index of the new column.
     */
    public long addColumn(RealmFieldType type, String name, boolean isNullable) {
        verifyColumnName(name);
        return nativeAddColumn(nativePtr, type.getNativeValue(), name, isNullable);
    }

    /**
     * Adds a non-nullable column to the table dynamically.
     *
     * @return the index of the new column.
     */
    @Override
    public long addColumn(RealmFieldType type, String name) {
        return addColumn(type, name, false);
    }

    /**
     * Adds a link column to the table dynamically.
     *
     * @return the index of the new column.
     */
    public long addColumnLink(RealmFieldType type, String name, Table table) {
        verifyColumnName(name);
        return nativeAddColumnLink(nativePtr, type.getNativeValue(), name, table.nativePtr);
    }

    /**
     * Removes a column in the table dynamically. If {@code columnIndex} is smaller than the primary
     * key column index, {@link #invalidateCachedPrimaryKeyIndex()} will be called to recalculate the
     * primary key column index.
     * <p>
     * <p>It should be noted if {@code columnIndex} is the same as the primary key column index,
     * the primary key column is removed from the meta table.
     *
     * @param columnIndex the column index to be removed.
     */
    @Override
    public void removeColumn(long columnIndex) {
        // Checks the PK column index before removing a column. We don't know if we're hitting a PK col,
        // but it should be noted that once a column is removed, there is no way we can find whether
        // a PK exists or not.
        final long oldPkColumnIndex = getPrimaryKey();

        // First removes a column. If there is no error, we can proceed. Otherwise, it will stop here.
        nativeRemoveColumn(nativePtr, columnIndex);

        // Checks if a PK exists and takes actions if there is. This is same as hasPrimaryKey(), but
        // this relies on the local cache.
        if (oldPkColumnIndex >= 0) {

            // In case we're hitting PK column, we should remove the PK as it is either 1) a user has
            // forgotten to remove PK or 2) removeColumn gets called before setPrimaryKey(null) is called.
            // Since there is no danger in removing PK twice, we'll do it here to be on safe side.
            if (oldPkColumnIndex == columnIndex) {
                setPrimaryKey(null);

                // But if you remove a column with a smaller index than that of PK column, you need to
                // recalculate the PK column index as core could have changed its column index.
            } else if (oldPkColumnIndex > columnIndex) {
                invalidateCachedPrimaryKeyIndex();
            }
        }
    }

    /**
     * Renames a column in the table. If the column is a primary key column, the corresponding entry
     * in PrimaryKeyTable will be renamed accordingly.
     *
     * @param columnIndex the column index to be renamed.
     * @param newName a new name replacing the old column name.
     * @throws IllegalArgumentException if {@code newFieldName} is an empty string, or exceeds field name length limit.
     * @throws IllegalStateException if a PrimaryKey column name could not be found in the meta table, but {@link #getPrimaryKey()} returns an index.
     */
    @Override
    public void renameColumn(long columnIndex, String newName) {
        verifyColumnName(newName);
        // Gets the old column name. We'll assume that the old column name is *NOT* an empty string.
        final String oldName = nativeGetColumnName(nativePtr, columnIndex);
        // Also old pk index. Once a column name changes, there is no way you can find the column name
        // by old name.
        final long oldPkColumnIndex = getPrimaryKey();

        // Then let's try to rename a column. If an error occurs for some reasons, we'll throw.
        nativeRenameColumn(nativePtr, columnIndex, newName);

        // Renames a primary key. At this point, renaming the column name should have been fine.
        if (oldPkColumnIndex == columnIndex) {
            try {
                String className = tableNameToClassName(getName());
                Table pkTable = getPrimaryKeyTable();
                if (pkTable == null) {
                    throw new IllegalStateException(
                            "Table is not created from a SharedRealm, primary key is not available");
                }
                long pkRowIndex = pkTable.findFirstString(PRIMARY_KEY_CLASS_COLUMN_INDEX, className);
                if (pkRowIndex != NO_MATCH) {
                    nativeSetString(pkTable.nativePtr, PRIMARY_KEY_FIELD_COLUMN_INDEX, pkRowIndex, newName, false);
                } else {
                    throw new IllegalStateException("Non-existent PrimaryKey column cannot be renamed");
                }
            } catch (Exception e) {
                // We failed to rename the pk meta table. roll back the column name, not pk meta table
                // then rethrow.
                nativeRenameColumn(nativePtr, columnIndex, oldName);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks whether the specific column is nullable?
     *
     * @param columnIndex the column index.
     * @return {@code true} if column is nullable, {@code false} otherwise.
     */
    public boolean isColumnNullable(long columnIndex) {
        return nativeIsColumnNullable(nativePtr, columnIndex);
    }

    /**
     * Converts a column to be nullable.
     *
     * @param columnIndex the column index.
     */
    public void convertColumnToNullable(long columnIndex) {
        nativeConvertColumnToNullable(nativePtr, columnIndex);
    }

    /**
     * Converts a column to be not nullable. null values will be converted to default values.
     *
     * @param columnIndex the column index.
     */
    public void convertColumnToNotNullable(long columnIndex) {
        nativeConvertColumnToNotNullable(nativePtr, columnIndex);
    }

    // Table Size and deletion. AutoGenerated subclasses are nothing to do with this
    // class.

    /**
     * Gets the number of entries/rows of this table.
     *
     * @return the number of rows.
     */
    public long size() {
        return nativeSize(nativePtr);
    }

    /**
     * Checks whether this table is empty or not.
     *
     * @return {@code true} if empty, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Clears the table i.e., deleting all rows in the table.
     */
    public void clear() {
        checkImmutable();
        nativeClear(nativePtr);
    }

    // Column Information.

    /**
     * Returns the number of columns in the table.
     *
     * @return the number of columns.
     */
    public long getColumnCount() {
        return nativeGetColumnCount(nativePtr);
    }

    /**
     * Returns the name of a column identified by columnIndex. Notice that the index is zero based.
     *
     * @param columnIndex the column index.
     * @return the name of the column.
     */
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePtr, columnIndex);
    }

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name.
     * @return the index, {@link #NO_MATCH} if not found.
     */
    public long getColumnIndex(String columnName) {
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        return nativeGetColumnIndex(nativePtr, columnName);
    }

    /**
     * Gets the type of a column identified by the columnIndex.
     *
     * @param columnIndex index of the column.
     * @return the type of the particular column.
     */
    public RealmFieldType getColumnType(long columnIndex) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativePtr, columnIndex));
    }

    /**
     * Removes a row from the specific index. If it is not the last row in the table, it then moves the last row into
     * the vacated slot.
     *
     * @param rowIndex the row index (starting with 0)
     */
    public void moveLastOver(long rowIndex) {
        checkImmutable();
        nativeMoveLastOver(nativePtr, rowIndex);
    }

    /**
     * Adds an empty row to the table which doesn't have a primary key defined.
     * <p>
     * NOTE: To add a table with a primary key defined, use {@link #addEmptyRowWithPrimaryKey(Object)} instead. This
     * won't check if this table has a primary key.
     *
     * @return row index.
     */
    public long addEmptyRow() {
        checkImmutable();
        return nativeAddEmptyRow(nativePtr, 1);
    }

    /**
     * Adds an empty row to the table and set the primary key with the given value. Equivalent to call
     * {@link #addEmptyRowWithPrimaryKey(Object, boolean)} with {@code validation = true}.
     *
     * @param primaryKeyValue the primary key value
     * @return the row index.
     */
    public long addEmptyRowWithPrimaryKey(Object primaryKeyValue) {
        return addEmptyRowWithPrimaryKey(primaryKeyValue, true);
    }

    /**
     * Adds an empty row to the table and set the primary key with the given value.
     *
     * @param primaryKeyValue the primary key value.
     * @param validation set to {@code false} to skip all validations. This is currently used by bulk insert which
     * has its own validations.
     * @return the row index.
     */
    public long addEmptyRowWithPrimaryKey(Object primaryKeyValue, boolean validation) {
        if (validation) {
            checkImmutable();
            checkHasPrimaryKey();
        }

        long primaryKeyColumnIndex = getPrimaryKey();
        RealmFieldType type = getColumnType(primaryKeyColumnIndex);
        long rowIndex;

        // Adds with primary key initially set.
        if (primaryKeyValue == null) {
            switch (type) {
                case STRING:
                case INTEGER:
                    if (validation && findFirstNull(primaryKeyColumnIndex) != NO_MATCH) {
                        throwDuplicatePrimaryKeyException("null");
                    }
                    rowIndex = nativeAddEmptyRow(nativePtr, 1);
                    if (type == RealmFieldType.STRING) {
                        nativeSetStringUnique(nativePtr, primaryKeyColumnIndex, rowIndex, null);
                    } else {
                        nativeSetNullUnique(nativePtr, primaryKeyColumnIndex, rowIndex);
                    }
                    break;

                default:
                    throw new RealmException("Cannot check for duplicate rows for unsupported primary key type: " + type);
            }

        } else {
            switch (type) {
                case STRING:
                    if (!(primaryKeyValue instanceof String)) {
                        throw new IllegalArgumentException("Primary key value is not a String: " + primaryKeyValue);
                    }
                    if (validation && findFirstString(primaryKeyColumnIndex, (String) primaryKeyValue) != NO_MATCH) {
                        throwDuplicatePrimaryKeyException(primaryKeyValue);
                    }
                    rowIndex = nativeAddEmptyRow(nativePtr, 1);
                    nativeSetStringUnique(nativePtr, primaryKeyColumnIndex, rowIndex, (String) primaryKeyValue);
                    break;

                case INTEGER:
                    long pkValue;
                    try {
                        pkValue = Long.parseLong(primaryKeyValue.toString());
                    } catch (RuntimeException e) {
                        throw new IllegalArgumentException("Primary key value is not a long: " + primaryKeyValue);
                    }
                    if (validation && findFirstLong(primaryKeyColumnIndex, pkValue) != NO_MATCH) {
                        throwDuplicatePrimaryKeyException(pkValue);
                    }
                    rowIndex = nativeAddEmptyRow(nativePtr, 1);
                    nativeSetLongUnique(nativePtr, primaryKeyColumnIndex, rowIndex, pkValue);
                    break;

                default:
                    throw new RealmException("Cannot check for duplicate rows for unsupported primary key type: " + type);
            }
        }
        return rowIndex;
    }

    @SuppressWarnings("WeakerAccess")
    public long addEmptyRows(long rows) {
        checkImmutable();
        if (rows < 1) {
            throw new IllegalArgumentException("'rows' must be > 0.");
        }
        if (hasPrimaryKey()) {
            if (rows > 1) {
                throw new RealmException("Multiple empty rows cannot be created if a primary key is defined for the table.");
            }
            return addEmptyRow();
        }
        return nativeAddEmptyRow(nativePtr, rows);
    }

    /**
     * Appends the specified row to the end of the table. For internal testing usage only.
     *
     * @param values values.
     * @return the row index of the appended row.
     * @deprecated Remove this functions since it doesn't seem to be useful. And this function does deal with tables
     * with primary key defined well. Primary key has to be set with `setXxxUnique` as the first thing to do after row
     * added.
     */
    protected long add(Object... values) {
        long rowIndex = addEmptyRow();

        checkImmutable();

        // Checks values types.
        int columns = (int) getColumnCount();
        if (columns != values.length) {
            throw new IllegalArgumentException("The number of value parameters (" +
                    String.valueOf(values.length) +
                    ") does not match the number of columns in the table (" +
                    String.valueOf(columns) + ").");
        }
        RealmFieldType colTypes[] = new RealmFieldType[columns];
        for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
            Object value = values[columnIndex];
            RealmFieldType colType = getColumnType(columnIndex);
            colTypes[columnIndex] = colType;
            if (!colType.isValid(value)) {
                // String representation of the provided value type.
                String providedType;
                if (value == null) {
                    providedType = "null";
                } else {
                    providedType = value.getClass().toString();
                }

                throw new IllegalArgumentException("Invalid argument no " + String.valueOf(1 + columnIndex) +
                        ". Expected a value compatible with column type " + colType + ", but got " + providedType + ".");
            }
        }

        // Inserts values.
        for (long columnIndex = 0; columnIndex < columns; columnIndex++) {
            Object value = values[(int) columnIndex];
            switch (colTypes[(int) columnIndex]) {
                case BOOLEAN:
                    nativeSetBoolean(nativePtr, columnIndex, rowIndex, (Boolean) value, false);
                    break;
                case INTEGER:
                    if (value == null) {
                        checkDuplicatedNullForPrimaryKeyValue(columnIndex, rowIndex);
                        nativeSetNull(nativePtr, columnIndex, rowIndex, false);
                    } else {
                        long intValue = ((Number) value).longValue();
                        checkIntValueIsLegal(columnIndex, rowIndex, intValue);
                        nativeSetLong(nativePtr, columnIndex, rowIndex, intValue, false);
                    }
                    break;
                case FLOAT:
                    nativeSetFloat(nativePtr, columnIndex, rowIndex, (Float) value, false);
                    break;
                case DOUBLE:
                    nativeSetDouble(nativePtr, columnIndex, rowIndex, (Double) value, false);
                    break;
                case STRING:
                    if (value == null) {
                        checkDuplicatedNullForPrimaryKeyValue(columnIndex, rowIndex);
                        nativeSetNull(nativePtr, columnIndex, rowIndex, false);
                    } else {
                        String stringValue = (String) value;
                        checkStringValueIsLegal(columnIndex, rowIndex, stringValue);
                        nativeSetString(nativePtr, columnIndex, rowIndex, (String) value, false);
                    }
                    break;
                case DATE:
                    if (value == null) { throw new IllegalArgumentException("Null Date is not allowed."); }
                    nativeSetTimestamp(nativePtr, columnIndex, rowIndex, ((Date) value).getTime(), false);
                    break;
                case BINARY:
                    if (value == null) { throw new IllegalArgumentException("Null Array is not allowed"); }
                    nativeSetByteArray(nativePtr, columnIndex, rowIndex, (byte[]) value, false);
                    break;
                case UNSUPPORTED_MIXED:
                case UNSUPPORTED_TABLE:
                default:
                    throw new RuntimeException("Unexpected columnType: " + String.valueOf(colTypes[(int) columnIndex]));
            }
        }
        return rowIndex;
    }

    private boolean isPrimaryKeyColumn(long columnIndex) {
        return columnIndex == getPrimaryKey();
    }

    /**
     * Returns the column index for the primary key.
     *
     * @return the column index or {@code #NO_MATCH} if no primary key is set.
     */
    public long getPrimaryKey() {
        if (cachedPrimaryKeyColumnIndex >= 0 || cachedPrimaryKeyColumnIndex == NO_PRIMARY_KEY) {
            return cachedPrimaryKeyColumnIndex;
        } else {
            Table pkTable = getPrimaryKeyTable();
            if (pkTable == null) {
                return NO_PRIMARY_KEY; // Free table = No primary key.
            }

            String className = tableNameToClassName(getName());
            long rowIndex = pkTable.findFirstString(PRIMARY_KEY_CLASS_COLUMN_INDEX, className);
            if (rowIndex != NO_MATCH) {
                String pkColumnName = pkTable.getUncheckedRow(rowIndex).getString(PRIMARY_KEY_FIELD_COLUMN_INDEX);
                cachedPrimaryKeyColumnIndex = getColumnIndex(pkColumnName);
            } else {
                cachedPrimaryKeyColumnIndex = NO_PRIMARY_KEY;
            }

            return cachedPrimaryKeyColumnIndex;
        }
    }

    /**
     * Checks if a given column is a primary key column.
     *
     * @param columnIndex the index of column in the table.
     * @return {@code true} if column is a primary key, {@code false} otherwise.
     */
    private boolean isPrimaryKey(long columnIndex) {
        return columnIndex >= 0 && columnIndex == getPrimaryKey();
    }

    /**
     * Checks if a table has a primary key.
     *
     * @return {@code true} if primary key is defined, {@code false} otherwise.
     */
    public boolean hasPrimaryKey() {
        return getPrimaryKey() >= 0;
    }

    void checkStringValueIsLegal(long columnIndex, long rowToUpdate, String value) {
        if (isPrimaryKey(columnIndex)) {
            long rowIndex = findFirstString(columnIndex, value);
            if (rowIndex != rowToUpdate && rowIndex != NO_MATCH) {
                throwDuplicatePrimaryKeyException(value);
            }
        }
    }

    void checkIntValueIsLegal(long columnIndex, long rowToUpdate, long value) {
        if (isPrimaryKeyColumn(columnIndex)) {
            long rowIndex = findFirstLong(columnIndex, value);
            if (rowIndex != rowToUpdate && rowIndex != NO_MATCH) {
                throwDuplicatePrimaryKeyException(value);
            }
        }
    }

    // Checks if it is ok to use null value for given row and column.
    void checkDuplicatedNullForPrimaryKeyValue(long columnIndex, long rowToUpdate) {
        if (isPrimaryKeyColumn(columnIndex)) {
            RealmFieldType type = getColumnType(columnIndex);
            switch (type) {
                case STRING:
                case INTEGER:
                    long rowIndex = findFirstNull(columnIndex);
                    if (rowIndex != rowToUpdate && rowIndex != NO_MATCH) {
                        throwDuplicatePrimaryKeyException("null");
                    }
                    break;
                default:
                    // Since it is sufficient to check the existence of duplicated null values
                    // on PrimaryKey in supported types only, this part is left empty.
            }
        }
    }

    /**
     * Throws a properly formatted exception when multiple objects with the same primary key
     * value is detected.
     *
     * @param value the primary key value.
     */
    public static void throwDuplicatePrimaryKeyException(Object value) {
        throw new RealmPrimaryKeyConstraintException("Value already exists: " + value);
    }

    //
    // Getters
    //

    public long getLong(long columnIndex, long rowIndex) {
        return nativeGetLong(nativePtr, columnIndex, rowIndex);
    }

    public boolean getBoolean(long columnIndex, long rowIndex) {
        return nativeGetBoolean(nativePtr, columnIndex, rowIndex);
    }

    public float getFloat(long columnIndex, long rowIndex) {
        return nativeGetFloat(nativePtr, columnIndex, rowIndex);
    }

    public double getDouble(long columnIndex, long rowIndex) {
        return nativeGetDouble(nativePtr, columnIndex, rowIndex);
    }

    public Date getDate(long columnIndex, long rowIndex) {
        return new Date(nativeGetTimestamp(nativePtr, columnIndex, rowIndex));
    }

    /**
     * Gets the value of a (string) cell.
     *
     * @param columnIndex 0 based index value of the column
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell
     */
    public String getString(long columnIndex, long rowIndex) {
        return nativeGetString(nativePtr, columnIndex, rowIndex);
    }

    public byte[] getBinaryByteArray(long columnIndex, long rowIndex) {
        return nativeGetByteArray(nativePtr, columnIndex, rowIndex);
    }

    public long getLink(long columnIndex, long rowIndex) {
        return nativeGetLink(nativePtr, columnIndex, rowIndex);
    }

    public Table getLinkTarget(long columnIndex) {
        long nativeTablePointer = nativeGetLinkTarget(nativePtr, columnIndex);
        // Copies context reference from parent.
        return new Table(this.sharedRealm, nativeTablePointer);
    }

    public boolean isNull(long columnIndex, long rowIndex) {
        return nativeIsNull(nativePtr, columnIndex, rowIndex);
    }

    /**
     * Returns a non-checking Row. Incorrect use of this Row will cause a hard core crash.
     * If error checking is required, use {@link #getCheckedRow(long)} instead.
     *
     * @param index the index of row to fetch.
     * @return the unsafe row wrapper object.
     */
    public UncheckedRow getUncheckedRow(long index) {
        return UncheckedRow.getByRowIndex(context, this, index);
    }

    /**
     * Returns a non-checking Row. Incorrect use of this Row will cause a hard core crash.
     * If error checking is required, use {@link #getCheckedRow(long)} instead.
     *
     * @param nativeRowPointer the pointer to the row to fetch.
     * @return the unsafe row wrapper object.
     */
    public UncheckedRow getUncheckedRowByPointer(long nativeRowPointer) {
        return UncheckedRow.getByRowPointer(context, this, nativeRowPointer);
    }

    /**
     * Returns a wrapper around Row access. All access will be error checked in JNI and will throw an appropriate
     * {@link RuntimeException} if used incorrectly.
     * <p>
     * If error checking is done elsewhere, consider using {@link #getUncheckedRow(long)} for better performance.
     *
     * @param index the index of row to fetch.
     * @return the safe row wrapper object.
     */
    public CheckedRow getCheckedRow(long index) {
        return CheckedRow.get(context, this, index);
    }

    //
    // Setters
    //

    public void setLong(long columnIndex, long rowIndex, long value, boolean isDefault) {
        checkImmutable();
        checkIntValueIsLegal(columnIndex, rowIndex, value);
        nativeSetLong(nativePtr, columnIndex, rowIndex, value, isDefault);
    }

    public void setBoolean(long columnIndex, long rowIndex, boolean value, boolean isDefault) {
        checkImmutable();
        nativeSetBoolean(nativePtr, columnIndex, rowIndex, value, isDefault);
    }

    public void setFloat(long columnIndex, long rowIndex, float value, boolean isDefault) {
        checkImmutable();
        nativeSetFloat(nativePtr, columnIndex, rowIndex, value, isDefault);
    }

    public void setDouble(long columnIndex, long rowIndex, double value, boolean isDefault) {
        checkImmutable();
        nativeSetDouble(nativePtr, columnIndex, rowIndex, value, isDefault);
    }

    public void setDate(long columnIndex, long rowIndex, Date date, boolean isDefault) {
        if (date == null) { throw new IllegalArgumentException("Null Date is not allowed."); }
        checkImmutable();
        nativeSetTimestamp(nativePtr, columnIndex, rowIndex, date.getTime(), isDefault);
    }

    /**
     * Sets a String value to a cell of Table, pointed by column and row index.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index value of the cell row.
     * @param value a String value to set in the cell.
     */
    public void setString(long columnIndex, long rowIndex, String value, boolean isDefault) {
        checkImmutable();
        if (value == null) {
            checkDuplicatedNullForPrimaryKeyValue(columnIndex, rowIndex);
            nativeSetNull(nativePtr, columnIndex, rowIndex, isDefault);
        } else {
            checkStringValueIsLegal(columnIndex, rowIndex, value);
            nativeSetString(nativePtr, columnIndex, rowIndex, value, isDefault);
        }
    }

    public void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data, boolean isDefault) {
        checkImmutable();
        nativeSetByteArray(nativePtr, columnIndex, rowIndex, data, isDefault);
    }

    public void setLink(long columnIndex, long rowIndex, long value, boolean isDefault) {
        checkImmutable();
        nativeSetLink(nativePtr, columnIndex, rowIndex, value, isDefault);
    }

    public void setNull(long columnIndex, long rowIndex, boolean isDefault) {
        checkImmutable();
        checkDuplicatedNullForPrimaryKeyValue(columnIndex, rowIndex);
        nativeSetNull(nativePtr, columnIndex, rowIndex, isDefault);
    }

    public void addSearchIndex(long columnIndex) {
        checkImmutable();
        nativeAddSearchIndex(nativePtr, columnIndex);
    }

    public void removeSearchIndex(long columnIndex) {
        checkImmutable();
        nativeRemoveSearchIndex(nativePtr, columnIndex);
    }

    /**
     * Defines a primary key for this table. This needs to be called manually before inserting data into the table.
     *
     * @param columnName the name of the field that will function primary key. "" or {@code null} will remove any
     * previous set magic key.
     * @throws io.realm.exceptions.RealmException if it is not possible to set the primary key due to the column
     * not having distinct values (i.e. violating the primary key constraint).
     */
    public void setPrimaryKey(String columnName) {
        Table pkTable = getPrimaryKeyTable();
        if (pkTable == null) {
            throw new RealmException("Primary keys are only supported if Table is part of a Group");
        }
        cachedPrimaryKeyColumnIndex = nativeSetPrimaryKey(pkTable.nativePtr, nativePtr, columnName);
    }

    public void setPrimaryKey(long columnIndex) {
        setPrimaryKey(nativeGetColumnName(nativePtr, columnIndex));
    }

    private Table getPrimaryKeyTable() {
        if (sharedRealm == null) {
            return null;
        }
        Table pkTable = sharedRealm.getTable(PRIMARY_KEY_TABLE_NAME);
        if (pkTable.getColumnCount() == 0) {
            checkImmutable();
            long columnIndex = pkTable.addColumn(RealmFieldType.STRING, PRIMARY_KEY_CLASS_COLUMN_NAME);
            pkTable.addSearchIndex(columnIndex);
            pkTable.addColumn(RealmFieldType.STRING, PRIMARY_KEY_FIELD_COLUMN_NAME);
        }

        return pkTable;
    }

    /**
     * Invalidates a cached primary key column index for the table.
     */
    private void invalidateCachedPrimaryKeyIndex() {
        cachedPrimaryKeyColumnIndex = NO_MATCH;
    }

    /*
     * 1) Migration required to fix https://github.com/realm/realm-java/issues/1059
     * This will convert INTEGER column to the corresponding STRING column if needed.
     * Any database created on Realm-Java 0.80.1 and below will have this error.
     *
     * 2) Migration required to fix: https://github.com/realm/realm-java/issues/1703
     * This will remove the prefix "class_" from all table names in the pk_column
     * Any database created on Realm-Java 0.84.1 and below will have this error.
     */
    public static boolean migratePrimaryKeyTableIfNeeded(SharedRealm sharedRealm) {
        if (sharedRealm == null || !sharedRealm.isInTransaction()) {
            throwImmutable();
        }
        if (!sharedRealm.hasTable(PRIMARY_KEY_TABLE_NAME)) {
            return false;
        }
        Table pkTable = sharedRealm.getTable(PRIMARY_KEY_TABLE_NAME);
        return nativeMigratePrimaryKeyTableIfNeeded(sharedRealm.getGroupNative(), pkTable.nativePtr);
    }

    public static boolean primaryKeyTableNeedsMigration(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable(PRIMARY_KEY_TABLE_NAME)) {
            return false;
        }
        Table pkTable = sharedRealm.getTable(PRIMARY_KEY_TABLE_NAME);
        return nativePrimaryKeyTableNeedsMigration(pkTable.nativePtr);
    }

    public boolean hasSearchIndex(long columnIndex) {
        return nativeHasSearchIndex(nativePtr, columnIndex);
    }

    public boolean isNullLink(long columnIndex, long rowIndex) {
        return nativeIsNullLink(nativePtr, columnIndex, rowIndex);
    }

    public void nullifyLink(long columnIndex, long rowIndex) {
        nativeNullifyLink(nativePtr, columnIndex, rowIndex);
    }

    boolean isImmutable() {
        return sharedRealm != null && !sharedRealm.isInTransaction();
    }

    // This checking should be moved to SharedRealm level.
    void checkImmutable() {
        if (isImmutable()) {
            throwImmutable();
        }
    }

    private void checkHasPrimaryKey() {
        if (!hasPrimaryKey()) {
            throw new IllegalStateException(getName() + " has no primary key defined");
        }
    }

    //
    // Count
    //

    public long count(long columnIndex, long value) {
        return nativeCountLong(nativePtr, columnIndex, value);
    }

    public long count(long columnIndex, float value) {
        return nativeCountFloat(nativePtr, columnIndex, value);
    }

    public long count(long columnIndex, double value) {
        return nativeCountDouble(nativePtr, columnIndex, value);
    }

    public long count(long columnIndex, String value) {
        return nativeCountString(nativePtr, columnIndex, value);
    }

    //
    // Searching methods.
    //

    public TableQuery where() {
        long nativeQueryPtr = nativeWhere(nativePtr);
        // Copies context reference from parent.
        return new TableQuery(this.context, this, nativeQueryPtr);
    }

    public long findFirstLong(long columnIndex, long value) {
        return nativeFindFirstInt(nativePtr, columnIndex, value);
    }

    public long findFirstBoolean(long columnIndex, boolean value) {
        return nativeFindFirstBool(nativePtr, columnIndex, value);
    }

    public long findFirstFloat(long columnIndex, float value) {
        return nativeFindFirstFloat(nativePtr, columnIndex, value);
    }

    public long findFirstDouble(long columnIndex, double value) {
        return nativeFindFirstDouble(nativePtr, columnIndex, value);
    }

    public long findFirstDate(long columnIndex, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstTimestamp(nativePtr, columnIndex, date.getTime());
    }

    public long findFirstString(long columnIndex, String value) {
        if (value == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstString(nativePtr, columnIndex, value);
    }

    /**
     * Searches for first occurrence of null. Beware that the order in the column is undefined.
     *
     * @param columnIndex the column to search in.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    public long findFirstNull(long columnIndex) {
        return nativeFindFirstNull(nativePtr, columnIndex);
    }

    // Experimental feature
    public long lowerBoundLong(long columnIndex, long value) {
        return nativeLowerBoundInt(nativePtr, columnIndex, value);
    }

    public long upperBoundLong(long columnIndex, long value) {
        return nativeUpperBoundInt(nativePtr, columnIndex, value);
    }

    public Table pivot(long stringCol, long intCol, PivotType pivotType) {
        if (!this.getColumnType(stringCol).equals(RealmFieldType.STRING)) {
            throw new UnsupportedOperationException("Group by column must be of type String");
        }
        if (!this.getColumnType(intCol).equals(RealmFieldType.INTEGER)) {
            throw new UnsupportedOperationException("Aggregation column must be of type Int");
        }
        Table result = new Table();
        nativePivot(nativePtr, stringCol, intCol, pivotType.value, result.nativePtr);
        return result;
    }

    //

    /**
     * Returns the table name as it is in the associated group.
     *
     * @return Name of the the table or null if it not part of a group.
     */
    public String getName() {
        return nativeGetName(nativePtr);
    }

    public String toJson() {
        return nativeToJson(nativePtr);
    }

    @Override
    public String toString() {
        long columnCount = getColumnCount();
        String name = getName();
        StringBuilder stringBuilder = new StringBuilder("The Table ");
        if (name != null && !name.isEmpty()) {
            stringBuilder.append(getName());
            stringBuilder.append(" ");
        }
        if (hasPrimaryKey()) {
            String pkFieldName = getColumnName(getPrimaryKey());
            stringBuilder.append("has \'").append(pkFieldName).append("\' field as a PrimaryKey, and ");
        }
        stringBuilder.append("contains ");
        stringBuilder.append(columnCount);
        stringBuilder.append(" columns: ");

        for (int i = 0; i < columnCount; i++) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(getColumnName(i));
        }
        stringBuilder.append(".");

        stringBuilder.append(" And ");
        stringBuilder.append(size());
        stringBuilder.append(" rows.");

        return stringBuilder.toString();
    }

    private static void throwImmutable() {
        throw new IllegalStateException("Changing Realm data can only be done from inside a transaction.");
    }

    /**
     * Compares the schema of the current instance of Table with another instance.
     *
     * @param table the instance to compare with. It cannot be null.
     * @return {@code true} if the two instances have the same schema (column names and types).
     */
    public boolean hasSameSchema(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("The argument cannot be null");
        }
        return nativeHasSameSchema(this.nativePtr, table.nativePtr);
    }

    /**
     * Checks if a given table name is a name for a model table.
     */
    public static boolean isModelTable(String tableName) {
        return tableName.startsWith(TABLE_PREFIX);
    }

    /**
     * Reports the current versioning counter for the table. The versioning counter is guaranteed to
     * change when the contents of the table changes after advance_read() or promote_to_write(), or
     * immediately after calls to methods which change the table.
     *
     * @return version_counter for the table.
     */
    public long getVersion() {
        return nativeVersion(nativePtr);
    }

    public static String tableNameToClassName(String tableName) {
        if (!tableName.startsWith(Table.TABLE_PREFIX)) {
            return tableName;
        }
        return tableName.substring(Table.TABLE_PREFIX.length());
    }

    protected native long createNative();

    private native boolean nativeIsValid(long nativeTablePtr);

    private native long nativeAddColumn(long nativeTablePtr, int type, String name, boolean isNullable);

    private native long nativeAddColumnLink(long nativeTablePtr, int type, String name, long targetTablePtr);

    private native void nativeRenameColumn(long nativeTablePtr, long columnIndex, String name);

    private native void nativeRemoveColumn(long nativeTablePtr, long columnIndex);

    private native boolean nativeIsColumnNullable(long nativePtr, long columnIndex);

    private native void nativeConvertColumnToNullable(long nativeTablePtr, long columnIndex);

    private native void nativeConvertColumnToNotNullable(long nativePtr, long columnIndex);

    private native long nativeSize(long nativeTablePtr);

    private native void nativeClear(long nativeTablePtr);

    private native long nativeGetColumnCount(long nativeTablePtr);

    private native String nativeGetColumnName(long nativeTablePtr, long columnIndex);

    private native long nativeGetColumnIndex(long nativeTablePtr, String columnName);

    private native int nativeGetColumnType(long nativeTablePtr, long columnIndex);

    private native void nativeMoveLastOver(long nativeTablePtr, long rowIndex);

    public static native long nativeAddEmptyRow(long nativeTablePtr, long rows);

    private native long nativeGetSortedViewMulti(long nativeTableViewPtr, long[] columnIndices, boolean[] ascending);

    private native long nativeGetLong(long nativeTablePtr, long columnIndex, long rowIndex);

    private native boolean nativeGetBoolean(long nativeTablePtr, long columnIndex, long rowIndex);

    private native float nativeGetFloat(long nativeTablePtr, long columnIndex, long rowIndex);

    private native double nativeGetDouble(long nativeTablePtr, long columnIndex, long rowIndex);

    private native long nativeGetTimestamp(long nativeTablePtr, long columnIndex, long rowIndex);

    private native String nativeGetString(long nativePtr, long columnIndex, long rowIndex);

    private native byte[] nativeGetByteArray(long nativePtr, long columnIndex, long rowIndex);

    private native long nativeGetLink(long nativePtr, long columnIndex, long rowIndex);

    public static native long nativeGetLinkView(long nativePtr, long columnIndex, long rowIndex);

    private native long nativeGetLinkTarget(long nativePtr, long columnIndex);

    private native boolean nativeIsNull(long nativePtr, long columnIndex, long rowIndex);

    native long nativeGetRowPtr(long nativePtr, long index);

    public static native void nativeSetLong(long nativeTablePtr, long columnIndex, long rowIndex, long value, boolean isDefault);

    public static native void nativeSetLongUnique(long nativeTablePtr, long columnIndex, long rowIndex, long value);

    public static native void nativeSetBoolean(long nativeTablePtr, long columnIndex, long rowIndex, boolean value, boolean isDefault);

    public static native void nativeSetFloat(long nativeTablePtr, long columnIndex, long rowIndex, float value, boolean isDefault);

    public static native void nativeSetDouble(long nativeTablePtr, long columnIndex, long rowIndex, double value, boolean isDefault);

    public static native void nativeSetTimestamp(long nativeTablePtr, long columnIndex, long rowIndex, long dateTimeValue, boolean isDefault);

    public static native void nativeSetString(long nativeTablePtr, long columnIndex, long rowIndex, String value, boolean isDefault);

    public static native void nativeSetStringUnique(long nativeTablePtr, long columnIndex, long rowIndex, String value);

    public static native void nativeSetNull(long nativeTablePtr, long columnIndex, long rowIndex, boolean isDefault);

    // Use nativeSetStringUnique(null) for String column!
    public static native void nativeSetNullUnique(long nativeTablePtr, long columnIndex, long rowIndex);

    public static native void nativeSetByteArray(long nativePtr, long columnIndex, long rowIndex, byte[] data, boolean isDefault);

    public static native void nativeSetLink(long nativeTablePtr, long columnIndex, long rowIndex, long value, boolean isDefault);

    private native long nativeSetPrimaryKey(long privateKeyTableNativePtr, long nativePtr, String columnName);

    private static native boolean nativeMigratePrimaryKeyTableIfNeeded(long groupNativePtr, long primaryKeyTableNativePtr);

    private static native boolean nativePrimaryKeyTableNeedsMigration(long primaryKeyTableNativePtr);

    private native void nativeAddSearchIndex(long nativePtr, long columnIndex);

    private native void nativeRemoveSearchIndex(long nativePtr, long columnIndex);

    private native boolean nativeHasSearchIndex(long nativePtr, long columnIndex);

    private native boolean nativeIsNullLink(long nativePtr, long columnIndex, long rowIndex);

    public static native void nativeNullifyLink(long nativePtr, long columnIndex, long rowIndex);

    private native long nativeCountLong(long nativePtr, long columnIndex, long value);

    private native long nativeCountFloat(long nativePtr, long columnIndex, float value);

    private native long nativeCountDouble(long nativePtr, long columnIndex, double value);

    private native long nativeCountString(long nativePtr, long columnIndex, String value);

    private native long nativeWhere(long nativeTablePtr);

    public static native long nativeFindFirstInt(long nativeTablePtr, long columnIndex, long value);

    private native long nativeFindFirstBool(long nativePtr, long columnIndex, boolean value);

    private native long nativeFindFirstFloat(long nativePtr, long columnIndex, float value);

    private native long nativeFindFirstDouble(long nativePtr, long columnIndex, double value);

    private native long nativeFindFirstTimestamp(long nativeTablePtr, long columnIndex, long dateTimeValue);

    public static native long nativeFindFirstString(long nativeTablePtr, long columnIndex, String value);

    public static native long nativeFindFirstNull(long nativeTablePtr, long columnIndex);

    // FIXME: Disabled in cpp code, see comments there
    // private native long nativeFindAllTimestamp(long nativePtr, long columnIndex, long dateTimeValue);
    private native long nativeLowerBoundInt(long nativePtr, long columnIndex, long value);

    private native long nativeUpperBoundInt(long nativePtr, long columnIndex, long value);

    private native void nativePivot(long nativeTablePtr, long stringCol, long intCol, int pivotType, long resultPtr);

    private native String nativeGetName(long nativeTablePtr);

    private native String nativeToJson(long nativeTablePtr);

    private native boolean nativeHasSameSchema(long thisTable, long otherTable);

    private native long nativeVersion(long nativeTablePtr);

    private static native long nativeGetFinalizerPtr();
}
