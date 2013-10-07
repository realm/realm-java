package com.tightdb;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.TableView.Order;
import com.tightdb.internal.CloseMutex;
import com.tightdb.typed.TightDB;

/*
 Add isEqual(Table)

 */

/**
 * This class is a base class for all TightDB tables. The class supports all low
 * level methods (define/insert/delete/update) a table has. All the native
 * communications to the TightDB C++ library are also handled by this class.
 *
 * A user who wants to create a table of his choice will automatically inherit
 * from this class by the tightdb-class generator.
 *
 * As an example, let's create a table to keep records of an employee in a
 * company.
 *
 * For this purpose we will create a class named "employee" with an Entity
 * annotation as follows.
 *
 *      @DefineTable
 *      public class employee {
 *          String name;
 *          long age;
 *          boolean hired;
 *          byte[] imageData;
 *      }
 *
 * The tightdb class generator will generate classes relevant to the employee:
 *
 * 1. Employee.java:  Represents one employee of the employee table i.e., a single row. Getter/setter
 *                    methods are declared from which you will be able to set/get values
 *                    for a particular employee.
 * 2. EmployeeTable.java:  Represents the class for storing a collection of employee i.e., a table
 *                    of rows. The class is inherited from the TableBase class as described above.
 *                    It has all the high level methods to manipulate Employee objects from the table.
 * 3. EmployeeView.java: Represents view of the employee table i.e., result set of queries.
 *
 *
 */

public class Table implements TableOrView, TableSchema {

    public static final long INFINITE = -1;

    protected long nativePtr;
    protected boolean immutable = false;

    // test:
    protected int tableNo;
    protected boolean DEBUG = false;
    protected static int TableCount = 0;

    static {
        TightDB.loadLibrary();
    }

    /**
     * Construct a Table base object. It can be used to register columns in this
     * table. Registering into table is allowed only for empty tables. It
     * creates a native reference of the object and keeps a reference to it.
     */
    public Table() {
        // Native methods work will be initialized here. Generated classes will
        // have nothing to do with the native functions. Generated Java Table
        // classes will work as a wrapper on top of table.
        nativePtr = createNative();
        if (nativePtr == 0)
            throw new OutOfMemoryError("Out of native memory.");
        if (DEBUG) {
            tableNo = ++TableCount;
            System.err.println("====== New Tablebase " + tableNo + " : ptr = " + nativePtr);
        }
    }

    protected native long createNative();

    protected Table(Object parent, long nativePtr, boolean immutable) {
        this.immutable = immutable;
        this.nativePtr = nativePtr;
        if (DEBUG) {
            tableNo = ++TableCount;
            System.err.println("===== New Tablebase(ptr) " + tableNo + " : ptr = " + nativePtr);
        }
    }

    @Override
    public void finalize() throws Throwable {
        if (DEBUG) System.err.println("==== FINALIZE " + tableNo + "...");
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private void close() {
        synchronized (CloseMutex.getInstance()) {
            if (nativePtr == 0) {
                if (DEBUG)
                    System.err.println(".... CLOSE ignored.");
                return;
            }
            if (DEBUG) {
                TableCount--;
                System.err.println("==== CLOSE " + tableNo + " ptr= " + nativePtr + " remaining " + TableCount);
            }
            nativeClose(nativePtr);
            nativePtr = 0;
        }
    }

    protected native void nativeClose(long nativeTablePtr);

    /*
     * Check if the Table is valid.
     * Whenever a Table/subtable is changed/updated all it's subtables are invalidated.
     * You can no longer perform any actions on the table, and if done anyway, an exception is thrown.
     * The only method you can call is 'isValid()'.
     */

    public boolean isValid(){
    	if (nativePtr == 0)
    		return false;
        return nativeIsValid(nativePtr);
    }

    protected native boolean nativeIsValid(long nativeTablePtr);

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        // Has to work for all the typed tables as well
        if (!(other instanceof Table))
            return false;

        Table otherTable = (Table) other;
        return nativeEquals(nativePtr, otherTable.nativePtr);
    }

