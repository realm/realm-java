package com.tightdb;

import java.util.Date;
import java.util.List;

import com.tightdb.lib.IRowsetBase;

/**
 * This class a base class for any table structure. This class supports 
 * all the low level methods of define/insert/delete/update a table have.
 * Along with this, all the native communications are also taken care by 
 * this class (Note: tightdb-java is a java support of C++ based tightdb 
 * implementation.)
 * 
 * Any user who wants to create a table of his choice will be automatically
 * inherited from this class by tightdb-class generator.
 * 
 * For an example lets have a table which will take care of an employee of a 
 * company.
 * 
 * For this purpose we will create a class named Employee_Spec with an Entity
 * annotation as follows.
 * 
 * 		@Entity
 *		public class Employee_Spec {
 *			String name;
 *			long age;
 *			boolean hired;
 *			byte[] imageData;
 *		}
 * Now our tightdb class generator will generate some important class relevant 
 * to the employee
 * 
 * 1. Employee.java : Represents one employee of the employee table. Getter/setter 
 *                    methods are there from which user will be able to set/get values
 *                    for a particular employee.
 * 2. EmployeeTable.java : Represents the class for storing a collection of employee. This class
 *                    is inherited from the TableBase class as stated above. Have all higher 
 *                    level methods to manipulate Employee objects from the table.
 * 3. EmployeeView.java: Represent one view of the employee table.
 * 					
 * @author Anirban Talukdar
 *
 */

public class TableBase implements IRowsetBase {
	/**
	 * Contruct a Table base object. Which can be used to register columns in this 
	 * table. Registering into table is allowed only in empty table. Creates a native 
	 * reference of the object and keeps a reference to it.
	 */
	public TableBase(){
		// Native methods work will be initialized here. Generated classes will
		// have nothing to do with the native functions. Generated Java Table 
		// classes will work as a wrapper on top of table.
		this.nativePtr = createNative();
		//this.parentTable = null;
		//this.columnIndex = -1;
		//this.rowIndex = -1;
	}

	/*protected TableBase(TableBase parent, int columnIndex, int rowIndex){
		this.parentTable = parent;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}*/

	/*protected TableBase(TableViewBase parent, int columnIndex, int rowIndex){
		//TODO for the support of a table from tableview this.parentView = parent;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}*/
	/**
	 * Updates a table specification from a Table specification structure.
	 * types that are supported refer to @see ColumnType. 
	 * 
	 * @param columnType data type of the column @see <code>ColumnType</code>
	 * @param columnName name of the column. Duplicate column name is not allowed.
	 */
	public void updateFromSpec(TableSpec tableSpec){
		nativeUpdateFromSpec(tableSpec);
	}
	
	protected native void nativeUpdateFromSpec(TableSpec tableSpec);
	/**
	 * Returns the string of a cell identified by rowIndex and columnIndex.
	 * @param columnIndex 0 based index value of the column
	 * @param rowIndex 0 based index of the row.
	 * @return value of the particular cell
	 */
	public String getString(int columnIndex, int rowIndex){
		/*if(parentTable != null){
			List<CellId> columnRowTreeList = getColumnRowPairForParents();
			return nativeGetStringFromRoot(getTopLevelTable(), columnRowTreeList, columnIndex, rowIndex);
		}*/
		return nativeGetString(columnIndex, rowIndex);
	}
	
	protected native String nativeGetString(int columnIndex, int rowIndex);
	protected native String nativeGetStringFromRoot(TableBase tableBase, List<CellId> columnRowTreeList, int columnIndex, int rowIndex);
	
