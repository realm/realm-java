package com.tightdb;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.lib.IRowsetBase;

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
 * @Table public class employee { String name; long age; boolean hired; byte[]
 *        imageData; }
 * 
 *        The tightdb class generator will generate classes relevant to the
 *        employee:
 * 
 *        1. Employee.java: Represents one employee of the employee table i.e.,
 *        a single row. Getter/setter methods are declared from which you will
 *        be able to set/get values for a particular employee. 2.
 *        EmployeeTable.java: Represents the class for storing a collection of
 *        employee i.e., a table of rows. The class is inherited from the
 *        TableBase class as described above. It has all the high level methods
 *        to manipulate Employee objects from the table. 3. EmployeeView.java:
 *        Represents view of the employee table i.e., result set of queries.
 * 
 * 
 */

public class TableBase implements IRowsetBase {

	/**
	 * Construct a Table base object. It can be used to register columns in this
	 * table. Registering into table is allowed only for empty tables. It
	 * creates a native reference of the object and keeps a reference to it.
	 */
	public TableBase() {
		// Native methods work will be initialized here. Generated classes will
		// have nothing to do with the native functions. Generated Java Table
		// classes will work as a wrapper on top of table.
		this.nativePtr = createNative();
	}

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
		nativeUpdateFromSpec(nativePtr, tableSpec);
	}

	protected native void nativeUpdateFromSpec(long nativePtr, TableSpec tableSpec);

	// Table Size and deletion. AutoGeneraed subclasses are nothing to do with
	// this
	// class.
	/**
	 * Get the number of entries/rows of this table.
	 * 
	 * @return The number of rows.
	 */
	public long size() {
		return nativeSize(nativePtr);
	}

	protected native long nativeSize(long nativeTablePtr);

	/**
	 * Checks whether this table is empty or not.
	 * 
	 * @return true if empty, otherwise false.
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Clears the table i.e., deleting all rows in the table.
	 */
	public void clear() {
		nativeClear(nativePtr);
	}

	protected native void nativeClear(long nativeTablePtr);

	// Column Information.
	/**
	 * Use this method to get the number of columns of the table.
	 * 
	 * @return the number of column.
	 */
	public long getColumnCount() {
		return nativeGetColumnCount(nativePtr);
	}

	protected native long nativeGetColumnCount(long nativeTablePtr);

	/**
	 * Returns the name of a column identified by columnIndex. Notice that the
	 * index is zero based.
	 * 
	 * @param columnIndex
	 *            the column index
	 * @return the name of the column
	 */
	public String getColumnName(long columnIndex) {
		return nativeGetColumnName(nativePtr, columnIndex);
	}

	protected native String nativeGetColumnName(long nativeTablePtr, long columnIndex);

	/**
	 * Returns the index of a column based on the name.
	 * 
	 * @param name
	 *            column name
	 * @return the index, -1 if not found
	 */
	public long getColumnIndex(String name) {
		long columnCount = getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			if (name.equals(getColumnName(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the type of a column identified by the columnIdex.
	 * 
	 * @param columnIndex
	 *            index of the column.
	 * @return Type of the particular column.
	 */
	public ColumnType getColumnType(long columnIndex) {
		int columnType;
		columnType = nativeGetColumnType(nativePtr, columnIndex);
		ColumnType[] columnTypes = ColumnType.values();
		return columnTypes[columnType];
	}

	protected native int nativeGetColumnType(long nativeTablePtr, long columnIndex);

	// Row Handling methods.
	public long addEmptyRow() {
		return nativeAddEmptyRow(nativePtr, 1);
	}

	public long addEmptyRows(long rows) {
		return nativeAddEmptyRow(nativePtr, rows);
	}

	protected native long nativeAddEmptyRow(long nativeTablePtr, long rows);

	/**
	 * Removes a row from the specific index. As of now the entry is simply
	 * removed from the table. No Cascading delete for other table is not taken
	 * care of. Notice that row index is zero based.
	 * 
	 * @param rowIndex
	 *            the row index
	 * 
	 */
	public void remove(long rowIndex) {
		nativeRemove(nativePtr, rowIndex);
	}

	protected native void nativeRemove(long nativeTablePtr, long rowIndex);

	public void removeLast() {
		nativeRemoveLast(nativePtr);
	}

	protected native void nativeRemoveLast(long nativeTablePtr);

	// Insert Row
	/**
	 * Inserts long value on the specific cell. Note that the insertion will
	 * replace old values.
	 * 
	 * @param columnIndex
	 *            0 based column index of the cell
	 * @param rowIndex
	 *            0 based row index of the cell.
	 * @param value
	 *            new value for the cell to be inserted.
	 */
	public void insertLong(long columnIndex, long rowIndex, long value) {
		nativeInsertLong(nativePtr, columnIndex, rowIndex, value);
	}

	protected native void nativeInsertLong(long nativeTablePtr, long columnIndex, long rowIndex, long value);

	/**
	 * Inserts a boolean value into the cell identified by the columnIndex and
	 * rowIndex Note that the insertion will replace old values.
	 * 
	 * @param columnIndex
	 *            0 based columnIndex of the cell
	 * @param rowIndex
	 *            0 based rowIndex of the cell
	 * @param value
	 *            value to be inserted.
	 */
	public void insertBoolean(long columnIndex, long rowIndex, boolean value) {
		nativeInsertBoolean(nativePtr, columnIndex, rowIndex, value);
	}

	protected native void nativeInsertBoolean(long nativeTablePtr, long columnIndex, long rowIndex, boolean value);

	public void insertDate(long columnIndex, long rowIndex, Date date) {
		nativeInsertDate(nativePtr, columnIndex, rowIndex, date.getTime());
	}

	protected native void nativeInsertDate(long nativePtr, long columnIndex, long rowIndex, long dateTimeValue);

	/**
	 * Inserts a string in a cell. Note that the insertion will replace old
	 * values.
	 * 
	 * @param columnIndex
	 *            0 based columnIndex of the cell
	 * @param rowIndex
	 *            0 based rowIndex of the cell
	 * @param value
	 *            value to be inserted.
	 */
	public void insertString(long columnIndex, long rowIndex, String value) {
		nativeInsertString(nativePtr, columnIndex, rowIndex, value);
	}

	protected native void nativeInsertString(long nativeTablePtr, long columnIndex, long rowIndex, String value);

	public void insertMixed(long columnIndex, long rowIndex, Mixed data) {
		nativeInsertMixed(nativePtr, columnIndex, rowIndex, data);
	}

	protected native void nativeInsertMixed(long nativeTablePtr, long columnIndex, long rowIndex, Mixed mixed);

	/**
	 * Inserts a binary byte[] data into the cell. Note that the insertion will
	 * replace old values.
	 * 
	 * @param columnIndex
	 *            0 based column index of the cell
	 * @param rowIndex
	 *            0 based row index of the cell
	 * @param data
	 *            data to be inserted.
	 */
	public void insertBinaryByteBuffer(long columnIndex, long rowIndex, ByteBuffer data) {
		nativeInsertBinary(nativePtr, columnIndex, rowIndex, data);
	}

	protected native void nativeInsertBinary(long nativeTablePtr, long columnIndex, long rowIndex, ByteBuffer data);

	public void insertBinaryByteArray(long columnIndex, long rowIndex, byte[] data) {
		nativeInsertBinary(nativePtr, columnIndex, rowIndex, data);
	}

	protected native void nativeInsertBinary(long nativePtr, long columnIndex, long rowIndex, byte[] data);

	public void insertSubTable(long columnIndex, long rowIndex) {
		nativeInsertSubTable(nativePtr, columnIndex, rowIndex);
	}

	protected native void nativeInsertSubTable(long nativeTablePtr, long columnIndex, long rowIndex);

	/**
	 * Once insertions are done "say for a particular row" or before switching
	 * to a new row user must call this method to keep the stability of the
	 * system, allowing TightDB to perform internal works and make it ready for
	 * a new insertion. This is similar to a "commit" in transactional systems
	 * (note that TightDB is currently not a transactional system).
	 * 
	 */
	public void insertDone() {
		nativeInsertDone(nativePtr);
	}

	protected native void nativeInsertDone(long nativeTablePtr);

	/**
	 * Get the value of the particular (integer) cell.
	 * 
	 * @param columnIndex
	 *            0 based index value of the column.
	 * @param rowIndex
	 *            0 based row value of the column.
	 * @return value of the particular cell.
	 */
	public long getLong(long columnIndex, long rowIndex) {
		return nativeGetLong(nativePtr, columnIndex, rowIndex);
	}

	protected native long nativeGetLong(long nativeTablePtr, long columnIndex, long rowIndex);

	/**
	 * Get the value of the particular (boolean) cell.
	 * 
	 * @param columnIndex
	 *            0 based index value of the cell column.
	 * @param rowIndex
	 *            0 based index of the row.
	 * @return value of the particular cell.
	 */
	public boolean getBoolean(long columnIndex, long rowIndex) {
		return nativeGetBoolean(nativePtr, columnIndex, rowIndex);
	}

	protected native boolean nativeGetBoolean(long nativeTablePtr, long columnIndex, long rowIndex);

	public Date getDate(long columnIndex, long rowIndex) {
		return new Date(nativeGetDateTime(nativePtr, columnIndex, rowIndex));
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
	public ByteBuffer getBinaryByteBuffer(long columnIndex, long rowIndex) {
		return nativeGetBinary(nativePtr, columnIndex, rowIndex);
	}

	protected native ByteBuffer nativeGetBinary(long nativeTablePtr, long columnIndex, long rowIndex);

	public byte[] getBinaryByteArray(long columnIndex, long rowIndex) {
		return nativeGetByteArray(nativePtr, columnIndex, rowIndex);
	}

	protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex, long rowIndex);

	public Mixed getMixed(long columnIndex, long rowIndex) {
		return nativeGetMixed(nativePtr, columnIndex, rowIndex);
	}

	public ColumnType getMixedType(long columnIndex, long rowIndex) {
		int mixedColumnType = nativeGetMixedType(nativePtr, columnIndex, rowIndex);
		ColumnType[] columnTypes = ColumnType.values();
		if (mixedColumnType < 0 || mixedColumnType >= columnTypes.length) {
			return null;
		}
		return columnTypes[mixedColumnType];
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
	public TableBase getSubTable(long columnIndex, long rowIndex) {
		return new TableBase(nativeGetSubTable(nativePtr, columnIndex, rowIndex));
	}

	protected native long nativeGetSubTable(long nativeTablePtr, long columnIndex, long rowIndex);

	public long getSubTableSize(long columnIndex, long rowIndex) {
		return nativeGetSubTableSize(nativePtr, columnIndex, rowIndex);
	}

	protected native long nativeGetSubTableSize(long nativeTablePtr, long columnIndex, long rowIndex);

	/**
	 * Sets a value for a (string) cell. Note that if we call this method on the
	 * table for a particular column marked by the columnIndex, that column has
	 * to be an String based column which means the type of the column must be
	 * ColumnType.ColumnTypeString.
	 * 
	 * @param columnIndex
	 *            column index of the cell
	 * @param rowIndex
	 *            row index of the cell
	 * @param value
	 */
	public void setString(long columnIndex, long rowIndex, String value) {
		nativeSetString(nativePtr, columnIndex, rowIndex, value);
	}

	protected native void nativeSetString(long nativeTablePtr, long columnIndex, long rowIndex, String value);

	/**
	 * Sets the value for a particular (integer) cell.
	 * 
	 * @param columnIndex
	 *            column index of the cell
	 * @param rowIndex
	 *            row index of the cell
	 * @param value
	 */
	public void setLong(long columnIndex, long rowIndex, long value) {
		nativeSetLong(nativePtr, columnIndex, rowIndex, value);
	}

	protected native void nativeSetLong(long nativeTablePtr, long columnIndex, long rowIndex, long value);

	/**
	 * Sets value for a particular (boolean) cell.
	 * 
	 * @param columnIndex
	 *            column index of the cell
	 * @param rowIndex
	 *            row index of the cell
	 * @param value
	 */
	public void setBoolean(long columnIndex, long rowIndex, boolean value) {
		nativeSetBoolean(nativePtr, columnIndex, rowIndex, value);
	}

	protected native void nativeSetBoolean(long nativeTablePtr, long columnIndex, long rowIndex, boolean value);

	public void setDate(long columnIndex, long rowIndex, Date date) {
		nativeSetDate(nativePtr, columnIndex, rowIndex, date.getTime());
	}

	protected native void nativeSetDate(long nativeTablePtr, long columnIndex, long rowIndex, long dateTimeValue);

	/**
	 * Sets the value for a (binary) cell.
	 * 
	 * @param columnIndex
	 *            column index of the cell
	 * @param rowIndex
	 *            row index of the cell
	 * @param data
	 */
	public void setBinaryByteBuffer(long columnIndex, long rowIndex, ByteBuffer data) {
		if (data == null)
			throw new NullPointerException("Null array");
		nativeSetBinary(nativePtr, columnIndex, rowIndex, data);
	}

	protected native void nativeSetBinary(long nativeTablePtr, long columnIndex, long rowIndex, ByteBuffer data);

	public void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data) {
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
	public void setMixed(long columnIndex, long rowIndex, Mixed data) {
		if (data == null)
			throw new NullPointerException();
		nativeSetMixed(nativePtr, columnIndex, rowIndex, data);
	}

	protected native void nativeSetMixed(long nativeTablePtr, long columnIndex, long rowIndex, Mixed data);

	public void clearSubTable(long columnIndex, long rowIndex) {
		nativeClearSubTable(nativePtr, columnIndex, rowIndex);
	}

	protected native void nativeClearSubTable(long nativeTablePtr, long columnIndex, long rowIndex);

	// Indexing
	public void setIndex(long columnIndex) {
		nativeSetIndex(nativePtr, columnIndex);
	}

	protected native void nativeSetIndex(long nativePtr, long columnIndex);

	public boolean hasIndex(long columnIndex) {
		return nativeHasIndex(nativePtr, columnIndex);
	}

	protected native boolean nativeHasIndex(long nativePtr, long columnIndex);

	// Agregate functions.
	public long sum(long columnIndex) {
		return nativeSum(nativePtr, columnIndex);
	}

	protected native long nativeSum(long nativePtr, long columnIndex);

	public long maximum(long columnIndex) {
		return nativeMaximum(nativePtr, columnIndex);
	}

	protected native long nativeMaximum(long nativePtr, long columnIndex);

	public long minimum(long columnIndex) {
		return nativeMinimum(nativePtr, columnIndex);
	}

	protected native long nativeMinimum(long nativePtr, long columnnIndex);

	public long average(long columnIndex) {
		return nativeAverage(nativePtr, columnIndex);
	}

	protected native long nativeAverage(long nativePtr, long columnIndex);

	// Searching methods.
	public long findFirstLong(long columnIndex, long value) {
		return nativeFindFirstInt(nativePtr, columnIndex, value);
	}

	protected native long nativeFindFirstInt(long nativeTablePtr, long columnIndex, long value);

	public long findFirstBoolean(long columnIndex, boolean value) {
		return nativeFindFirstBoolean(nativePtr, columnIndex, value);
	}

	protected native long nativeFindFirstBoolean(long nativePtr, long columnIndex, boolean value);

	public long findFirstDate(long columnIndex, Date date) {
		return nativeFindFirstDate(nativePtr, columnIndex, date.getTime());
	}

	protected native long nativeFindFirstDate(long nativeTablePtr, long columnIndex, long dateTimeValue);

	public long findFirstString(long columnIndex, String value) {
		return nativeFindFirstString(nativePtr, columnIndex, value);
	}

	protected native long nativeFindFirstString(long nativeTablePtr, long columnIndex, String value);

	public TableViewBase findAllLong(long columnIndex, long value) {
		return new TableViewBase(this, nativeFindAllInt(nativePtr, columnIndex, value));
	}

	protected native long nativeFindAllInt(long nativePtr, long columnIndex, long value);

	public TableViewBase findAllAllBool(long columnIndex, boolean value) {
		return new TableViewBase(this, nativeFindAllBool(nativePtr, columnIndex, value));
	}

	protected native long nativeFindAllBool(long nativePtr, long columnIndex, boolean value);

	public TableViewBase findAllDate(long columnIndex, Date date) {
		return new TableViewBase(this, nativeFindAllDate(nativePtr, columnIndex, date.getTime()));
	}

	protected native long nativeFindAllDate(long nativePtr, long columnIndex, long dateTimeValue);

	public TableViewBase findAllString(long columnIndex, String value) {
		return new TableViewBase(this, nativeFindAllString(nativePtr, columnIndex, value));
	}

	protected native long nativeFindAllString(long nativePtr, long columnIndex, String value);

	// Optimize
	public void optimize() {
		nativeOptimize(nativePtr);
	}

	protected native void nativeOptimize(long nativeTablePtr);

	protected TableBase(long nativePtr) {
		this.nativePtr = nativePtr;
	}

	@Override
	public void finalize() {
		close();
	}

	public void close() {
		if (nativePtr == 0)
			return;
		nativeClose(nativePtr);
		nativePtr = 0;
	}

	protected native void nativeClose(long nativeTablePtr);

	protected native long createNative();

	protected long nativePtr;
}
