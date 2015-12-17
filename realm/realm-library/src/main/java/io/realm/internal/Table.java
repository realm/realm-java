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

import java.io.Closeable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.RealmFieldType;
import io.realm.Sort;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.internal.log.RealmLog;


/**
 * This class is a base class for all Realm tables. The class supports all low level methods
 * (define/insert/delete/update) a table has. All the native communications to the Realm C++ library are also handled by
 * this class.
 */
public class Table implements TableOrView, TableSchema, Closeable {

    public static final int TABLE_MAX_LENGTH = 56; // Max length of class names without prefix
    public static final String TABLE_PREFIX = Util.getTablePrefix();
    public static final long INFINITE = -1;
    public static final String STRING_DEFAULT_VALUE = "";
    public static final long INTEGER_DEFAULT_VALUE = 0;
    public static final String METADATA_TABLE_NAME = "metadata";
    public static final boolean NULLABLE = true;
    public static final boolean NOT_NULLABLE = false;

    private static final String PRIMARY_KEY_TABLE_NAME = "pk";
    private static final String PRIMARY_KEY_CLASS_COLUMN_NAME = "pk_table";
    private static final long PRIMARY_KEY_CLASS_COLUMN_INDEX = 0;
    private static final String PRIMARY_KEY_FIELD_COLUMN_NAME = "pk_property";
    private static final long PRIMARY_KEY_FIELD_COLUMN_INDEX = 1;
    private static final long NO_PRIMARY_KEY = -2;

    protected long nativePtr;
    protected final Object parent;
    private final Context context;
    private long cachedPrimaryKeyColumnIndex = NO_MATCH;

    // test:
    protected int tableNo;
    private static final boolean DEBUG = false;
    static AtomicInteger tableCount = new AtomicInteger(0);

    static {
        RealmCore.loadLibrary();
    }

    /**
     * Constructs a Table base object. It can be used to register columns in this table. Registering into table is
     * allowed only for empty tables. It creates a native reference of the object and keeps a reference to it.
     */
    public Table() {
        this.parent = null; // No parent in free-standing table
        this.context = new Context();
        // Native methods work will be initialized here. Generated classes will
        // have nothing to do with the native functions. Generated Java Table
        // classes will work as a wrapper on top of table.
        this.nativePtr = createNative();
        if (nativePtr == 0) {
            throw new java.lang.OutOfMemoryError("Out of native memory.");
        }
        if (DEBUG) {
            tableNo = tableCount.incrementAndGet();
            RealmLog.d("====== New Tablebase " + tableNo + " : ptr = " + nativePtr);
        }
    }

    Table(Context context, Object parent, long nativePointer) {
        this.context = context;
        this.parent  = parent;
        this.nativePtr = nativePointer;
        if (DEBUG) {
            tableNo = tableCount.incrementAndGet();
            RealmLog.d("===== New Tablebase(ptr) " + tableNo + " : ptr = " + nativePtr);
        }
    }

    @Override
    public Table getTable() {
        return this;
    }

    // If close() is called, no penalty is paid for delayed disposal
    // via the context
    @Override
    public void close() {
        synchronized (context) {
            if (nativePtr != 0) {
                nativeClose(nativePtr);
                if (DEBUG) {
                    tableCount.decrementAndGet();
                    RealmLog.d("==== CLOSE " + tableNo + " ptr= " + nativePtr + " remaining " + tableCount.get());
                }
                nativePtr = 0;
            }
        }
    }

