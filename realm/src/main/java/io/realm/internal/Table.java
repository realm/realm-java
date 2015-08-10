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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.exceptions.RealmException;


/**
 * This class is a base class for all Realm tables. The class supports all low
 * level methods (define/insert/delete/update) a table has. All the native
 * communications to the Realm C++ library are also handled by this class.
 */
public class Table implements TableOrView, TableSchema, Closeable {

    public static final String TABLE_PREFIX = "class_";
    public static final long INFINITE = -1;
    public static final String STRING_DEFAULT_VALUE = "";
    public static final long INTEGER_DEFAULT_VALUE = 0;

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
    protected boolean DEBUG = false;
    static AtomicInteger tableCount = new AtomicInteger(0);

    static {
        RealmCore.loadLibrary();
    }


    /**
     * Construct a Table base object. It can be used to register columns in this
     * table. Registering into table is allowed only for empty tables. It
     * creates a native reference of the object and keeps a reference to it.
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
            System.err.println("====== New Tablebase " + tableNo + " : ptr = " + nativePtr);
        }
    }

    protected native long createNative();
    
    Table(Context context, Object parent, long nativePointer) {
        this.context = context;
        this.parent  = parent;
        this.nativePtr = nativePointer;

        if (DEBUG) {
            tableNo = tableCount.incrementAndGet();
            System.err.println("===== New Tablebase(ptr) " + tableNo + " : ptr = " + nativePtr);
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
                    System.err.println("==== CLOSE " + tableNo + " ptr= " + nativePtr + " remaining " + tableCount.get());
                }
                
                nativePtr = 0;
            }   
        }
    }

    protected static native void nativeClose(long nativeTablePtr);
    
    @Override
    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                boolean isRoot = (parent == null);
                context.asyncDisposeTable(nativePtr, isRoot);
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
            }
        }

        if (DEBUG) 
            System.err.println("==== FINALIZE " + tableNo + "...");
    }

    /*
     * Check if the Table is valid.
     * Whenever a Table/subtable is changed/updated all it's subtables are invalidated.
     * You can no longer perform any actions on the table, and if done anyway, an exception is thrown.
     * The only method you can call is 'isValid()'.
     */

    public boolean isValid() {
        return nativePtr != 0 && nativeIsValid(nativePtr);
    }

    protected native boolean nativeIsValid(long nativeTablePtr);

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

    protected native boolean nativeIsRootTable(long nativeTablePtr);

    /**
     * Add a column to the table dynamically.
     * @return Index of the new column.
     */
    @Override
    public long addColumn (ColumnType type, String name) {
        verifyColumnName(name);
        return nativeAddColumn(nativePtr, type.getValue(), name);
    }

    protected native long nativeAddColumn(long nativeTablePtr, int type, String name);

    /**
     * Add a link column to the table dynamically.
     * @return Index of the new column.
     */
    public long addColumnLink (ColumnType type, String name, Table table) {
        verifyColumnName(name);
        return nativeAddColumnLink(nativePtr, type.getValue(), name, table.nativePtr);
    }

    protected native long nativeAddColumnLink(long nativeTablePtr, int type, String name, long targetTablePtr);

    /**
     * Remove a column in the table dynamically.
     */
    @Override
    public void removeColumn(long columnIndex) {
        nativeRemoveColumn(nativePtr, columnIndex);
    }

    protected native void nativeRemoveColumn(long nativeTablePtr, long columnIndex);

    /**
     * Rename a column in the table.
     */
    @Override
    public void renameColumn(long columnIndex, String newName) {
        verifyColumnName(newName);
        nativeRenameColumn(nativePtr, columnIndex, newName);
    }

    protected native void nativeRenameColumn(long nativeTablePtr, long columnIndex, String name);


    /**
     * Updates a table specification from a Table specification structure.
     */
    public void updateFromSpec(TableSpec tableSpec) {
        checkImmutable();
        nativeUpdateFromSpec(nativePtr, tableSpec);
    }

    protected native void nativeUpdateFromSpec(long nativeTablePtr, TableSpec tableSpec);

    // Table Size and deletion. AutoGenerated subclasses are nothing to do with this
    // class.
    /**
     * Get the number of entries/rows of this table.
     *
     * @return The number of rows.
     */
    @Override
    public long size() {
        return nativeSize(nativePtr);
    }