	/**
	 * Get the long value of the particular cell, identified by columnIndex and rowIndex.
	 * 
	 * @param columnIndex 0 based index value of the column.
	 * @param rowIndex 0 based row value of the column.
	 * @return value of the particular cell.
	 */
	public long getLong(int columnIndex, int rowIndex){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			return nativeGetLongFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex);
		}*/
		return nativeGetLong(columnIndex, rowIndex);
	}
	
	protected native long nativeGetLong(int columnIndex, int rowIndex);
	protected native long nativeGetLongFromRoot(TableBase table, List<CellId> columnRowIndexList, int columnIndex, int rowIndex);

	/**
	 * Get the boolean value of the particular cell, indentified the columnIndex and rowIndex.
	 * 
	 * @param columnIndex 0 based index value of the cell column.
	 * @param rowIndex 0 based index of the row.
	 * @return value of the particular cell.
	 */
	public boolean getBoolean(int columnIndex, int rowIndex){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			return nativeGetBooleanFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex);
		}*/
		return nativeGetBoolean(columnIndex, rowIndex);
	}
	
	protected native boolean nativeGetBoolean(int columnIndex, int rowIndex);
	protected native boolean nativeGetBooleanFromRoot(TableBase table, List<CellId> columnRowIndexList, int columnIndex, int rowIndex);
	
	/**
	 * Get the binary byte[] based value of a cell identified by the columnIndex and rowIndex.
	 * 
	 * @param columnIndex 0 based index value of the cell column
	 * @param rowIndex 0 based index value of the cell row
	 * @return value of the particular cell.
	 */
	public byte[] getBinaryData(int columnIndex, int rowIndex){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			return nativeGetBinaryDataFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex);
		}*/
		return nativeGetBinaryData(columnIndex, rowIndex);
	}
	
	protected native byte[] nativeGetBinaryData(int columnIndex, int rowIndex);
	protected native byte[] nativeGetBinaryDataFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex);
	
	public Mixed getMixed(int columnIndex, int rowIndex){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			return nativeGetMixedFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex);
		}*/
		return nativeGetMixed(columnIndex, rowIndex);
	}
	
	protected native Mixed nativeGetMixed(int columnIndex, int rowIndex);
	protected native Mixed nativeGetMixedFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex);
	
  	public SubTableBase getSubTable(int columnIndex, int rowIndex){
		return new SubTableBase(nativeGetSubTable(columnIndex, rowIndex));
	}
	
	protected native long nativeGetSubTable(int columnIndex, int rowIndex);

	/**
	 * use this method to get the number of columns of the table.
	 * @return number of column.
	 */
	public int getColumnCount(){
		/*if(parentTable != null){
			List<CellId> columnRowTree = getColumnRowPairForParents();
			return nativeGetColumnCountFromRoot(getTopLevelTable(), columnRowTree);
		}*/
		return nativeGetColumnCount();
	}
	
	protected native int nativeGetColumnCount();
	protected native int nativeGetColumnCountFromRoot(TableBase rootTable, List<CellId> columnRowTree);
	
	/**
	 * Returns the name of a column identified by columnIndex, which is 0 based.
	 * @param columnIndex
	 * @return
	 */
	public String getColumnName(int columnIndex){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			return nativeGetColumnNameFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex);
		}*/
		return nativeGetColumnName(columnIndex);
	}

	protected native String nativeGetColumnName(int columnIndex);
	protected native String nativeGetColumnNameFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex);
	
	/**
	 * Get the type of a column identified by the columnIdex. 
	 * @param columnIndex 0 based index value of the column.
	 * @return Type of the particular column.
	 */
	public ColumnType getColumnType(int columnIndex){
		int columnType;
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			columnType = nativeGetColumnTypeFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex);
		}else{
			columnType = nativeGetColumnType(columnIndex);
		}*/
		columnType = nativeGetColumnType(columnIndex);
		ColumnType[] columnTypes = ColumnType.values();
		return columnTypes[columnType];
	}
	
	protected native int nativeGetColumnType(int columnIndex);
	protected native int nativeGetColumnTypeFromRoot(TableBase tableBase, List<CellId> columnRowIndexList, int columnIndex);
	
	/**
	 * Returns the index of a column based on its name index.
	 * It first searches against the name. if found returns the index found 
	 * otherwise returns -1.
	 * 
	 * @param name
	 * @return
	 */
	public int getColumnIndex(String name){
		int columnCount = getColumnCount();
		for(int i=0; i<columnCount; i++){
			if(name.equals(getColumnName(i))){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the number of entires of this table. 
	 * @return
	 */
	public int getCount(){
		/*if(parentTable != null){
			List<CellId> columnRowTree = getColumnRowPairForParents();
			return nativeGetCountFromRoot(getTopLevelTable(), columnRowTree);
		}*/
		return nativeGetCount();
	}
	
	protected native int nativeGetCountFromRoot(TableBase rootTable, List<CellId> columnRowTree);
	protected native int nativeGetCount();
	
	/**
	 * checks whether this table is empty or not.
	 * @return
	 */
	public boolean isEmpty(){
		return getCount() == 0;
	}
	
	/**
	 * Sets a string value for a cell identified by columnIndex and rowIndex. 
	 * Note that if we call this method on the table for a particular column 
	 * marked by the columnIndex, that column has to be an String based column
	 * which means the type of the column must be ColumnType.ColumnTypeString.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 */
	public void setString(int columnIndex, int rowIndex, String value){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeSetStringFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, value);
			return;
		}*/
		nativeSetString(columnIndex, rowIndex, value);
	}
	
	protected native void nativeSetString(int columnIndex, int rowIndex, String value);
	protected native void nativeSetStringFromRoot(TableBase tableBase, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, String value);
	
	/**
	 * Sets the long value for a particular cell identified by rowIndex and columnIndex.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 */
	public void setLong(int columnIndex, int rowIndex, long value){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeSetLongFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, value);
			return;
		}*/
		nativeSetLong(columnIndex, rowIndex, value);
	}
	
	protected native void nativeSetLong(int columnIndex, int rowIndex, long value);
	protected native void nativeSetLongFromRoot(TableBase topLevelTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, long value);

	/**
	 * Sets boolean value for a particular cell marked by the rowIndex and columnIndex.
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 */
	public void setBoolean(int columnIndex, int rowIndex, boolean value){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeSetBooleanFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, value);
			return;
		}*/
		nativeSetBoolean(columnIndex, rowIndex, value);
	}
	
	protected native void nativeSetBoolean(int columnIndex, int rowIndex, boolean value);
	protected native void nativeSetBooleanFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, boolean value);
	
	/**
	 * Sets the binary value for a cell marked by the rowIndex and columnIndex.
	 * @param columnIndex
	 * @param rowIndex
	 * @param data
	 */
	public void setBinaryData(int columnIndex, int rowIndex, byte[] data){
		if(data == null)
			throw new NullPointerException("Null array");
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeSetBinaryDataFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, data);
			return;
		}*/
		nativeSetBinaryData(columnIndex, rowIndex, data);
	}
	
	protected native void nativeSetBinaryData(int columnIndex, int rowIndex, byte[] data);
	protected native void nativeSetBinaryDataFromRoot(TableBase tableBase, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, byte[] data);
	
	
	public void setMixed(int columnIndex, int rowIndex, Mixed data) {
		if (data == null)
			throw new NullPointerException();
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeSetMixedFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, data);
			return;
		}*/
		nativeSetMixed(columnIndex, rowIndex, data);
	}
	
	protected native void nativeSetMixed(int columnIndex, int rowIndex, Mixed data);
	protected native void nativeSetMixedFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, Mixed data);
	
	/**
	 * Inserts a string on a cell identified by rowindex and columnindex. 
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 */
	public void insertString(int columnIndex, int rowIndex, String value){
		/*if(parentTable != null){
			List<CellId> columnRowTree = getColumnRowPairForParents();
			nativeInsertStringFromRoot(getTopLevelTable(), columnRowTree, columnIndex, rowIndex, value);
			return;
		}*/
		nativeInsertString(columnIndex, rowIndex, value);
	}
	
	protected native void nativeInsertString(int columnIndex, int rowIndex, String value);
	protected native void nativeInsertStringFromRoot(TableBase rootTable, List<CellId> columnRowCellTree, int columnIndex, int rowIndex, String value);
	
	/**
	 * Inserts long value on the specific cell identified by columnIndex and rowIndex.
	 * Note that after the insertion old value will vanish whose place is taken by the new value.
	 * 
	 * @param columnIndex 0 based column index of the cell
	 * @param rowIndex 0 based row index of the cell.
	 * @param value new value for the cell to be inserted.
	 */
	public void insertLong(int columnIndex, int rowIndex, long value){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeInsertLongFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, value);
			return;
		}*/
		nativeInsertLong(columnIndex, rowIndex, value);
	}
	
	protected native void nativeInsertLong(int columnIndex, int rowIndex, long value);
	protected native void nativeInsertLongFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, long value);

	/**
	 * Inserts a boolean value into the cell identified by the columnIndex and rowIndex
	 * Note that after the insertion old value of that cell will vanish whose place is
	 * taken by new value.
	 * 
	 * @param columnIndex 0 based columnIndex of the cell
	 * @param rowIndex 0 based rowIndex of the cell
	 * @param value value to be inserted.
	 */
	public void insertBoolean(int columnIndex, int rowIndex, boolean value){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeInsertBooleanFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, value);
			return;
		}*/
		nativeInsertBoolean(columnIndex, rowIndex, value);
	}
	
	protected native void nativeInsertBoolean(int columnIndex, int rowIndex, boolean value);
	protected native void nativeInsertBooleanFromRoot(TableBase tableBase, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, boolean value);
	
	/**
	 * Inserts a binary byte[] data into the cell identified by the columnIndex and rowIndex.
	 * Note that after the insertion old value of that call will vanish whose place is
	 * taken by new value.
	 * 
	 * @param columnIndex 0 based column index of the cell
	 * @param rowIndex 0 based row index of the cell
	 * @param data data to be inserted.
	 */
	public void insertBinaryData(int columnIndex, int rowIndex, byte[] data){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeInsertBinaryDataFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, data);
			return;
		}*/
		nativeInsertBinaryData(columnIndex, rowIndex, data);
	}
	
	protected native void nativeInsertBinaryData(int columnIndex, int rowIndex, byte[] data);
	protected native void nativeInsertBinaryDataFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, byte[] data);
	
	public void insertTable(int columnIndex, int rowIndex){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeInsertTableFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex);
			return;
		}*/
		nativeInsertTable(columnIndex, rowIndex);	
	}
	
	protected native void nativeInsertTable(int columnIndex, int rowIndex);
	protected native void nativeInsertTableFromRoot(TableBase tableBase, List<CellId> columnRowIndexList, int columnIndex, int rowIndex);
	
	public void insertMixed(int columnIndex, int rowIndex, Mixed data){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeInsertMixedFromRoot(getTopLevelTable(), columnRowIndexList, columnIndex, rowIndex, data);
			return;
		}*/
		nativeInsertMixed(columnIndex, rowIndex, data);
	}
	
	protected native void nativeInsertMixed(int columnIndex, int rowIndex, Mixed mixed);
	protected native void nativeInsertMixedFromRoot(TableBase rootTable, List<CellId> columnRowIndexList, int columnIndex, int rowIndex, Mixed data);
	/**
	 * Once insertions are done "say for a particular row" or before switching to a new row
	 * user must call this method to keep the stability of the system, allowing tightdb a chance 
	 * to perform internal works and make it ready for a new insertion.
	 * 
	 */
	public void insertDone(){
		/*if(parentTable != null){
			List<CellId> columnRowTree = getColumnRowPairForParents();
			nativeInsertDoneFromRoot(getTopLevelTable(), columnRowTree);
			return;
		}*/
		nativeInsertDone();
	}
	
	protected native void nativeInsertDone();
	protected native void nativeInsertDoneFromRoot(TableBase rootTable, List<CellId> columnRowTree);
	
	/**
	 * Removes a row from the specific index. As of now the entry is simply 
	 * removed from the table. No Cascading delete for other table is not 
	 * taken care of.
	 * 
	 */
	public void removeRow(int rowIndex){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeRemoveRowFromRoot(getTopLevelTable(), columnRowIndexList, rowIndex);
			return;
		}*/
		nativeRemoveRow(rowIndex);
	}

	protected native void nativeRemoveRow(int rowIndex);
	protected native void nativeRemoveRowFromRoot(TableBase table, List<CellId> columnRowIndexList, int rowIndex);
	
	/*public TableBase getTopLevelTable(){
		TableBase root = this;
		while(root.parentTable != null){
			root = root.parentTable;
		}
		return root;
	}*/
	
	/**
	 * Clears this table. After this call all the row's of this table is deleted.
	 */
	public void clear(){
		/*if(parentTable != null){
			List<CellId> columnRowIndexList = getColumnRowPairForParents();
			nativeClearFromRoot(getTopLevelTable(), columnRowIndexList);
			return;
		}*/
		nativeClear();
	}
	
	protected native void nativeClear();
	protected native void nativeClearFromRoot(TableBase table, List<CellId> columnRowIndexList);
	/**
	 * Returns a TableQuery Object which can be later used to query the table based on 
	 * some condition.
	 * 
	 * @return
	 */
	public TableQuery query(){
		return null;
	}
	
	public void optimize(){
	}
	
	protected TableBase(long nativePtr){
		this.nativePtr = nativePtr;
	}
	class CellId {
		protected int columnIndex;
		protected int rowIndex;
		public CellId(int columnIndex, int rowIndex){
			this.columnIndex = columnIndex;
			this.rowIndex = rowIndex;
		}
	}
	List<CellId> rowColTreeList = null;

	/*protected List<CellId>getColumnRowPairForParents(){
		if(rowColTreeList != null)
			return rowColTreeList;
		TableBase currentBase = this;
		while(currentBase.parentTable != null){
			if(rowColTreeList == null){
				rowColTreeList = new ArrayList<CellId>();
			}
			rowColTreeList.add(new CellId(currentBase.columnIndex, currentBase.rowIndex));
			currentBase = currentBase.parentTable;
		}
		if(rowColTreeList != null){
			Collections.reverse(rowColTreeList);
		}
		return rowColTreeList;
	}*/

	protected native long createNative();
	
	//TODO Test method, to be removed.
	public native static void executeNative();
	
	protected long nativePtr;

	@Override
	public Date getDate(int columnIndex, int rowIndex) {
		return null;
	}

	@Override
	public Mixed getMixed(int columnIndex, int rowIndex, Mixed value) {
		return null;
	}
	
	//protected TableBase parentTable;
	//protected int columnIndex;
	//protected int rowIndex;
}