    protected native boolean nativeEquals(long nativeTablePtr, long nativeTableToComparePtr);

    private void verifyColumnName(String name) {
    	if (name.length() > 63) {
    		throw new IllegalArgumentException("Column names are currently limited to max 63 characters.");
    	}    	
    }

    @Override
    public TableSchema getSubTableSchema(long columnIndex) {
        if(nativeIsRootTable(nativePtr) == false)
            throw new UnsupportedOperationException("This is a subtable. Can only be called on root table.");

        long[] newPath = new long[1];
        newPath[0] = columnIndex;
        return new SubTableSchema(nativePtr, newPath);
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
     * Supported types - refer to @see ColumnType.
     *
     * @param columnType
     *            data type of the column @see <code>ColumnType</code>
     * @param columnName
     *            name of the column. Duplicate column name is not allowed.
     */
    public void updateFromSpec(TableSpec tableSpec) {
        if (immutable) throwImmutable();
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
        if (immutable) throwImmutable();
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
     * @param name column name
     * @return the index, -1 if not found
     */
    @Override
    public long getColumnIndex(String name) {
        long columnCount = getColumnCount();
        for (long i = 0; i < columnCount; i++) {
            if (name.equals(getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }

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
        if (immutable) throwImmutable();
        nativeRemove(nativePtr, rowIndex);
    }

    protected native void nativeRemove(long nativeTablePtr, long rowIndex);

    @Override
    public void removeLast() {
        if (immutable) throwImmutable();
        nativeRemoveLast(nativePtr);
    }

    protected native void nativeRemoveLast(long nativeTablePtr);

    /**
     *  EXPERIMENTAL function
     */
    public void moveLastOver(long rowIndex) {
        if (immutable) throwImmutable();
        nativeMoveLastOver(nativePtr, rowIndex);
    }

    protected native void nativeMoveLastOver(long nativeTablePtr, long rowIndex);


    // Row Handling methods.
    public long addEmptyRow() {
        if (immutable) throwImmutable();
        return nativeAddEmptyRow(nativePtr, 1);
    }

    public long addEmptyRows(long rows) {
        if (immutable) throwImmutable();
        if (rows < 1)
        	throw new IllegalArgumentException("'rows' must be > 0.");
        return nativeAddEmptyRow(nativePtr, rows);
    }

    protected native long nativeAddEmptyRow(long nativeTablePtr, long rows);

    
    /**
     * Appends the specified row to the end of the table
     * @param values
     * @return The row index of the appended row
     */
    public long add(Object... values) {
        long rowIndex = size();
    	addAt(rowIndex, values);
    	return rowIndex;
    }


    /**
     * Inserts a row at the specified row index. Shifts the row currently at that row index and any subsequent rows down (adds one to their row index).
     * @param rowIndex
     * @param values
     */
    public void addAt(long rowIndex, Object... values) {
        if (immutable) throwImmutable();

        // Check index
        long size = size();
        if (rowIndex > size) {
            throw new IllegalArgumentException("rowIndex " + String.valueOf(rowIndex) +
                    " must be <= table.size() " + String.valueOf(size) + ".");
        }

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
                nativeInsertBoolean(nativePtr, columnIndex, rowIndex, (Boolean)value);
                break;
            case INTEGER:
                nativeInsertLong(nativePtr, columnIndex, rowIndex, ((Number)value).longValue());
                break;
            case FLOAT:
                nativeInsertFloat(nativePtr, columnIndex, rowIndex, ((Float)value).floatValue());
                break;
            case DOUBLE:
                nativeInsertDouble(nativePtr, columnIndex, rowIndex, ((Double)value).doubleValue());
                break;
            case STRING:
                nativeInsertString(nativePtr, columnIndex, rowIndex, (String)value);
                break;
            case DATE:
                nativeInsertDate(nativePtr, columnIndex, rowIndex, ((Date)value).getTime()/1000);
                break;
            case MIXED:
                nativeInsertMixed(nativePtr, columnIndex, rowIndex, Mixed.mixedValue(value));
                break;
            case BINARY:
                nativeInsertByteArray(nativePtr, columnIndex, rowIndex, (byte[])value);
                break;
            case TABLE:
                nativeInsertSubTable(nativePtr, columnIndex, rowIndex);
                insertSubtableValues(rowIndex, columnIndex, value);
                break;
            default:
                throw new RuntimeException("Unexpected columnType: " + String.valueOf(colTypes[(int)columnIndex]));
            }
        }
        //Insert done. Use native, no need to check for immutable again here
        nativeInsertDone(nativePtr); 

    }

    private void insertSubtableValues(long rowIndex, long columnIndex, Object value) {
        if (value != null) {
            // insert rows in subtable recursively
            Table subtable = getSubTableDuringInsert(columnIndex, rowIndex);
            int rows = ((Object[])value).length;
            for (int i=0; i<rows; ++i) {
                Object rowArr = ((Object[])value)[i];
                subtable.addAt(i, (Object[])rowArr);
            }
        }
    }
    
    /**
     * Returns a view sorted by the specified column by the default order
     * @param columnIndex
     * @return
     */
    public TableView getSortedView(long columnIndex){
        TableView view = this.where().findAll();
        view.sort(columnIndex);
        return view;
    }
    
    /**
     * Returns a view sorted by the specified column and order
     * @param columnIndex
     * @param order
     * @return
     */
    public TableView getSortedView(long columnIndex, Order order){
        TableView view = this.where().findAll();
        view.sort(columnIndex, order);
        return view;
    }

    /**
     * Replaces the row at the specified position with the specified row.
     * @param rowIndex
     * @param values
     */
    public void set(long rowIndex, Object... values) {
        if (immutable) throwImmutable();

        // Check index
        long size = size();
        if (rowIndex >= size) {
            throw new IllegalArgumentException("rowIndex " + String.valueOf(rowIndex) +
                    " must be < table.size() " + String.valueOf(size) + ".");
        }

        // Verify number of 'values'
        int columns = (int)getColumnCount();
        if (columns != values.length) {
            throw new IllegalArgumentException("The number of value parameters (" +
                    String.valueOf(values.length) +
                    ") does not match the number of columns in the table (" +
                    String.valueOf(columns) + ").");
        }
        // Verify type of 'values'
        ColumnType colTypes[] = new ColumnType[columns];
        for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
            Object value = values[columnIndex];
            ColumnType colType = getColumnType(columnIndex);
            colTypes[columnIndex] = colType;
            if (!colType.matchObject(value)) {
                throw new IllegalArgumentException("Invalid argument no " + String.valueOf(1 + columnIndex) +
                        ". Expected a value compatible with column type " + colType + ", but got " + value.getClass() + ".");
            }
        }

        // Now that all values are verified, we can remove the row and insert it again.
        // TODO: Can be optimized to only set the values (but clear any subtables)
        remove(rowIndex);
        addAt(rowIndex, values);
    }
    
    //Instance of the inner class InternalMethods.
    private InternalMethods internal = new InternalMethods();
    
    //Returns InternalMethods instance with public internal methods. Should only be called by AbstractTable
    public InternalMethods getInternalMethods(){
        return this.internal;
    }
    
    
    //Holds methods that must be publicly available for AbstractClass.
    //Should not be called when using the dynamic interface. The methods can be accessed by calling getInternalMethods() in Table class
    public class InternalMethods{
        
        public void insertLong(long columnIndex, long rowIndex, long value) {
            if (immutable) throwImmutable();
            nativeInsertLong(nativePtr, columnIndex, rowIndex, value);
        }
        
        public void insertDouble(long columnIndex, long rowIndex, double value) {
            if (immutable) throwImmutable();
            nativeInsertDouble(nativePtr, columnIndex, rowIndex, value);
        }
        
        public void insertFloat(long columnIndex, long rowIndex, float value) {
            if (immutable) throwImmutable();
            nativeInsertFloat(nativePtr, columnIndex, rowIndex, value);
        }
        
        public void insertBoolean(long columnIndex, long rowIndex, boolean value) { 
            if (immutable) throwImmutable();
            nativeInsertBoolean(nativePtr, columnIndex, rowIndex, value);
        }
        
        public void insertDate(long columnIndex, long rowIndex, Date date) {
            if (immutable) throwImmutable();
            nativeInsertDate(nativePtr, columnIndex, rowIndex, date.getTime()/1000);
        }
        
        public void insertString(long columnIndex, long rowIndex, String value) {
            if (immutable) throwImmutable();
            nativeInsertString(nativePtr, columnIndex, rowIndex, value);
        }
        
        public void insertMixed(long columnIndex, long rowIndex, Mixed data) {
            if (immutable) throwImmutable();
            nativeInsertMixed(nativePtr, columnIndex, rowIndex, data);
        }

        /*

        public void insertBinary(long columnIndex, long rowIndex, ByteBuffer data) {
            if (immutable) throwImmutable();
            //System.err.printf("\ninsertBinary(col %d, row %d, ByteBuffer)\n", columnIndex, rowIndex);
            //System.err.println("-- HasArray: " + (data.hasArray() ? "yes":"no") + " len= " + data.array().length);
            if (data.isDirect())
                nativeInsertByteBuffer(nativePtr, columnIndex, rowIndex, data);
            else
                throw new RuntimeException("Currently ByteBuffer must be allocateDirect().");   // FIXME: support other than allocateDirect
        }

        */
        
        public void insertBinary(long columnIndex, long rowIndex, byte[] data) {
            if (immutable) throwImmutable();
            if(data != null)
                nativeInsertByteArray(nativePtr, columnIndex, rowIndex, data);
            else
                throw new NullPointerException("byte[] must not be null. Alternatively insert empty array.");
        }
        
        public void insertSubTable(long columnIndex, long rowIndex, Object[][] values) {
            if (immutable) throwImmutable();
            nativeInsertSubTable(nativePtr, columnIndex, rowIndex);
            insertSubtableValues(rowIndex, columnIndex, values);
        }
        
        public void insertDone() {
            if (immutable) throwImmutable();
            nativeInsertDone(nativePtr);
        }
    }


    

    protected native void nativeInsertFloat(long nativeTablePtr, long columnIndex, long rowIndex, float value);

    

    protected native void nativeInsertDouble(long nativeTablePtr, long columnIndex, long rowIndex, double value);

    
    protected native void nativeInsertLong(long nativeTablePtr, long columnIndex, long rowIndex, long value);

    

    protected native void nativeInsertBoolean(long nativeTablePtr, long columnIndex, long rowIndex, boolean value);

    

    protected native void nativeInsertDate(long nativePtr, long columnIndex, long rowIndex, long dateTimeValue);

   
    protected native void nativeInsertString(long nativeTablePtr, long columnIndex, long rowIndex, String value);

   

    protected native void nativeInsertMixed(long nativeTablePtr, long columnIndex, long rowIndex, Mixed mixed);


   /* public void insertBinary(long columnIndex, long rowIndex, byte[] data) {
        if (data == null)
            throw new NullPointerException("Null Array");
        if (immutable) throwImmutable();
        nativeInsertByteArray(nativePtr, columnIndex, rowIndex, data);
    }*/

    
    protected native void nativeInsertByteArray(long nativePtr, long columnIndex, long rowIndex, byte[] data);

   

    protected native void nativeInsertSubTable(long nativeTablePtr, long columnIndex, long rowIndex);

   

    protected native void nativeInsertDone(long nativeTablePtr);

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
    public Table getSubTable(long columnIndex, long rowIndex) {
        return new Table(this, nativeGetSubTable(nativePtr, columnIndex, rowIndex), immutable);
    }

    protected native long nativeGetSubTable(long nativeTablePtr, long columnIndex, long rowIndex);

    // Below version will allow to getSubTable when number of available rows are not updated yet -
    // which happens before an insertDone().
    
    private Table getSubTableDuringInsert(long columnIndex, long rowIndex) {
        return new Table(this, nativeGetSubTableDuringInsert(nativePtr, columnIndex, rowIndex), immutable);
    }
    private native long nativeGetSubTableDuringInsert(long nativeTablePtr, long columnIndex, long rowIndex);


    public long getSubTableSize(long columnIndex, long rowIndex) {
        return nativeGetSubTableSize(nativePtr, columnIndex, rowIndex);
    }

    protected native long nativeGetSubTableSize(long nativeTablePtr, long columnIndex, long rowIndex);

    public void clearSubTable(long columnIndex, long rowIndex) {
        if (immutable) throwImmutable();
        nativeClearSubTable(nativePtr, columnIndex, rowIndex);
    }

    protected native void nativeClearSubTable(long nativeTablePtr, long columnIndex, long rowIndex);


    //
    // Setters
    //

    @Override
    public void setLong(long columnIndex, long rowIndex, long value) {
        if (immutable) throwImmutable();
        nativeSetLong(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetLong(long nativeTablePtr, long columnIndex, long rowIndex, long value);

    @Override
    public void setBoolean(long columnIndex, long rowIndex, boolean value) {
        if (immutable) throwImmutable();
        nativeSetBoolean(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetBoolean(long nativeTablePtr, long columnIndex, long rowIndex, boolean value);

    @Override
    public void setFloat(long columnIndex, long rowIndex, float value) {
        if (immutable) throwImmutable();
        nativeSetFloat(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetFloat(long nativeTablePtr, long columnIndex, long rowIndex, float value);

    @Override
    public void setDouble(long columnIndex, long rowIndex, double value) {
        if (immutable) throwImmutable();
        nativeSetDouble(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetDouble(long nativeTablePtr, long columnIndex, long rowIndex, double value);

    @Override
    public void setDate(long columnIndex, long rowIndex, Date date) {
        if (immutable) throwImmutable();
        nativeSetDate(nativePtr, columnIndex, rowIndex, date.getTime() / 1000);
    }

    protected native void nativeSetDate(long nativeTablePtr, long columnIndex, long rowIndex, long dateTimeValue);

    @Override
    public void setString(long columnIndex, long rowIndex, String value) {
        if (immutable) throwImmutable();
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
            throw new NullPointerException("Null array");
        if (data.isDirect())
            nativeSetByteBuffer(nativePtr, columnIndex, rowIndex, data);
        else
            throw new RuntimeException("Currently ByteBuffer must be allocateDirect()."); // FIXME: support other than allocateDirect
    }

    protected native void nativeSetByteBuffer(long nativeTablePtr, long columnIndex, long rowIndex, ByteBuffer data);
    */


    @Override
    public void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data) {
        if (immutable) throwImmutable();
        if (data == null)
            throw new NullPointerException("Null Array");
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
        if (immutable) throwImmutable();
        if (data == null)
            throw new NullPointerException();
        nativeSetMixed(nativePtr, columnIndex, rowIndex, data);
    }

    protected native void nativeSetMixed(long nativeTablePtr, long columnIndex, long rowIndex, Mixed data);

    /**
     * Add the value for to all cells in the column.
     *
     * @param columnIndex column index of the cell
     * @param value
     */
    //!!!TODO: New. Support in highlevel API
    @Override
    public void adjust(long columnIndex, long value) {
        if (immutable) throwImmutable();
        nativeAddInt(nativePtr, columnIndex, value);
    }

    protected native void nativeAddInt(long nativeViewPtr, long columnIndex, long value);

    
    public void setIndex(long columnIndex) {
        if (immutable) throwImmutable();
        if (getColumnType(columnIndex) != ColumnType.STRING)
            throw new IllegalArgumentException("Index is only supported on string columns.");
        nativeSetIndex(nativePtr, columnIndex);
    }

    protected native void nativeSetIndex(long nativePtr, long columnIndex);

    
    public boolean hasIndex(long columnIndex) {
        return nativeHasIndex(nativePtr, columnIndex);
    }

    protected native boolean nativeHasIndex(long nativePtr, long columnIndex);

    //
    // Aggregate functions
    //

    // Integers
    @Override
    public long sumInt(long columnIndex) {
        return nativeSumInt(nativePtr, columnIndex);
    }

    protected native long nativeSumInt(long nativePtr, long columnIndex);

    @Override
    public long maximumInt(long columnIndex) {
        return nativeMaximumInt(nativePtr, columnIndex);
    }

    protected native long nativeMaximumInt(long nativePtr, long columnIndex);

    @Override
    public long minimumInt(long columnIndex) {
        return nativeMinimumInt(nativePtr, columnIndex);
    }

    protected native long nativeMinimumInt(long nativePtr, long columnnIndex);

    @Override
    public double averageInt(long columnIndex) {
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
        return new TableQuery(nativeWhere(nativePtr), immutable);
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
        return nativeFindFirstDate(nativePtr, columnIndex, date.getTime() / 1000);
    }

    protected native long nativeFindFirstDate(long nativeTablePtr, long columnIndex, long dateTimeValue);

    @Override
    public long findFirstString(long columnIndex, String value) {
        return nativeFindFirstString(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstString(long nativeTablePtr, long columnIndex, String value);

    @Override
    public TableView findAllLong(long columnIndex, long value) {
        return new TableView(nativeFindAllInt(nativePtr, columnIndex, value), immutable);
    }

    protected native long nativeFindAllInt(long nativePtr, long columnIndex, long value);

    @Override
    public TableView findAllBoolean(long columnIndex, boolean value) {
        return new TableView(nativeFindAllBool(nativePtr, columnIndex, value), immutable);
    }

    protected native long nativeFindAllBool(long nativePtr, long columnIndex, boolean value);

    @Override
    public TableView findAllFloat(long columnIndex, float value) {
        return new TableView(nativeFindAllFloat(nativePtr, columnIndex, value), immutable);
    }

    protected native long nativeFindAllFloat(long nativePtr, long columnIndex, float value);

    @Override
    public TableView findAllDouble(long columnIndex, double value) {
        return new TableView(nativeFindAllDouble(nativePtr, columnIndex, value), immutable);
    }

    protected native long nativeFindAllDouble(long nativePtr, long columnIndex, double value);

    @Override
    public TableView findAllDate(long columnIndex, Date date) {
        return new TableView(nativeFindAllDate(nativePtr, columnIndex, date.getTime() / 1000), immutable);
    }

    protected native long nativeFindAllDate(long nativePtr, long columnIndex, long dateTimeValue);

    @Override
    public TableView findAllString(long columnIndex, String value) {
        return new TableView(nativeFindAllString(nativePtr, columnIndex, value), immutable);
    }

    protected native long nativeFindAllString(long nativePtr, long columnIndex, String value);

    /*  // Requires that the first column is a string column with unique values. Also index required?

    @Override
    public long lookup(String value) {
        if (this.getColumnType(0) != ColumnType.STRING)
            throw new RuntimeException("lookup() requires a String column.");
        return nativeLookup(nativePtr, value);
    }

    protected native long nativeLookup(long nativeTablePtr, String value); */


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

    //

    public TableView getDistinctView(long columnIndex) {
        return new TableView(nativeGetDistinctView(nativePtr, columnIndex), immutable);
    }

    protected native long nativeGetDistinctView(long nativePtr, long columnIndex);

    // Optimize
    public void optimize() {
        if (immutable) throwImmutable();
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

    private void throwImmutable() {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }
}
