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

import javax.annotation.Nullable;

import io.realm.RealmFieldType;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;


/**
 * This class is a base class for all Realm tables. The class supports all low level methods
 * (define/insert/delete/update) a table has. All the native communications to the Realm C++ library are also handled by
 * this class.
 */
public class Table implements NativeObject {

    private static final String TABLE_PREFIX = Util.getTablePrefix();
    private static final int TABLE_NAME_MAX_LENGTH = 63; // Max length of table names
    public static final int CLASS_NAME_MAX_LENGTH = TABLE_NAME_MAX_LENGTH - TABLE_PREFIX.length(); // Max length of class names
    public static final long INFINITE = -1;
    public static final boolean NULLABLE = true;
    public static final boolean NOT_NULLABLE = false;
    public static final int NO_MATCH = -1;

    public static final int MAX_BINARY_SIZE = 0xFFFFF8 - 8/*array header size*/;
    public static final int MAX_STRING_SIZE = 0xFFFFF8 - 8/*array header size*/ - 1;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativeTableRefPtr;
    private final NativeContext context;

    private final OsSharedRealm sharedRealm;

    Table(OsSharedRealm sharedRealm, long nativeTableRefPointer) {
        this.context = sharedRealm.context;
        this.sharedRealm = sharedRealm;
        this.nativeTableRefPtr = nativeTableRefPointer;
        context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativeTableRefPtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public Table getTable() {
        return this;
    }

    /*
     * Checks if the Table is valid.
     * Whenever a Table/subtable is changed/updated all it's subtables are invalidated.
     * You can no longer perform any actions on the table, and if done anyway, an exception is thrown.
     * The only method you can call is 'isValid()'.
     */
    public boolean isValid() {
        return nativeTableRefPtr != 0 && nativeIsValid(nativeTableRefPtr);
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
     * @return the column key of the new column.
     */
    public long addColumn(RealmFieldType type, String name, boolean isNullable) {
        verifyColumnName(name);
        switch (type) {
            case INTEGER:
            case BOOLEAN:
            case STRING:
            case BINARY:
            case DATE:
            case FLOAT:
            case DOUBLE:
                return nativeAddColumn(nativeTableRefPtr, type.getNativeValue(), name, isNullable);

            case INTEGER_LIST:
            case BOOLEAN_LIST:
            case STRING_LIST:
            case BINARY_LIST:
            case DATE_LIST:
            case FLOAT_LIST:
            case DOUBLE_LIST:
                return nativeAddPrimitiveListColumn(nativeTableRefPtr, type.getNativeValue() - 128, name, isNullable);

            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    /**
     * Adds a non-nullable column to the table dynamically.
     *
     * @return the column key of the new column.
     */
    public long addColumn(RealmFieldType type, String name) {
        return addColumn(type, name, false);
    }

    /**
     * Adds a link column to the table dynamically.
     *
     * @return the column key of the new column.
     */
    public long addColumnLink(RealmFieldType type, String name, Table table) {
        verifyColumnName(name);
        return nativeAddColumnLink(nativeTableRefPtr, type.getNativeValue(), name, table.nativeTableRefPtr);
    }

    /**
     * Removes a column in the table dynamically.
     * <p>
     * It should be noted if {@code columnKey} is the same as the primary key column key,
     * the primary key column is removed from the meta table.
     *
     * @param columnKey the column key to be removed.
     */
    public void removeColumn(long columnKey) {
        final String className = getClassName();
        // Checks the PK column key before removing a column. We don't know if we're hitting a PK col,
        // but it should be noted that once a column is removed, there is no way we can find whether
        // a PK exists or not.
        final String columnName = getColumnName(columnKey);
        final String pkName = OsObjectStore.getPrimaryKeyForObject(sharedRealm, getClassName());

        // First removes a column. If there is no error, we can proceed. Otherwise, it will stop here.
        nativeRemoveColumn(nativeTableRefPtr, columnKey);

        // Checks if a PK exists and takes actions if there is.
        if (columnName.equals(pkName)) {
            // In case we're hitting PK column, we should remove the PK as it is either 1) a user has
            // forgotten to remove PK or 2) removeColumn gets called before setPrimaryKey(null) is called.
            // Since there is no danger in removing PK twice, we'll do it here to be on safe side.
            OsObjectStore.setPrimaryKeyForObject(sharedRealm, className, null);
        }
    }

    /**
     * Renames a column in the table. If the column is a primary key column, the corresponding entry
     * in PrimaryKeyTable will be renamed accordingly.
     *
     * @param columnKey the column to be renamed.
     * @param newName a new name replacing the old column name.
     * @throws IllegalArgumentException if {@code newFieldName} is an empty string, or exceeds field name length limit.
     */
    public void renameColumn(long columnKey, String newName) {
        verifyColumnName(newName);
        // Gets the old column name. We'll assume that the old column name is *NOT* an empty string.
        final String oldName = nativeGetColumnName(nativeTableRefPtr, columnKey);
        final String pkName = OsObjectStore.getPrimaryKeyForObject(sharedRealm, getClassName());

        // Then let's try to rename a column. If an error occurs for some reasons, we'll throw.
        nativeRenameColumn(nativeTableRefPtr, columnKey, newName);

        // Renames a primary key. At this point, renaming the column name should have been fine.
        if (oldName.equals(pkName)) {
            try {
                OsObjectStore.setPrimaryKeyForObject(sharedRealm, getClassName(), newName);
            } catch (Exception e) {
                // We failed to rename the pk meta table. roll back the column name, not pk meta table
                // then rethrow.
                nativeRenameColumn(nativeTableRefPtr, columnKey, oldName);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks whether the specific column is nullable?
     *
     * @param columnKey the column to check.
     * @return {@code true} if column is nullable, {@code false} otherwise.
     */
    public boolean isColumnNullable(long columnKey) {
        return nativeIsColumnNullable(nativeTableRefPtr, columnKey);
    }

    /**
     * Converts a column to be nullable.
     *
     * @param columnKey the key for the column to convert.
     */
    public void convertColumnToNullable(long columnKey) {
        if (sharedRealm.isSyncRealm()) {
            throw new IllegalStateException("This method is only available for non-synchronized Realms");
        }
        nativeConvertColumnToNullable(nativeTableRefPtr, columnKey, isPrimaryKey(columnKey));
    }

    /**
     * Converts a column to be not nullable. null values will be converted to default values.
     *
     * @param columnKey the key for the column to convert.
     */
    public void convertColumnToNotNullable(long columnKey) {
        if (sharedRealm.isSyncRealm()) {
            throw new IllegalStateException("This method is only available for non-synchronized Realms");
        }
        nativeConvertColumnToNotNullable(nativeTableRefPtr, columnKey, isPrimaryKey(columnKey));
    }

    // Table Size and deletion. AutoGenerated subclasses are nothing to do with this
    // class.

    /**
     * Gets the number of entries/rows of this table.
     *
     * @return the number of rows.
     */
    public long size() {
        return nativeSize(nativeTableRefPtr);
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
     *
     * If using partial sync, this method will behave similarly to 'findAll().deleteFromRealm()'.
     */
    public void clear(boolean partialRealm) {
        checkImmutable();
        nativeClear(nativeTableRefPtr, partialRealm);
    }

    // Column Information.

    /**
     * Returns the number of columns in the table.
     *
     * @return the number of columns.
     */
    public long getColumnCount() {
        return nativeGetColumnCount(nativeTableRefPtr);
    }

    /**
     * Returns the name of a column identified by columnKey.
     *
     * @param columnKey the key of the column to find.
     * @return the name of the column.
     */
    public String getColumnName(long columnKey) {
        return nativeGetColumnName(nativeTableRefPtr, columnKey);
    }

    public String[] getColumnNames() {
        return nativeGetColumnNames(nativeTableRefPtr);
    }

    public long getColumnKey(String columnName) {
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        return nativeGetColumnKey(nativeTableRefPtr, columnName);
    }

    /**
     * Gets the type of a column identified by the columnKey.
     *
     * @param columnKey key of the column.
     * @return the type of the particular column.
     */
    public RealmFieldType getColumnType(long columnKey) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativeTableRefPtr, columnKey));
    }

    /**
     * Removes a row from the specific row key. If it is not the last row in the table, it then moves the last row into
     * the vacated slot.
     *
     * @param rowKey the row key
     */
    public void moveLastOver(long rowKey) {
        checkImmutable();
        nativeMoveLastOver(nativeTableRefPtr, rowKey);
    }

    /**
     * Checks if a given column is a primary key column.
     *
     * @param columnKey key of the column.
     * @return {@code true} if column is a primary key, {@code false} otherwise.
     */
    private boolean isPrimaryKey(long columnKey) {
        return getColumnName(columnKey).equals(OsObjectStore.getPrimaryKeyForObject(sharedRealm, getClassName()));
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

    public OsSharedRealm getSharedRealm() {
        return sharedRealm;
    }

    public long getLong(long columnKey, long rowKey) {
        return nativeGetLong(nativeTableRefPtr, columnKey, rowKey);
    }

    public boolean getBoolean(long columnKey, long rowKey) {
        return nativeGetBoolean(nativeTableRefPtr, columnKey, rowKey);
    }

    public float getFloat(long columnKey, long rowKey) {
        return nativeGetFloat(nativeTableRefPtr, columnKey, rowKey);
    }

    public double getDouble(long columnKey, long rowKey) {
        return nativeGetDouble(nativeTableRefPtr, columnKey, rowKey);
    }

    public Date getDate(long columnKey, long rowKey) {
        return new Date(nativeGetTimestamp(nativeTableRefPtr, columnKey, rowKey));
    }

    /**
     * Gets the value of a (string) cell.
     *
     * @param columnKey column key.
     * @param rowKey row key.
     * @return value of the particular cell
     */
    public String getString(long columnKey, long rowKey) {
        return nativeGetString(nativeTableRefPtr, columnKey, rowKey);
    }

    public byte[] getBinaryByteArray(long columnKey, long rowKey) {
        return nativeGetByteArray(nativeTableRefPtr, columnKey, rowKey);
    }

    public long getLink(long columnKey, long rowKey) {
        return nativeGetLink(nativeTableRefPtr, columnKey, rowKey);
    }

    public Table getLinkTarget(long columnKey) {
        long nativeTablePointer = nativeGetLinkTarget(nativeTableRefPtr, columnKey);
        // Copies context reference from parent.
        return new Table(this.sharedRealm, nativeTablePointer);
    }

    /**
     * Returns a non-checking Row. Incorrect use of this Row will cause a hard core crash.
     * If error checking is required, use {@link #getCheckedRow(long)} instead.
     *
     * @param rowKey row  key to fetch.
     * @return the unsafe row wrapper object.
     */
    public UncheckedRow getUncheckedRow(long rowKey) {
        return UncheckedRow.getByRowKey(context, this, rowKey);
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
     * @param objKey the Object Key.
     * @return the safe row wrapper object.
     */
    public CheckedRow getCheckedRow(long objKey) {
        return CheckedRow.get(context, this, objKey);
    }

    //
    // Setters
    //

    public void setLong(long columnKey, long rowKey, long value, boolean isDefault) {
        checkImmutable();
        nativeSetLong(nativeTableRefPtr, columnKey, rowKey, value, isDefault);
    }

    // must not be called on a primary key field
    public void incrementLong(long columnKey, long rowKey, long value) {
        checkImmutable();
        nativeIncrementLong(nativeTableRefPtr, columnKey, rowKey, value);
    }

    public void setBoolean(long columnKey, long rowKey, boolean value, boolean isDefault) {
        checkImmutable();
        nativeSetBoolean(nativeTableRefPtr, columnKey, rowKey, value, isDefault);
    }

    public void setFloat(long columnKey, long rowKey, float value, boolean isDefault) {
        checkImmutable();
        nativeSetFloat(nativeTableRefPtr, columnKey, rowKey, value, isDefault);
    }

    public void setDouble(long columnKey, long rowKey, double value, boolean isDefault) {
        checkImmutable();
        nativeSetDouble(nativeTableRefPtr, columnKey, rowKey, value, isDefault);
    }

    public void setDate(long columnKey, long rowKey, Date date, boolean isDefault) {
        if (date == null) { throw new IllegalArgumentException("Null Date is not allowed."); }
        checkImmutable();
        nativeSetTimestamp(nativeTableRefPtr, columnKey, rowKey, date.getTime(), isDefault);
    }

    /**
     * Sets a String value to a cell of Table, pointed by column and row key.
     *
     * @param columnKey cell column.
     * @param rowKey cell row.
     * @param value a String value to set in the cell.
     */
    public void setString(long columnKey, long rowKey, @Nullable String value, boolean isDefault) {
        checkImmutable();
        if (value == null) {
            nativeSetNull(nativeTableRefPtr, columnKey, rowKey, isDefault);
        } else {
            nativeSetString(nativeTableRefPtr, columnKey, rowKey, value, isDefault);
        }
    }

    public void setBinaryByteArray(long columnKey, long rowKey, byte[] data, boolean isDefault) {
        checkImmutable();
        nativeSetByteArray(nativeTableRefPtr, columnKey, rowKey, data, isDefault);
    }

    public void setLink(long columnKey, long rowKey, long value, boolean isDefault) {
        checkImmutable();
        nativeSetLink(nativeTableRefPtr, columnKey, rowKey, value, isDefault);
    }

    public void setNull(long columnKey, long rowKey, boolean isDefault) {
        checkImmutable();
        nativeSetNull(nativeTableRefPtr, columnKey, rowKey, isDefault);
    }

    public void addSearchIndex(long columnKey) {
        checkImmutable();
        nativeAddSearchIndex(nativeTableRefPtr, columnKey);
    }

    public void removeSearchIndex(long columnKey) {
        checkImmutable();
        nativeRemoveSearchIndex(nativeTableRefPtr, columnKey);
    }

    public boolean hasSearchIndex(long columnKey) {
        return nativeHasSearchIndex(nativeTableRefPtr, columnKey);
    }

    public boolean isNullLink(long columnKey, long rowKey) {
        return nativeIsNullLink(nativeTableRefPtr, columnKey, rowKey);
    }

    public void nullifyLink(long columnKey, long rowKey) {
        nativeNullifyLink(nativeTableRefPtr, columnKey, rowKey);
    }

    boolean isImmutable() {
        return sharedRealm != null && !sharedRealm.isInTransaction();
    }

    // This checking should be moved to OsSharedRealm level.
    void checkImmutable() {
        if (isImmutable()) {
            throwImmutable();
        }
    }

    //
    // Count
    //

    public long count(long columnKey, long value) {
        return nativeCountLong(nativeTableRefPtr, columnKey, value);
    }

    public long count(long columnKey, float value) {
        return nativeCountFloat(nativeTableRefPtr, columnKey, value);
    }

    public long count(long columnKey, double value) {
        return nativeCountDouble(nativeTableRefPtr, columnKey, value);
    }

    public long count(long columnKey, String value) {
        return nativeCountString(nativeTableRefPtr, columnKey, value);
    }

    //
    // Searching methods.
    //

    public TableQuery where() {
        long nativeQueryPtr = nativeWhere(nativeTableRefPtr);
        // Copies context reference from parent.
        return new TableQuery(this.context, this, nativeQueryPtr);
    }

    public long findFirstLong(long columnKey, long value) {
        return nativeFindFirstInt(nativeTableRefPtr, columnKey, value);
    }

    public long findFirstBoolean(long columnKey, boolean value) {
        return nativeFindFirstBool(nativeTableRefPtr, columnKey, value);
    }

    public long findFirstFloat(long columnKey, float value) {
        return nativeFindFirstFloat(nativeTableRefPtr, columnKey, value);
    }

    public long findFirstDouble(long columnKey, double value) {
        return nativeFindFirstDouble(nativeTableRefPtr, columnKey, value);
    }

    public long findFirstDate(long columnKey, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstTimestamp(nativeTableRefPtr, columnKey, date.getTime());
    }

    public long findFirstString(long columnKey, String value) {
        if (value == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstString(nativeTableRefPtr, columnKey, value);
    }

    /**
     * Searches for first occurrence of null. Beware that the order in the column is undefined.
     *
     * @param columnKey the column to search in.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    public long findFirstNull(long columnKey) {
        return nativeFindFirstNull(nativeTableRefPtr, columnKey);
    }

    //

    /**
     * Returns the table name as it is in the associated group.
     *
     * @return Name of the the table or {@code null} if it not part of a group.
     */
    @Nullable
    public String getName() {
        return nativeGetName(nativeTableRefPtr);
    }

    /**
     * Returns the class name for the table.
     *
     * @return Name of the the table
     * @throws IllegalStateException if the table has been deleted or no longer is part of the group.
     */
    public String getClassName() {
        String name = getClassNameForTable(getName()); // Core returns "" if Table is no longer attached
        if (Util.isEmptyString(name)) {
            throw new IllegalStateException("This object class is no longer part of the schema for the Realm file. It is therefor not possible to access the schema name.");
        }
        return name;
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
        stringBuilder.append("contains ");
        stringBuilder.append(columnCount);
        stringBuilder.append(" columns: ");

        boolean isFirst = true;
        for (String column: getColumnNames()) {
            if(!isFirst) {
                stringBuilder.append(", ");
            }
            isFirst = false;
            stringBuilder.append(column);
        }
        stringBuilder.append(".");

        stringBuilder.append(" And ");
        stringBuilder.append(size());
        stringBuilder.append(" rows.");

        return stringBuilder.toString();
    }

    private static void throwImmutable() {
        throw new IllegalStateException("Cannot modify managed objects outside of a write transaction.");
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
        return nativeHasSameSchema(this.nativeTableRefPtr, table.nativeTableRefPtr);
    }

    /**
     * Returns a frozen copy of this table.
     */
    public Table freeze(OsSharedRealm frozenRealm) {
        if (!frozenRealm.isFrozen()) {
            throw new IllegalArgumentException("Frozen Realm required");
        }
        return new Table(frozenRealm, nativeFreeze(frozenRealm.getNativePtr(), nativeTableRefPtr));
    }

    @Nullable
    public static String getClassNameForTable(@Nullable String name) {
        if (name == null) { return null; }
        if (!name.startsWith(TABLE_PREFIX)) {
            return name;
        }
        return name.substring(TABLE_PREFIX.length());
    }

    public static String getTableNameForClass(String name) {
        //noinspection ConstantConditions
        if (name == null) { return null; }
        return TABLE_PREFIX + name;
    }

    private native boolean nativeIsValid(long nativeTableRefPtr);

    private native long nativeAddColumn(long nativeTableRefPtr, int type, String name, boolean isNullable);

    private native long nativeAddPrimitiveListColumn(long nativeTableRefPtr, int type, String name, boolean isNullable);

    private native long nativeAddColumnLink(long nativeTableRefPtr, int type, String name, long targetTablePtr);

    private native void nativeRenameColumn(long nativeTableRefPtr, long columnKey, String name);

    private native void nativeRemoveColumn(long nativeTableRefPtr, long columnKey);

    private native boolean nativeIsColumnNullable(long nativePtr, long columnKey);

    private native void nativeConvertColumnToNullable(long nativeTableRefPtr, long columnKey, boolean isPrimaryKey);

    private native void nativeConvertColumnToNotNullable(long nativePtr, long columnKey, boolean isPrimaryKey);

    private native long nativeSize(long nativeTableRefPtr);

    private native void nativeClear(long nativeTableRefPtr, boolean partialRealm);

    private native long nativeGetColumnCount(long nativeTableRefPtr);

    private native String nativeGetColumnName(long nativeTableRefPtr, long columnKey);

    private native String[] nativeGetColumnNames(long nativeTableRefPtr);

    private native long nativeGetColumnKey(long nativeTableRefPtr, String columnName);

    private native int nativeGetColumnType(long nativeTableRefPtr, long columnKey);

    private native void nativeMoveLastOver(long nativeTableRefPtr, long rowKey);

    private native long nativeGetLong(long nativeTableRefPtr, long columnKey, long rowKey);

    private native boolean nativeGetBoolean(long nativeTableRefPtr, long columnKey, long rowKey);

    private native float nativeGetFloat(long nativeTableRefPtr, long columnKey, long rowKey);

    private native double nativeGetDouble(long nativeTableRefPtr, long columnKey, long rowKey);

    private native long nativeGetTimestamp(long nativeTableRefPtr, long columnKey, long rowKey);

    private native String nativeGetString(long nativePtr, long columnKey, long rowKey);

    private native byte[] nativeGetByteArray(long nativePtr, long columnKey, long rowKey);

    private native long nativeGetLink(long nativePtr, long columnKey, long rowKey);

    private native long nativeGetLinkTarget(long nativePtr, long columnKey);

    private native boolean nativeIsNull(long nativePtr, long columnKey, long rowKey);

    native long nativeGetRowPtr(long nativePtr, long objKey);

    public static native void nativeSetLong(long nativeTableRefPtr, long columnKey, long rowKey, long value, boolean isDefault);

    public static native void nativeIncrementLong(long nativeTableRefPtr, long columnKey, long rowKey, long value);

    public static native void nativeSetBoolean(long nativeTableRefPtr, long columnKey, long rowKey, boolean value, boolean isDefault);

    public static native void nativeSetFloat(long nativeTableRefPtr, long columnKey, long rowKey, float value, boolean isDefault);

    public static native void nativeSetDouble(long nativeTableRefPtr, long columnKey, long rowKey, double value, boolean isDefault);

    public static native void nativeSetTimestamp(long nativeTableRefPtr, long columnKey, long rowKey, long dateTimeValue, boolean isDefault);

    public static native void nativeSetString(long nativeTableRefPtr, long columnKey, long rowKey, String value, boolean isDefault);

    public static native void nativeSetNull(long nativeTableRefPtr, long columnKey, long rowKey, boolean isDefault);

    public static native void nativeSetByteArray(long nativePtr, long columnKey, long rowKey, byte[] data, boolean isDefault);

    public static native void nativeSetLink(long nativeTableRefPtr, long columnKey, long rowKey, long value, boolean isDefault);

    private native void nativeAddSearchIndex(long nativePtr, long columnKey);

    private native void nativeRemoveSearchIndex(long nativePtr, long columnKey);

    private native boolean nativeHasSearchIndex(long nativePtr, long columnKey);

    private native boolean nativeIsNullLink(long nativePtr, long columnKey, long rowKey);

    public static native void nativeNullifyLink(long nativePtr, long columnKey, long rowKey);

    private native long nativeCountLong(long nativePtr, long columnKey, long value);

    private native long nativeCountFloat(long nativePtr, long columnKey, float value);

    private native long nativeCountDouble(long nativePtr, long columnKey, double value);

    private native long nativeCountString(long nativePtr, long columnKey, String value);

    private native long nativeWhere(long nativeTableRefPtr);

    public static native long nativeFindFirstInt(long nativeTableRefPtr, long columnKey, long value);

    private native long nativeFindFirstBool(long nativePtr, long columnKey, boolean value);

    private native long nativeFindFirstFloat(long nativePtr, long columnKey, float value);

    private native long nativeFindFirstDouble(long nativePtr, long columnKey, double value);

    private native long nativeFindFirstTimestamp(long nativeTableRefPtr, long columnKey, long dateTimeValue);

    public static native long nativeFindFirstString(long nativeTableRefPtr, long columnKey, String value);

    public static native long nativeFindFirstNull(long nativeTableRefPtr, long columnKey);

    private native String nativeGetName(long nativeTableRefPtr);

    private native boolean nativeHasSameSchema(long thisTable, long otherTable);

    private static native long nativeGetFinalizerPtr();

    private static native long nativeFreeze(long frozenSharedRealmPtr, long nativeTableRefPtr);
}
