package com.tightdb;


public class TableBase {
	public TableBase(){
		// Native methods work will be initialized here. Generated classes will
		// have nothing to do with the native functions. Generated Java Table 
		// classes will work as a wrapper on top of table.
		this.nativePtr = createNative();
	}

	public void registerColumn(ColumnType columnType, String columnName){
		if(getCount() != 0){
			throw new IllegalStateException("Column can be registered in a empty table only");
		}
		nativeRegisterColumn(columnType, columnName);
	}
	
	protected native void nativeRegisterColumn(ColumnType columnType, String columnName);
	
	public String getString(int columnIndex, int rowIndex){
		return nativeGetString(columnIndex, rowIndex);
	}
	
	protected native String nativeGetString(int columnIndex, int rowIndex);
	
	public long getLong(int columnIndex, int rowIndex){
		return nativeGetLong(columnIndex, rowIndex);
	}
	
	protected native long nativeGetLong(int columnIndex, int rowIndex);
	
	public boolean getBoolean(int columnIndex, int rowIndex){
		return nativeGetBoolean(columnIndex, rowIndex);
	}
	
	protected native boolean nativeGetBoolean(int columnIndex, int rowIndex);
	
	public byte[] getBinaryData(int columnIndex, int rowIndex){
		return nativeGetBinaryData(columnIndex, rowIndex);
	}
	
	protected native byte[] nativeGetBinaryData(int columnIndex, int rowIndex);
	
	// Schema based apis
	public int getColumnCount(){
		return nativeGetColumnCount();
	}
	
	protected native int nativeGetColumnCount();
	
	public String getColumnName(int columnIndex){
		return nativeGetColumnName(columnIndex);
	}

	protected native String nativeGetColumnName(int columnIndex);

	public ColumnType getColumnType(int columnIndex){
		int columnType = nativeGetColumnType(columnIndex);
		ColumnType[] columnTypes = ColumnType.values();
		return columnTypes[columnType];
	}
	
	protected native int nativeGetColumnType(int columnIndex);
	
	public int getColumnIndex(String name){
		int columnCount = getColumnCount();
		for(int i=0; i<columnCount; i++){
			if(name.equals(getColumnName(i))){
				return i;
			}
		}
		return -1;
	}
	
	// Size based api
	public int getCount(){
		return nativeGetCount();
	}
	
	protected native int nativeGetCount();
	
	public boolean isEmpty(){
		return getCount() == 0;
	}
	
	public void setString(int columnIndex, int rowIndex, String value){
		nativeSetString(columnIndex, rowIndex, value);
	}
	
	protected native void nativeSetString(int columnIndex, int rowIndex, String value);
	
	public void setLong(int columnIndex, int rowIndex, long value){
		nativeSetLong(columnIndex, rowIndex, value);
	}
	
	protected native void nativeSetLong(int columnIndex, int rowIndex, long value);
	
	public void setBoolean(int columnIndex, int rowIndex, boolean value){
		nativeSetBoolean(columnIndex, rowIndex, value);
	}
	
	protected native void nativeSetBoolean(int columnIndex, int rowIndex, boolean value);
	
	public void setBinaryData(int columnIndex, int rowIndex, byte[] data){
		if(data == null)
			throw new NullPointerException("Null array");
		nativeSetBinaryData(columnIndex, rowIndex, data);
	}
	
	protected native void nativeSetBinaryData(int columnIndex, int rowIndex, byte[] data);
	
	public void insertString(int columnIndex, int rowIndex, String value){
		nativeInsertString(columnIndex, rowIndex, value);
	}
	
	protected native void nativeInsertString(int columnIndex, int rowIndex, String value);
	
	public void insertLong(int columnIndex, int rowIndex, long value){
		nativeInsertLong(columnIndex, rowIndex, value);
	}
	
	protected native void nativeInsertLong(int columnIndex, int rowIndex, long value);
	
	public void insertBoolean(int columnIndex, int rowIndex, boolean value){
		nativeInsertBoolean(columnIndex, rowIndex, value);
	}
	
	protected native void nativeInsertBoolean(int columnIndex, int rowIndex, boolean value);
	
	public void insertBinaryData(int columnIndex, int rowIndex, byte[] data){
		nativeInsertBinaryData(columnIndex, rowIndex, data);
	}
	
	protected native void nativeInsertBinaryData(int columnIndex, int rowIndex, byte[] data);
	
	public void insertDone(){
		nativeInsertDone();
	}
	
	protected native void nativeInsertDone();
	
	/**
	 * Removes a row from the specific index. As of now the entry is simply 
	 * removed from the table. No Cascading delete for other table is not 
	 * taken care of.
	 * 
	 */
	public void removeRow(int rowIndex){
		nativeRemoveRow(rowIndex);
	}

	protected native void nativeRemoveRow(int rowIndex);
	
	public void clear(){
		nativeClear();
	}
	
	protected native void nativeClear();

	public TableQuery query(){
		return null;
	}
	
	public void optimize(){
	}
	
	protected TableBase(long nativePtr){
		this.nativePtr = nativePtr;
	}
	
	protected native long createNative();
	
	//TODO Test method, to be removed.
	public native static void executeNative();
	
	protected long nativePtr;
}