    protected native long nativeSize(long nativeTablePtr);

    /**
     * Checks whether this table is empty or not.
     *
     * @return true if empty, otherwise false.
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

    protected native void nativeClear(long nativeTablePtr);

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

    protected native long nativeGetColumnCount(long nativeTablePtr);


    public TableSpec getTableSpec(){
        return nativeGetTableSpec(nativePtr);
    }

    protected native TableSpec nativeGetTableSpec(long nativeTablePtr);

    /**
     * Returns the name of a column identified by columnIndex. Notice that the
     * index is zero based.
     *
     * @param columnIndex the column index
     * @return the name of the column
     */
    @Override
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePtr, columnIndex);
    }

    protected native String nativeGetColumnName(long nativeTablePtr, long columnIndex);

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name
     * @return the index, {@link #NO_MATCH} if not found
     */
    @Override
    public long getColumnIndex(String columnName) {
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        return nativeGetColumnIndex(nativePtr, columnName);
    }
    
    protected native long nativeGetColumnIndex(long nativeTablePtr, String columnName);


    /**
     * Get the type of a column identified by the columnIdex.
     *
     * @param columnIndex index of the column.
     * @return Type of the particular column.
     */
    @Override
    public ColumnType getColumnType(long columnIndex) {
        return ColumnType.fromNativeValue(nativeGetColumnType(nativePtr, columnIndex));
    }

    protected native int nativeGetColumnType(long nativeTablePtr, long columnIndex);

    /**
     * Removes a row from the specific index. As of now the entry is simply
     * removed from the table.
     *
     * @param rowIndex the row index (starting with 0)
     *
     */
    @Override
    public void remove(long rowIndex) {
        checkImmutable();
        nativeRemove(nativePtr, rowIndex);
    }

    protected native void nativeRemove(long nativeTablePtr, long rowIndex);

    @Override
    public void removeLast() {
        checkImmutable();
        nativeRemoveLast(nativePtr);
    }

    protected native void nativeRemoveLast(long nativeTablePtr);

    public void moveLastOver(long rowIndex) {
        checkImmutable();
        nativeMoveLastOver(nativePtr, rowIndex);
    }

    protected native void nativeMoveLastOver(long nativeTablePtr, long rowIndex);

    public long addEmptyRow() {
        checkImmutable();
        if (hasPrimaryKey()) {
            long primaryKeyColumnIndex = getPrimaryKey();
            ColumnType type = getColumnType(primaryKeyColumnIndex);
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
        ColumnType type = getColumnType(primaryKeyColumnIndex);
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

    protected native long nativeAddEmptyRow(long nativeTablePtr, long rows);


    /**
     * Appends the specified row to the end of the table.
     * For internal testing usage only.
     *
     * @param values
     * @return The row index of the appended row
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
        ColumnType colTypes[] = new ColumnType[columns];
        for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
            Object value = values[columnIndex];
            ColumnType colType = getColumnType(columnIndex);
            colTypes[columnIndex] = colType;
            if (!colType.matchObject(value)) {
                //String representation of the provided value type
                String providedType;
                if (value == null)
                    providedType = "null";
                else
                    providedType = value.getClass().toString();

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
                nativeSetString(nativePtr, columnIndex, rowIndex, (String)value);
                break;
            case DATE:
                if (value == null)
                    throw new IllegalArgumentException("Null Date is not allowed.");
                nativeSetDate(nativePtr, columnIndex, rowIndex, ((Date)value).getTime()/1000);
                break;
            case MIXED:
                if (value == null)
                    throw new IllegalArgumentException("Null Mixed data is not allowed");
                nativeSetMixed(nativePtr, columnIndex, rowIndex, Mixed.mixedValue(value));
                break;
            case BINARY:
                if (value == null)
                    throw new IllegalArgumentException("Null Array is not allowed");
                nativeSetByteArray(nativePtr, columnIndex, rowIndex, (byte[])value);
                break;
            case TABLE:
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
     * Returns a view sorted by the specified column and order
     * @param columnIndex
     * @param order
     * @return
     */
    public TableView getSortedView(long columnIndex, TableView.Order order){
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeGetSortedView(nativePtr, columnIndex, (order == TableView.Order.ascending));
        try {
            return new TableView(this.context, this, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    /**
     * Returns a view sorted by the specified column by the default order
     * @param columnIndex
     * @return
     */
    public TableView getSortedView(long columnIndex) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeGetSortedView(nativePtr, columnIndex, true);
        return new TableView(this.context, this, nativeViewPtr);
    }

    protected native long nativeGetSortedView(long nativeTableViewPtr, long columnIndex, boolean ascending);


    public TableView getSortedView(long columnIndices[], boolean orders[]) {
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeGetSortedViewMulti(nativePtr, columnIndices, orders);
        return new TableView(this.context, this, nativeViewPtr);
    }

    protected native long nativeGetSortedViewMulti(long nativeTableViewPtr, long[] columnIndices, boolean[] ascending);

    /**
     * Returns the column index for the primary key.
     *
     * @return Column index or {@code #NO_MATCH} if no primary key is set.
     */
    public long getPrimaryKey() {
        if (cachedPrimaryKeyColumnIndex >= 0 || cachedPrimaryKeyColumnIndex == NO_PRIMARY_KEY) {
            return cachedPrimaryKeyColumnIndex;
        } else {
            Table pkTable = getPrimaryKeyTable();
            if (pkTable == null) {
                return NO_PRIMARY_KEY; // Free table = No primary key
            }
            long rowIndex = pkTable.findFirstString(PRIMARY_KEY_CLASS_COLUMN_INDEX, getName());
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
     * @param columnIndex   Index of column in the table.
     * @return              True if column is a primary key, false otherwise.
     */
    public boolean isPrimaryKey(long columnIndex) {
        return columnIndex >= 0 && columnIndex == getPrimaryKey();
    }

    /**
     * Check if a table has a primary key.
     * @return True if primary key is defined, false otherwise.
     */
    public boolean hasPrimaryKey() {
        return getPrimaryKey() >= 0;
    }

    void checkStringValueIsLegal(long columnIndex, long rowToUpdate, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null String is not allowed.");
        }
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
        throw new RealmException("Primary key constraint broken. Value already exists: " + value);
    }

    //
    // Getters
    //

    @Override
    public long getLong(long columnIndex, long rowIndex) {
        return nativeGetLong(nativePtr, columnIndex, rowIndex);
    }

    protected native long nativeGetLong(long nativeTablePtr, long columnIndex, long rowIndex);

    @Override
    public boolean getBoolean(long columnIndex, long rowIndex) {
        return nativeGetBoolean(nativePtr, columnIndex, rowIndex);
    }

    protected native boolean nativeGetBoolean(long nativeTablePtr, long columnIndex, long rowIndex);

    @Override
    public float getFloat(long columnIndex, long rowIndex) {
        return nativeGetFloat(nativePtr, columnIndex, rowIndex);
    }

    protected native float nativeGetFloat(long nativeTablePtr, long columnIndex, long rowIndex);

    @Override
    public double getDouble(long columnIndex, long rowIndex) {
        return nativeGetDouble(nativePtr, columnIndex, rowIndex);
    }

    protected native double nativeGetDouble(long nativeTablePtr, long columnIndex, long rowIndex);

    @Override
    public Date getDate(long columnIndex, long rowIndex) {
        return new Date(nativeGetDateTime(nativePtr, columnIndex, rowIndex)*1000);
    }

    protected native long nativeGetDateTime(long nativeTablePtr, long columnIndex, long rowIndex);

    /**
     * Get the value of a (string )cell.
     *
     * @param columnIndex
     *            0 based index value of the column
     * @param rowIndex
     *            0 based index of the row.
     * @return value of the particular cell
     */
    @Override
    public String getString(long columnIndex, long rowIndex) {
        return nativeGetString(nativePtr, columnIndex, rowIndex);
    }

    protected native String nativeGetString(long nativePtr, long columnIndex, long rowIndex);

    /**
     * Get the value of a (binary) cell.
     *
     * @param columnIndex
     *            0 based index value of the cell column
     * @param rowIndex
     *            0 based index value of the cell row
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

    protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex, long rowIndex);

    @Override
    public Mixed getMixed(long columnIndex, long rowIndex) {
        return nativeGetMixed(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public ColumnType getMixedType(long columnIndex, long rowIndex) {
        return ColumnType.fromNativeValue(nativeGetMixedType(nativePtr, columnIndex, rowIndex));
    }

    protected native int nativeGetMixedType(long nativePtr, long columnIndex, long rowIndex);

    protected native Mixed nativeGetMixed(long nativeTablePtr, long columnIndex, long rowIndex);

    public long getLink(long columnIndex, long rowIndex) {
        return nativeGetLink(nativePtr, columnIndex, rowIndex);
    }

    protected native long nativeGetLink(long nativePtr, long columnIndex, long rowIndex);


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

    protected native long nativeGetLinkTarget(long nativePtr, long columnIndex);


    /**
     *
     * Note: The subtable returned will have to be closed again after use.
     * You can let javas garbage collector handle that or better yet call close()
     * after use.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @return TableBase the subtable at the requested cell
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

    protected native long nativeGetSubtable(long nativeTablePtr, long columnIndex, long rowIndex);

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

    private native long nativeGetSubtableDuringInsert(long nativeTablePtr, long columnIndex, long rowIndex);


    public long getSubtableSize(long columnIndex, long rowIndex) {
        return nativeGetSubtableSize(nativePtr, columnIndex, rowIndex);
    }

    protected native long nativeGetSubtableSize(long nativeTablePtr, long columnIndex, long rowIndex);

    public void clearSubtable(long columnIndex, long rowIndex) {
        checkImmutable();
        nativeClearSubtable(nativePtr, columnIndex, rowIndex);
    }

    protected native void nativeClearSubtable(long nativeTablePtr, long columnIndex, long rowIndex);

    /**
     * Returns a non-checking Row. Incorrect use of this Row will cause a hard core crash.
     * If error checking is required, use {@link #getCheckedRow(long)} instead.
     *
     * @param index Index of row to fetch.
     * @return Unsafe row wrapper object.
     */
    public UncheckedRow getUncheckedRow(long index) {
        return UncheckedRow.get(context, this, index);
    }

    /**
     * Returns a wrapper around Row access. All access will be error checked in JNI and will throw an
     * appropriate {@link RuntimeException} if used incorrectly.
     *
     * If error checking is done elsewhere, consider using {@link #getUncheckedRow(long)} for better performance.
     *
     * @param index Index of row to fetch./
     * @return Safe row wrapper object.
     */
    public CheckedRow getCheckedRow(long index) {
        return CheckedRow.get(context, this, index);
    }

    protected native long nativeGetRowPtr(long nativePtr, long index);


    //
    // Setters
    //

    @Override
    public void setLong(long columnIndex, long rowIndex, long value) {
        checkImmutable();
        checkIntValueIsLegal(columnIndex, rowIndex, value);
        nativeSetLong(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetLong(long nativeTablePtr, long columnIndex, long rowIndex, long value);

    @Override
    public void setBoolean(long columnIndex, long rowIndex, boolean value) {
        checkImmutable();
        nativeSetBoolean(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetBoolean(long nativeTablePtr, long columnIndex, long rowIndex, boolean value);

    @Override
    public void setFloat(long columnIndex, long rowIndex, float value) {
        checkImmutable();
        nativeSetFloat(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetFloat(long nativeTablePtr, long columnIndex, long rowIndex, float value);

    @Override
    public void setDouble(long columnIndex, long rowIndex, double value) {
        checkImmutable();
        nativeSetDouble(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetDouble(long nativeTablePtr, long columnIndex, long rowIndex, double value);

    @Override
    public void setDate(long columnIndex, long rowIndex, Date date) {
        if (date == null)
            throw new IllegalArgumentException("Null Date is not allowed.");
        checkImmutable();
        nativeSetDate(nativePtr, columnIndex, rowIndex, date.getTime() / 1000);
    }

    protected native void nativeSetDate(long nativeTablePtr, long columnIndex, long rowIndex, long dateTimeValue);

    @Override
    public void setString(long columnIndex, long rowIndex, String value) {
        checkImmutable();
        checkStringValueIsLegal(columnIndex, rowIndex, value);
        nativeSetString(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetString(long nativeTablePtr, long columnIndex, long rowIndex, String value);

    /**
     * Sets the value for a (binary) cell.
     *
     * @param columnIndex
     *            column index of the cell
     * @param rowIndex
     *            row index of the cell
     * @param data
     *            the ByteBuffer must be allocated with ByteBuffer.allocateDirect(len)
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
        if (data == null)
            throw new IllegalArgumentException("Null Array");
        nativeSetByteArray(nativePtr, columnIndex, rowIndex, data);
    }

    protected native void nativeSetByteArray(long nativePtr, long columnIndex, long rowIndex, byte[] data);

    /**
     * Sets the value for a (mixed typed) cell.
     *
     * @param columnIndex
     *            column index of the cell
     * @param rowIndex
     *            row index of the cell
     * @param data
     */
    @Override
    public void setMixed(long columnIndex, long rowIndex, Mixed data) {
        checkImmutable();
        if (data == null)
            throw new IllegalArgumentException();
        nativeSetMixed(nativePtr, columnIndex, rowIndex, data);
    }

    protected native void nativeSetMixed(long nativeTablePtr, long columnIndex, long rowIndex, Mixed data);

    public void setLink(long columnIndex, long rowIndex, long value) {
        checkImmutable();
        nativeSetLink(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetLink(long nativeTablePtr, long columnIndex, long rowIndex, long value);

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
     * Define a primary key for this table. This needs to be called manually before inserting data
     * into the table.
     *
     * @param columnName    Name of the field that will function primary key. "" or <code>null</code>
     *                      will remove any previous set magic key.
     *
     * @throws              {@link io.realm.exceptions.RealmException} if it is not possible to set
     *                      the primary key due to the column not having distinct values (ie.
     *                      violating the primary key constraint).
     */
    public void setPrimaryKey(String columnName) {
        Table pkTable = getPrimaryKeyTable();
        if (pkTable == null) {
            throw new RealmException("Primary keys are only supported if Table is part of a Group");
        }
        cachedPrimaryKeyColumnIndex = nativeSetPrimaryKey(pkTable.nativePtr, nativePtr, columnName);
    }

    private native long nativeSetPrimaryKey(long privateKeyTableNativePtr, long nativePtr, String columnName);

    private Table getPrimaryKeyTable() {
        Group group = getTableGroup();
        if (group == null) {
            return null;
        }

        Table pkTable = group.getTable(PRIMARY_KEY_TABLE_NAME);
        if (pkTable.getColumnCount() == 0) {
            pkTable.addColumn(ColumnType.STRING, PRIMARY_KEY_CLASS_COLUMN_NAME);
            pkTable.addColumn(ColumnType.STRING, PRIMARY_KEY_FIELD_COLUMN_NAME);
        } else {
            migratePrimaryKeyTableIfNeeded(group, pkTable);
        }

        return pkTable;
    }

    // Migration required to fix https://github.com/realm/realm-java/issues/1059
    // This will convert INTEGER column to the corresponding STRING column if needed.
    // Any database created on Realm-Java 0.80.1 and below will have this error.
    private void migratePrimaryKeyTableIfNeeded(Group group, Table pkTable) {
        nativeMigratePrimaryKeyTableIfNeeded(group.nativePtr, pkTable.nativePtr);
    }

    private native void nativeMigratePrimaryKeyTableIfNeeded(long groupNativePtr, long primaryKeyTableNativePtr);

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

    protected native void nativeAddSearchIndex(long nativePtr, long columnIndex);

    protected native void nativeRemoveSearchIndex(long nativePtr, long columnIndex);

    public boolean hasSearchIndex(long columnIndex) {
        return nativeHasSearchIndex(nativePtr, columnIndex);
    }

    protected native boolean nativeHasSearchIndex(long nativePtr, long columnIndex);


    public boolean isNullLink(long columnIndex, long rowIndex) {
        return nativeIsNullLink(nativePtr, columnIndex, rowIndex);
    }

    protected native boolean nativeIsNullLink(long nativePtr, long columnIndex, long rowIndex);

    public void nullifyLink(long columnIndex, long rowIndex) {
        nativeNullifyLink(nativePtr, columnIndex, rowIndex);
    }

    protected native void nativeNullifyLink(long nativePtr, long columnIndex, long rowIndex);


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

    protected native long nativeSumInt(long nativePtr, long columnIndex);

    @Override
    public long maximumLong(long columnIndex) {
        return nativeMaximumInt(nativePtr, columnIndex);
    }

    protected native long nativeMaximumInt(long nativePtr, long columnIndex);

    @Override
    public long minimumLong(long columnIndex) {
        return nativeMinimumInt(nativePtr, columnIndex);
    }

    protected native long nativeMinimumInt(long nativePtr, long columnnIndex);

    @Override
    public double averageLong(long columnIndex) {
        return nativeAverageInt(nativePtr, columnIndex);
    }

    protected native double nativeAverageInt(long nativePtr, long columnIndex);

    // Floats
    @Override
    public double sumFloat(long columnIndex) {
        return nativeSumFloat(nativePtr, columnIndex);
    }

    protected native double nativeSumFloat(long nativePtr, long columnIndex);

    @Override
    public float maximumFloat(long columnIndex) {
        return nativeMaximumFloat(nativePtr, columnIndex);
    }

    protected native float nativeMaximumFloat(long nativePtr, long columnIndex);

    @Override
    public float minimumFloat(long columnIndex) {
        return nativeMinimumFloat(nativePtr, columnIndex);
    }

    protected native float nativeMinimumFloat(long nativePtr, long columnnIndex);

    @Override
    public double averageFloat(long columnIndex) {
        return nativeAverageFloat(nativePtr, columnIndex);
    }

    protected native double nativeAverageFloat(long nativePtr, long columnIndex);

    // Doubles
    @Override
    public double sumDouble(long columnIndex) {
        return nativeSumDouble(nativePtr, columnIndex);
    }

    protected native double nativeSumDouble(long nativePtr, long columnIndex);

    @Override
    public double maximumDouble(long columnIndex) {
        return nativeMaximumDouble(nativePtr, columnIndex);
    }

    protected native double nativeMaximumDouble(long nativePtr, long columnIndex);

    @Override
    public double minimumDouble(long columnIndex) {
        return nativeMinimumDouble(nativePtr, columnIndex);
    }

    protected native double nativeMinimumDouble(long nativePtr, long columnnIndex);

    @Override
    public double averageDouble(long columnIndex) {
        return nativeAverageDouble(nativePtr, columnIndex);
    }

    protected native double nativeAverageDouble(long nativePtr, long columnIndex);

    // Date aggregates

    @Override
    public Date maximumDate(long columnIndex) {
        return new Date(nativeMaximumDate(nativePtr, columnIndex) * 1000);
    }

    protected native long nativeMaximumDate(long nativePtr, long columnIndex);

    @Override
    public Date minimumDate(long columnIndex) {
        return new Date(nativeMinimumDate(nativePtr, columnIndex) * 1000);
    }

    protected native long nativeMinimumDate(long nativePtr, long columnnIndex);


    //
    // Count
    //

    public long count(long columnIndex, long value) {
        return nativeCountLong(nativePtr, columnIndex, value);
    }

    protected native long nativeCountLong(long nativePtr, long columnIndex, long value);


    public long count(long columnIndex, float value) {
        return nativeCountFloat(nativePtr, columnIndex, value);
    }

    protected native long nativeCountFloat(long nativePtr, long columnIndex, float value);

    public long count(long columnIndex, double value) {
        return nativeCountDouble(nativePtr, columnIndex, value);
    }

    protected native long nativeCountDouble(long nativePtr, long columnIndex, double value);

    @Override
    public long count(long columnIndex, String value) {
        return nativeCountString(nativePtr, columnIndex, value);
    }

    protected native long nativeCountString(long nativePtr, long columnIndex, String value);


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

    protected native long nativeWhere(long nativeTablePtr);

    @Override
    public long findFirstLong(long columnIndex, long value) {
        return nativeFindFirstInt(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstInt(long nativeTablePtr, long columnIndex, long value);

    @Override
    public long findFirstBoolean(long columnIndex, boolean value) {
        return nativeFindFirstBool(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstBool(long nativePtr, long columnIndex, boolean value);

    @Override
    public long findFirstFloat(long columnIndex, float value) {
        return nativeFindFirstFloat(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstFloat(long nativePtr, long columnIndex, float value);

    @Override
    public long findFirstDouble(long columnIndex, double value) {
        return nativeFindFirstDouble(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstDouble(long nativePtr, long columnIndex, double value);

    @Override
    public long findFirstDate(long columnIndex, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstDate(nativePtr, columnIndex, date.getTime() / 1000);
    }

    protected native long nativeFindFirstDate(long nativeTablePtr, long columnIndex, long dateTimeValue);

    @Override
    public long findFirstString(long columnIndex, String value) {
        if (value == null) {
            throw new IllegalArgumentException("null is not supported");
        }
        return nativeFindFirstString(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstString(long nativeTablePtr, long columnIndex, String value);

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

    protected native long nativeFindAllInt(long nativePtr, long columnIndex, long value);

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

    protected native long nativeFindAllBool(long nativePtr, long columnIndex, boolean value);

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

    protected native long nativeFindAllFloat(long nativePtr, long columnIndex, float value);

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

    protected native long nativeFindAllDouble(long nativePtr, long columnIndex, double value);

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

    protected native long nativeFindAllDate(long nativePtr, long columnIndex, long dateTimeValue);

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

    protected native long nativeFindAllString(long nativePtr, long columnIndex, String value);

    // Experimental feature
    @Override
    public long lowerBoundLong(long columnIndex, long value) {
        return nativeLowerBoundInt(nativePtr, columnIndex, value);
    }
    @Override
    public long upperBoundLong(long columnIndex, long value) {
        return nativeUpperBoundInt(nativePtr, columnIndex, value);
    }

    protected native long nativeLowerBoundInt(long nativePtr, long columnIndex, long value);
    protected native long nativeUpperBoundInt(long nativePtr, long columnIndex, long value);
    
    
    @Override
    public Table pivot(long stringCol, long intCol, PivotType pivotType){
        if (! this.getColumnType(stringCol).equals(ColumnType.STRING ))
            throw new UnsupportedOperationException("Group by column must be of type String");
        if (! this.getColumnType(intCol).equals(ColumnType.INTEGER ))
            throw new UnsupportedOperationException("Aggregation column must be of type Int");
        Table result = new Table();
        nativePivot(nativePtr, stringCol, intCol, pivotType.value, result.nativePtr);
        return result;
    }

    protected native void nativePivot(long nativeTablePtr, long stringCol, long intCol, int pivotType, long resultPtr);

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

    protected native long nativeGetDistinctView(long nativePtr, long columnIndex);

    /**
     * Return the table name as it is in the associated group.
     *
     * @return Name of the the table or null if it not part of a group.
     */
    public String getName() {
        return nativeGetName(nativePtr);
    }

    protected native String nativeGetName(long nativeTablePtr);

    // Optimize
    public void optimize() {
        checkImmutable();
        nativeOptimize(nativePtr);
    }

    protected native void nativeOptimize(long nativeTablePtr);

    @Override
    public String toJson() {
        return nativeToJson(nativePtr);
    }

    protected native String nativeToJson(long nativeTablePtr);

    @Override
    public String toString() {
        return nativeToString(nativePtr, INFINITE);
    }

    @Override
    public String toString(long maxRows) {
        return nativeToString(nativePtr, maxRows);
    }

    protected native String nativeToString(long nativeTablePtr, long maxRows);

    @Override
    public String rowToString(long rowIndex) {
        return nativeRowToString(nativePtr, rowIndex);
    }

    protected native String nativeRowToString(long nativeTablePtr, long rowIndex);

    @Override
    public long sync() {
        throw new RuntimeException("Not supported for tables");
    }

    private void throwImmutable() {
        throw new IllegalStateException("Changing Realm data can only be done from inside a transaction.");
    }

    /**
     * Compares the schema of the current instance of Table with another instance.
     * @param table The instance to compare with. It cannot be null.
     * @return true if the two instances have the same schema (column names and types)
     */
    public boolean hasSameSchema(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("The argument cannot be null");
        }
        return nativeHasSameSchema(this.nativePtr, table.nativePtr);
    }

    protected native boolean nativeHasSameSchema(long thisTable, long otherTable);
}