    @Override
    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                boolean isRoot = (parent == null);
                context.asyncDisposeTable(nativePtr, isRoot);
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
            }
        }
        if (DEBUG) {
            RealmLog.d("==== FINALIZE " + tableNo + "...");
        }
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

    @Override
    public TableSchema getSubtableSchema(long columnIndex) {
        if (!nativeIsRootTable(nativePtr)) {
            throw new UnsupportedOperationException("This is a subtable. Can only be called on root table.");
        }

        long[] newPath = new long[1];
        newPath[0] = columnIndex;
        return new SubtableSchema(nativePtr, newPath);
    }

    /**
     * Adds a column to the table dynamically.
     *
     * @param type the column type.
     * @param name the field/column name.
     * @param isNullable {@code true} if column can contain null values, {@ code false}e otherwise.
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
    public long addColumnLink (RealmFieldType type, String name, Table table) {
        verifyColumnName(name);
        return nativeAddColumnLink(nativePtr, type.getNativeValue(), name, table.nativePtr);
    }

    /**
     * Removes a column in the table dynamically.
     */
    @Override
    public void removeColumn(long columnIndex) {
        nativeRemoveColumn(nativePtr, columnIndex);
    }

    /**
     * Renames a column in the table.
     */
    @Override
    public void renameColumn(long columnIndex, String newName) {
        verifyColumnName(newName);
        nativeRenameColumn(nativePtr, columnIndex, newName);
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

    /**
     * Updates a table specification from a Table specification structure.
     */
    public void updateFromSpec(TableSpec tableSpec) {
        checkImmutable();
        nativeUpdateFromSpec(nativePtr, tableSpec);
    }

    // Table Size and deletion. AutoGenerated subclasses are nothing to do with this
    // class.
    /**
     * Gets the number of entries/rows of this table.
     *
     * @return the number of rows.
     */
    @Override
    public long size() {
        return nativeSize(nativePtr);
    }

    /**
     * Checks whether this table is empty or not.
     *
     * @return {@code true} if empty, otherwise {@code false}.
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Clears the table i.e., deleting all rows in the table.
     */
    @Override
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
    @Override
    public long getColumnCount() {
        return nativeGetColumnCount(nativePtr);
    }

    public TableSpec getTableSpec(){
        return nativeGetTableSpec(nativePtr);
    }

    /**
     * Returns the name of a column identified by columnIndex. Notice that the index is zero based.
     *
     * @param columnIndex the column index.
     * @return the name of the column.
     */
    @Override
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePtr, columnIndex);
    }

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name.
     * @return the index, {@link #NO_MATCH} if not found.
     */
    @Override
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
    @Override
    public RealmFieldType getColumnType(long columnIndex) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativePtr, columnIndex));
    }


    /**
     * Removes a row from the specific index. As of now the entry is simply removed from the table.
     *
     * @param rowIndex the row index (starting with 0)
     *
     */
    @Override
    public void remove(long rowIndex) {
        checkImmutable();
        nativeRemove(nativePtr, rowIndex);
    }

    @Override
    public void removeLast() {
        checkImmutable();
        nativeRemoveLast(nativePtr);
    }

    public void moveLastOver(long rowIndex) {
        checkImmutable();
        nativeMoveLastOver(nativePtr, rowIndex);
    }

    public long addEmptyRow() {
        checkImmutable();
        if (hasPrimaryKey()) {
            long primaryKeyColumnIndex = getPrimaryKey();
            RealmFieldType type = getColumnType(primaryKeyColumnIndex);
            switch (type) {
                case STRING:
                    if (findFirstString(primaryKeyColumnIndex, STRING_DEFAULT_VALUE) != NO_MATCH) {
                        throwDuplicatePrimaryKeyException(STRING_DEFAULT_VALUE);
                    }
                    break;
                case INTEGER:
                    if (findFirstLong(primaryKeyColumnIndex, INTEGER_DEFAULT_VALUE) != NO_MATCH) {
                        throwDuplicatePrimaryKeyException(INTEGER_DEFAULT_VALUE);
                    }
                    break;

                default:
                    throw new RealmException("Cannot check for duplicate rows for unsupported primary key type: " + type);
            }
        }

        return nativeAddEmptyRow(nativePtr, 1);
    }

    public long addEmptyRowWithPrimaryKey(Object primaryKeyValue) {
        checkImmutable();
        checkHasPrimaryKey();

        long primaryKeyColumnIndex = getPrimaryKey();
        RealmFieldType type = getColumnType(primaryKeyColumnIndex);
        long rowIndex;
        UncheckedRow row;

        // Add with primary key initially set
        switch (type) {
            case STRING:
                if (!(primaryKeyValue instanceof String)) {
                    throw new IllegalArgumentException("Primary key value is not a String: " + primaryKeyValue);
                }
                if (findFirstString(primaryKeyColumnIndex, (String)primaryKeyValue) != NO_MATCH) {
                    throwDuplicatePrimaryKeyException(primaryKeyValue);
                }
                rowIndex = nativeAddEmptyRow(nativePtr, 1);
                row = getUncheckedRow(rowIndex);
                row.setString(primaryKeyColumnIndex, (String) primaryKeyValue);
                break;

            case INTEGER:
                long pkValue;
                try {
                    pkValue = Long.parseLong(primaryKeyValue.toString());
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("Primary key value is not a long: " + primaryKeyValue);
                }
                if (findFirstLong(primaryKeyColumnIndex, pkValue) != NO_MATCH) {
                    throwDuplicatePrimaryKeyException(pkValue);
                }
                rowIndex = nativeAddEmptyRow(nativePtr, 1);
                row = getUncheckedRow(rowIndex);
                row.setLong(primaryKeyColumnIndex, pkValue);
                break;

            default:
                throw new RealmException("Cannot check for duplicate rows for unsupported primary key type: " + type);
        }

        return rowIndex;
    }

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
     */
    protected long add(Object... values) {
        long rowIndex = addEmptyRow();

        checkImmutable();

        // Check values types
        int columns = (int)getColumnCount();
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
                //String representation of the provided value type
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

        // Insert values
        for (long columnIndex = 0; columnIndex < columns; columnIndex++) {
            Object value = values[(int)columnIndex];
            switch (colTypes[(int)columnIndex]) {
            case BOOLEAN:
                nativeSetBoolean(nativePtr, columnIndex, rowIndex, (Boolean)value);
                break;
            case INTEGER:
                long intValue = ((Number) value).longValue();
                checkIntValueIsLegal(columnIndex, rowIndex, intValue);
                nativeSetLong(nativePtr, columnIndex, rowIndex, intValue);
                break;
            case FLOAT:
                nativeSetFloat(nativePtr, columnIndex, rowIndex, (Float) value);
                break;
            case DOUBLE:
                nativeSetDouble(nativePtr, columnIndex, rowIndex, (Double) value);
                break;
            case STRING:
                String stringValue = (String) value;
                checkStringValueIsLegal(columnIndex, rowIndex, stringValue);
                nativeSetString(nativePtr, columnIndex, rowIndex, (String) value);
                break;
            case DATE:
                if (value == null)
                    throw new IllegalArgumentException("Null Date is not allowed.");
                nativeSetDate(nativePtr, columnIndex, rowIndex, ((Date) value).getTime() / 1000);
                break;
            case UNSUPPORTED_MIXED:
                if (value == null)
                    throw new IllegalArgumentException("Null Mixed data is not allowed");
                nativeSetMixed(nativePtr, columnIndex, rowIndex, Mixed.mixedValue(value));
                break;
            case BINARY:
                if (value == null)
                    throw new IllegalArgumentException("Null Array is not allowed");
                nativeSetByteArray(nativePtr, columnIndex, rowIndex, (byte[])value);
                break;
            case UNSUPPORTED_TABLE:
                insertSubTable(columnIndex, rowIndex, value);
                break;
            default:
                throw new RuntimeException("Unexpected columnType: " + String.valueOf(colTypes[(int)columnIndex]));
            }
        }
        return rowIndex;
    }

    private boolean isPrimaryKeyColumn(long columnIndex) {
        return columnIndex == getPrimaryKey();
    }

    /**
     * Returns a view sorted by the specified column and order.
     *
     * @param columnIndex the column index.
     * @param sortOrder the sort order.
     * @return a sorted view.
     */
    public TableView getSortedView(long columnIndex, Sort sortOrder){
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeGetSortedView(nativePtr, columnIndex, sortOrder.getValue());
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    /**
     * Returns a view sorted by the specified column by the default order.
     *
     * @param columnIndex the column index.
     * @return a sorted view.
     */
    public TableView getSortedView(long columnIndex) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeGetSortedView(nativePtr, columnIndex, true);
        return new TableView(this.context, this, nativeViewPtr);
    }

    public TableView getSortedView(long columnIndices[], Sort sortOrders[]) {
        context.executeDelayedDisposal();
        boolean[] nativeSortOrder = new boolean[sortOrders.length];
        for (int i = 0; i < sortOrders.length; i++) {
            nativeSortOrder[i] = sortOrders[i].getValue();
        }
        long nativeViewPtr = nativeGetSortedViewMulti(nativePtr, columnIndices, nativeSortOrder);
        return new TableView(this.context, this, nativeViewPtr);
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
                return NO_PRIMARY_KEY; // Free table = No primary key
            }

            String tableName = getName();
            if (tableName.startsWith(TABLE_PREFIX)) {
                tableName = tableName.substring(TABLE_PREFIX.length());
            }
            long rowIndex = pkTable.findFirstString(PRIMARY_KEY_CLASS_COLUMN_INDEX, tableName);
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
     * @param columnIndex the ndex of column in the table.
     * @return {@code true} if column is a primary key, {@code false} otherwise.
     */
    public boolean isPrimaryKey(long columnIndex) {
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
            if (rowIndex != rowToUpdate && rowIndex != TableOrView.NO_MATCH) {
                throwDuplicatePrimaryKeyException(value);
            }
        }
    }

    void checkIntValueIsLegal(long columnIndex, long rowToUpdate, long value) {
        if (isPrimaryKeyColumn(columnIndex)) {
            long rowIndex = findFirstLong(columnIndex, value);
            if (rowIndex != rowToUpdate && rowIndex != TableOrView.NO_MATCH) {
                throwDuplicatePrimaryKeyException(value);
            }
        }
    }

    private void throwDuplicatePrimaryKeyException(Object value) {
        throw new RealmPrimaryKeyConstraintException("Value already exists: " + value);
    }

    //
    // Getters
    //

    @Override
    public long getLong(long columnIndex, long rowIndex) {
        return nativeGetLong(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public boolean getBoolean(long columnIndex, long rowIndex) {
        return nativeGetBoolean(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public float getFloat(long columnIndex, long rowIndex) {
        return nativeGetFloat(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public double getDouble(long columnIndex, long rowIndex) {
        return nativeGetDouble(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public Date getDate(long columnIndex, long rowIndex) {
        return new Date(nativeGetDateTime(nativePtr, columnIndex, rowIndex)*1000);
    }

    /**
     * Gets the value of a (string )cell.
     *
     * @param columnIndex 0 based index value of the column
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell
     */
    @Override
    public String getString(long columnIndex, long rowIndex) {
        return nativeGetString(nativePtr, columnIndex, rowIndex);
    }

    /**
     * Gets the value of a (binary) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index value of the cell row.
     * @return value of the particular cell.
     */
    /*
    @Override
    public ByteBuffer getBinaryByteBuffer(long columnIndex, long rowIndex) {
        return nativeGetByteBuffer(nativePtr, columnIndex, rowIndex);
    }

    protected native ByteBuffer nativeGetByteBuffer(long nativeTablePtr, long columnIndex, long rowIndex);
     */

    @Override
    public byte[] getBinaryByteArray(long columnIndex, long rowIndex) {
        return nativeGetByteArray(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public Mixed getMixed(long columnIndex, long rowIndex) {
        return nativeGetMixed(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public RealmFieldType getMixedType(long columnIndex, long rowIndex) {
        return RealmFieldType.fromNativeValue(nativeGetMixedType(nativePtr, columnIndex, rowIndex));
    }

    public long getLink(long columnIndex, long rowIndex) {
        return nativeGetLink(nativePtr, columnIndex, rowIndex);
    }

    public Table getLinkTarget(long columnIndex) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeTablePointer = nativeGetLinkTarget(nativePtr, columnIndex);
        try {
            // Copy context reference from parent
            return new Table(context, this.parent, nativeTablePointer);
        }
        catch (RuntimeException e) {
            Table.nativeClose(nativeTablePointer);
            throw e;
        }
    }

    /**
     *
     * Note: The subtable returned will have to be closed again after use.
     * You can let javas garbage collector handle that or better yet call close() after use.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @return the TableBase the subtable at the requested.
     */
    @Override
    public Table getSubtable(long columnIndex, long rowIndex) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeSubtablePtr = nativeGetSubtable(nativePtr, columnIndex, rowIndex);
        try {
            // Copy context reference from parent
            return new Table(context, this, nativeSubtablePtr);
        }
        catch (RuntimeException e) {
            nativeClose(nativeSubtablePtr);
            throw e;
        }
    }

    // Below version will allow to getSubtable when number of available rows are not updated yet -
    // which happens before an insertDone().

    private Table getSubtableDuringInsert(long columnIndex, long rowIndex) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeSubtablePtr =  nativeGetSubtableDuringInsert(nativePtr, columnIndex, rowIndex);
        try {
            return new Table(context, this, nativeSubtablePtr);
        }
        catch (RuntimeException e) {
            nativeClose(nativeSubtablePtr);
            throw e;
        }
    }

    public long getSubtableSize(long columnIndex, long rowIndex) {
        return nativeGetSubtableSize(nativePtr, columnIndex, rowIndex);
    }

    public void clearSubtable(long columnIndex, long rowIndex) {
        checkImmutable();
        nativeClearSubtable(nativePtr, columnIndex, rowIndex);
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
     *
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

    @Override
    public void setLong(long columnIndex, long rowIndex, long value) {
        checkImmutable();
        checkIntValueIsLegal(columnIndex, rowIndex, value);
        nativeSetLong(nativePtr, columnIndex, rowIndex, value);
    }

    @Override
    public void setBoolean(long columnIndex, long rowIndex, boolean value) {
        checkImmutable();
        nativeSetBoolean(nativePtr, columnIndex, rowIndex, value);
    }

    @Override
    public void setFloat(long columnIndex, long rowIndex, float value) {
        checkImmutable();
        nativeSetFloat(nativePtr, columnIndex, rowIndex, value);
    }

    @Override
    public void setDouble(long columnIndex, long rowIndex, double value) {
        checkImmutable();
        nativeSetDouble(nativePtr, columnIndex, rowIndex, value);
    }

    @Override
    public void setDate(long columnIndex, long rowIndex, Date date) {
        if (date == null)
            throw new IllegalArgumentException("Null Date is not allowed.");
        checkImmutable();
        nativeSetDate(nativePtr, columnIndex, rowIndex, date.getTime() / 1000);
    }

    @Override
    public void setString(long columnIndex, long rowIndex, String value) {
        checkImmutable();
        checkStringValueIsLegal(columnIndex, rowIndex, value);
        nativeSetString(nativePtr, columnIndex, rowIndex, value);
    }

    /**
     * Sets the value for a (binary) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param data the ByteBuffer must be allocated with {@code ByteBuffer.allocateDirect(len)}.
     */

    /*
    @Override
    public void setBinaryByteBuffer(long columnIndex, long rowIndex, ByteBuffer data) {
        if (immutable) throwImmutable();
        if (data == null)
            throw new IllegalArgumentException("Null array");
        if (data.isDirect())
            nativeSetByteBuffer(nativePtr, columnIndex, rowIndex, data);
        else
            throw new RuntimeException("Currently ByteBuffer must be allocateDirect()."); // FIXME: support other than allocateDirect
    }

    protected native void nativeSetByteBuffer(long nativeTablePtr, long columnIndex, long rowIndex, ByteBuffer data);
     */


    @Override
    public void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data) {
        checkImmutable();
        nativeSetByteArray(nativePtr, columnIndex, rowIndex, data);
    }

    /**
     * Sets the value for a (mixed typed) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param data the value.
     */
    @Override
    public void setMixed(long columnIndex, long rowIndex, Mixed data) {
        checkImmutable();
        if (data == null)
            throw new IllegalArgumentException();
        nativeSetMixed(nativePtr, columnIndex, rowIndex, data);
    }

    public void setLink(long columnIndex, long rowIndex, long value) {
        checkImmutable();
        nativeSetLink(nativePtr, columnIndex, rowIndex, value);
    }

    //TODO: Clean up this function
    private void insertSubTable(long columnIndex, long rowIndex, Object value) {
        checkImmutable();
        if (value != null) {
            // insert rows in subtable recursively
            Table subtable = getSubtableDuringInsert(columnIndex, rowIndex);
            int rows = ((Object[])value).length;
            for (int i=0; i<rows; ++i) {
                Object rowArr = ((Object[])value)[i];
                subtable.add((Object[])rowArr);
            }
        }
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
     * Define a primary key for this table. This needs to be called manually before inserting data into the table.
     *
     * @param columnName the name of the field that will function primary key. "" or {@code null} will remove any
     *                   previous set magic key.
     * @throws {@link io.realm.exceptions.RealmException} if it is not possible to set the primary key due to the column
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
        Group group = getTableGroup();
        if (group == null) {
            return null;
        }

        Table pkTable = group.getTable(PRIMARY_KEY_TABLE_NAME);
        if (pkTable.getColumnCount() == 0) {
            pkTable.addColumn(RealmFieldType.STRING, PRIMARY_KEY_CLASS_COLUMN_NAME);
            pkTable.addColumn(RealmFieldType.STRING, PRIMARY_KEY_FIELD_COLUMN_NAME);
        } else {
            migratePrimaryKeyTableIfNeeded(group, pkTable);
        }

        return pkTable;
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
    private void migratePrimaryKeyTableIfNeeded(Group group, Table pkTable) {
        nativeMigratePrimaryKeyTableIfNeeded(group.nativePtr, pkTable.nativePtr);
    }

    // Recursively look at parents until either a Group or null is found
    Group getTableGroup() {
        if (parent instanceof Group)  {
            return (Group) parent;
        } else if (parent instanceof Table) {
            return ((Table) parent).getTableGroup();
        } else {
            return null; // Free table
        }
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
        if (!(parent instanceof Table)) {
            return parent != null && ((Group) parent).immutable;
        } else {
            return ((Table)parent).isImmutable();
        }
    }

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
    // Aggregate functions
    //

    // Integers
    @Override
    public long sumLong(long columnIndex) {
        return nativeSumInt(nativePtr, columnIndex);
    }

    @Override
    public Long maximumLong(long columnIndex) {
        return nativeMaximumInt(nativePtr, columnIndex);
    }

    @Override
    public Long minimumLong(long columnIndex) {
        return nativeMinimumInt(nativePtr, columnIndex);
    }

    @Override
    public double averageLong(long columnIndex) {
        return nativeAverageInt(nativePtr, columnIndex);
    }

    // Floats
    @Override
    public double sumFloat(long columnIndex) {
        return nativeSumFloat(nativePtr, columnIndex);
    }

    @Override
    public Float maximumFloat(long columnIndex) {
        return nativeMaximumFloat(nativePtr, columnIndex);
    }

    @Override
    public Float minimumFloat(long columnIndex) {
        return nativeMinimumFloat(nativePtr, columnIndex);
    }

    @Override
    public double averageFloat(long columnIndex) {
        return nativeAverageFloat(nativePtr, columnIndex);
    }

    // Doubles
    @Override
    public double sumDouble(long columnIndex) {
        return nativeSumDouble(nativePtr, columnIndex);
    }

    @Override
    public Double maximumDouble(long columnIndex) {
        return nativeMaximumDouble(nativePtr, columnIndex);
    }

    @Override
    public Double minimumDouble(long columnIndex) {
        return nativeMinimumDouble(nativePtr, columnIndex);
    }

    @Override
    public double averageDouble(long columnIndex) {
        return nativeAverageDouble(nativePtr, columnIndex);
    }

    // Date aggregates

    @Override
    public Date maximumDate(long columnIndex) {
        return new Date(nativeMaximumDate(nativePtr, columnIndex) * 1000);
    }

    @Override
    public Date minimumDate(long columnIndex) {
        return new Date(nativeMinimumDate(nativePtr, columnIndex) * 1000);
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

    @Override
    public long count(long columnIndex, String value) {
        return nativeCountString(nativePtr, columnIndex, value);
    }

    //
    // Searching methods.
    //

    @Override
    public TableQuery where() {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeQueryPtr = nativeWhere(nativePtr);
        try {
            // Copy context reference from parent
            return new TableQuery(this.context, this, nativeQueryPtr);
        } catch (RuntimeException e) {
            TableQuery.nativeClose(nativeQueryPtr);
            throw e;
        }
    }

    @Override
    public long findFirstLong(long columnIndex, long value) {
        return nativeFindFirstInt(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstBoolean(long columnIndex, boolean value) {
        return nativeFindFirstBool(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstFloat(long columnIndex, float value) {
        return nativeFindFirstFloat(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstDouble(long columnIndex, double value) {
        return nativeFindFirstDouble(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstDate(long columnIndex, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstDate(nativePtr, columnIndex, date.getTime() / 1000);
    }

    @Override
    public long findFirstString(long columnIndex, String value) {
        if (value == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstString(nativePtr, columnIndex, value);
    }

    @Override
    public TableView findAllLong(long columnIndex, long value) {
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllInt(nativePtr, columnIndex, value);
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    @Override
    public TableView findAllBoolean(long columnIndex, boolean value) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllBool(nativePtr, columnIndex, value);
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    @Override
    public TableView findAllFloat(long columnIndex, float value) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllFloat(nativePtr, columnIndex, value);
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    @Override
    public TableView findAllDouble(long columnIndex, double value) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllDouble(nativePtr, columnIndex, value);
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    @Override
    public TableView findAllDate(long columnIndex, Date date) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllDate(nativePtr, columnIndex, date.getTime() / 1000);
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    @Override
    public TableView findAllString(long columnIndex, String value) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllString(nativePtr, columnIndex, value);
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    // Experimental feature
    @Override
    public long lowerBoundLong(long columnIndex, long value) {
        return nativeLowerBoundInt(nativePtr, columnIndex, value);
    }
    @Override
    public long upperBoundLong(long columnIndex, long value) {
        return nativeUpperBoundInt(nativePtr, columnIndex, value);
    }

    @Override
    public Table pivot(long stringCol, long intCol, PivotType pivotType) {
        if (! this.getColumnType(stringCol).equals(RealmFieldType.STRING ))
            throw new UnsupportedOperationException("Group by column must be of type String");
        if (! this.getColumnType(intCol).equals(RealmFieldType.INTEGER ))
            throw new UnsupportedOperationException("Aggregation column must be of type Int");
        Table result = new Table();
        nativePivot(nativePtr, stringCol, intCol, pivotType.value, result.nativePtr);
        return result;
    }

    //

    public TableView getDistinctView(long columnIndex) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        this.context.executeDelayedDisposal();
        long nativeViewPtr = nativeGetDistinctView(nativePtr, columnIndex);
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    /**
     * Return the table name as it is in the associated group.
     *
     * @return Name of the the table or null if it not part of a group.
     */
    public String getName() {
        return nativeGetName(nativePtr);
    }


    // Optimize
    public void optimize() {
        checkImmutable();
        nativeOptimize(nativePtr);
    }

    @Override
    public String toJson() {
        return nativeToJson(nativePtr);
    }

    @Override
    public String toString() {
        return nativeToString(nativePtr, INFINITE);
    }

    @Override
    public String toString(long maxRows) {
        return nativeToString(nativePtr, maxRows);
    }

    @Override
    public String rowToString(long rowIndex) {
        return nativeRowToString(nativePtr, rowIndex);
    }

    @Override
    public long sync() {
        throw new RuntimeException("Not supported for tables");
    }

    private void throwImmutable() {
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
     * Checks if a given table name is a meta-table, i.e. a table used by Realm to track its internal state.
     */
    public static boolean isMetaTable(String tableName) {
        return (tableName.equals(METADATA_TABLE_NAME) || tableName.equals(PRIMARY_KEY_TABLE_NAME));
    }

    /**
     * Report the current versioning counter for the table. The versioning counter is guaranteed to
     * change when the contents of the table changes after advance_read() or promote_to_write(), or
     * immediately after calls to methods which change the table.
     *
     * @return version_counter for the table.
     */
    public long version() {
        return nativeVersion(nativePtr);
    }

    protected native long createNative();
    static native void nativeClose(long nativeTablePtr);
    private native boolean nativeIsValid(long nativeTablePtr);
    private native boolean nativeIsRootTable(long nativeTablePtr);
    private native long nativeAddColumn(long nativeTablePtr, int type, String name, boolean isNullable);
    private native long nativeAddColumnLink(long nativeTablePtr, int type, String name, long targetTablePtr);
    private native void nativeRenameColumn(long nativeTablePtr, long columnIndex, String name);
    private native void nativeRemoveColumn(long nativeTablePtr, long columnIndex);
    private native boolean nativeIsColumnNullable(long nativePtr, long columnIndex);
    private native void nativeConvertColumnToNullable(long nativeTablePtr, long columnIndex);
    private native void nativeConvertColumnToNotNullable(long nativePtr, long columnIndex);
    private native void nativeUpdateFromSpec(long nativeTablePtr, TableSpec tableSpec);
    private native long nativeSize(long nativeTablePtr);
    private native void nativeClear(long nativeTablePtr);
    private native long nativeGetColumnCount(long nativeTablePtr);
    private native TableSpec nativeGetTableSpec(long nativeTablePtr);
    private native String nativeGetColumnName(long nativeTablePtr, long columnIndex);
    private native long nativeGetColumnIndex(long nativeTablePtr, String columnName);
    private native int nativeGetColumnType(long nativeTablePtr, long columnIndex);
    private native void nativeRemove(long nativeTablePtr, long rowIndex);
    private native void nativeRemoveLast(long nativeTablePtr);
    private native void nativeMoveLastOver(long nativeTablePtr, long rowIndex);
    private native long nativeAddEmptyRow(long nativeTablePtr, long rows);
    private native long nativeGetSortedView(long nativeTableViewPtr, long columnIndex, boolean ascending);
    private native long nativeGetSortedViewMulti(long nativeTableViewPtr, long[] columnIndices, boolean[] ascending);
    private native long nativeGetLong(long nativeTablePtr, long columnIndex, long rowIndex);
    private native boolean nativeGetBoolean(long nativeTablePtr, long columnIndex, long rowIndex);
    private native float nativeGetFloat(long nativeTablePtr, long columnIndex, long rowIndex);
    private native double nativeGetDouble(long nativeTablePtr, long columnIndex, long rowIndex);
    private native long nativeGetDateTime(long nativeTablePtr, long columnIndex, long rowIndex);
    private native String nativeGetString(long nativePtr, long columnIndex, long rowIndex);
    private native byte[] nativeGetByteArray(long nativePtr, long columnIndex, long rowIndex);
    private native int nativeGetMixedType(long nativePtr, long columnIndex, long rowIndex);
    private native Mixed nativeGetMixed(long nativeTablePtr, long columnIndex, long rowIndex);
    private native long nativeGetLink(long nativePtr, long columnIndex, long rowIndex);
    private native long nativeGetLinkTarget(long nativePtr, long columnIndex);
    private native long nativeGetSubtable(long nativeTablePtr, long columnIndex, long rowIndex);
    private native long nativeGetSubtableDuringInsert(long nativeTablePtr, long columnIndex, long rowIndex);
    private native long nativeGetSubtableSize(long nativeTablePtr, long columnIndex, long rowIndex);
    private native void nativeClearSubtable(long nativeTablePtr, long columnIndex, long rowIndex);
    native long nativeGetRowPtr(long nativePtr, long index);
    private native void nativeSetLong(long nativeTablePtr, long columnIndex, long rowIndex, long value);
    private native void nativeSetBoolean(long nativeTablePtr, long columnIndex, long rowIndex, boolean value);
    private native void nativeSetFloat(long nativeTablePtr, long columnIndex, long rowIndex, float value);
    private native void nativeSetDouble(long nativeTablePtr, long columnIndex, long rowIndex, double value);
    private native void nativeSetDate(long nativeTablePtr, long columnIndex, long rowIndex, long dateTimeValue);
    private native void nativeSetString(long nativeTablePtr, long columnIndex, long rowIndex, String value);
    private native void nativeSetByteArray(long nativePtr, long columnIndex, long rowIndex, byte[] data);
    private native void nativeSetMixed(long nativeTablePtr, long columnIndex, long rowIndex, Mixed data);
    private native void nativeSetLink(long nativeTablePtr, long columnIndex, long rowIndex, long value);
    private native long nativeSetPrimaryKey(long privateKeyTableNativePtr, long nativePtr, String columnName);
    private native void nativeMigratePrimaryKeyTableIfNeeded(long groupNativePtr, long primaryKeyTableNativePtr);
    private native void nativeAddSearchIndex(long nativePtr, long columnIndex);
    private native void nativeRemoveSearchIndex(long nativePtr, long columnIndex);
    private native boolean nativeHasSearchIndex(long nativePtr, long columnIndex);
    private native boolean nativeIsNullLink(long nativePtr, long columnIndex, long rowIndex);
    private native void nativeNullifyLink(long nativePtr, long columnIndex, long rowIndex);
    private native long nativeSumInt(long nativePtr, long columnIndex);
    private native long nativeMaximumInt(long nativePtr, long columnIndex);
    private native long nativeMinimumInt(long nativePtr, long columnIndex);
    private native double nativeAverageInt(long nativePtr, long columnIndex);
    private native double nativeSumFloat(long nativePtr, long columnIndex);
    private native float nativeMaximumFloat(long nativePtr, long columnIndex);
    private native float nativeMinimumFloat(long nativePtr, long columnIndex);
    private native double nativeAverageFloat(long nativePtr, long columnIndex);
    private native double nativeSumDouble(long nativePtr, long columnIndex);
    private native double nativeMaximumDouble(long nativePtr, long columnIndex);
    private native double nativeMinimumDouble(long nativePtr, long columnIndex);
    private native double nativeAverageDouble(long nativePtr, long columnIndex);
    private native long nativeMaximumDate(long nativePtr, long columnIndex);
    private native long nativeMinimumDate(long nativePtr, long columnIndex);
    private native long nativeCountLong(long nativePtr, long columnIndex, long value);
    private native long nativeCountFloat(long nativePtr, long columnIndex, float value);
    private native long nativeCountDouble(long nativePtr, long columnIndex, double value);
    private native long nativeCountString(long nativePtr, long columnIndex, String value);
    private native long nativeWhere(long nativeTablePtr);
    private native long nativeFindFirstInt(long nativeTablePtr, long columnIndex, long value);
    private native long nativeFindFirstBool(long nativePtr, long columnIndex, boolean value);
    private native long nativeFindFirstFloat(long nativePtr, long columnIndex, float value);
    private native long nativeFindFirstDouble(long nativePtr, long columnIndex, double value);
    private native long nativeFindFirstDate(long nativeTablePtr, long columnIndex, long dateTimeValue);
    private native long nativeFindFirstString(long nativeTablePtr, long columnIndex, String value);
    private native long nativeFindAllInt(long nativePtr, long columnIndex, long value);
    private native long nativeFindAllBool(long nativePtr, long columnIndex, boolean value);
    private native long nativeFindAllFloat(long nativePtr, long columnIndex, float value);
    private native long nativeFindAllDouble(long nativePtr, long columnIndex, double value);
    private native long nativeFindAllDate(long nativePtr, long columnIndex, long dateTimeValue);
    private native long nativeFindAllString(long nativePtr, long columnIndex, String value);
    private native long nativeLowerBoundInt(long nativePtr, long columnIndex, long value);
    private native long nativeUpperBoundInt(long nativePtr, long columnIndex, long value);
    private native void nativePivot(long nativeTablePtr, long stringCol, long intCol, int pivotType, long resultPtr);
    private native long nativeGetDistinctView(long nativePtr, long columnIndex);
    private native String nativeGetName(long nativeTablePtr);
    private native void nativeOptimize(long nativeTablePtr);
    private native String nativeToJson(long nativeTablePtr);
    private native String nativeToString(long nativeTablePtr, long maxRows);
    private native boolean nativeHasSameSchema(long thisTable, long otherTable);
    private native long nativeVersion(long nativeTablePtr);
    private native String nativeRowToString(long nativeTablePtr, long rowIndex);
}
