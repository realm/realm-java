package com.tightdb;

public enum ColumnType {
	ColumnTypeInt(0),
	ColumnTypeBool(1),
	ColumnTypeString(2),
	ColumnTypeDate(3),
	ColumnTypeBinary(4),
	ColumnTypeTable(5),
	ColumnTypeMixed(6),
	// Internal types
	ColumnTypeStringEnum(7);	// This is NOT a user selectable datatype - You can not create a table containing this type
	
	private ColumnType(int index){
		this.index = index;
	}
	private int index;
	
	public static ColumnType getColumnTypeForIndex(int index){
		ColumnType[] columnTypes = values();
		for(int i=0; i<columnTypes.length; i++){
			if(columnTypes[i].index == index)
				return columnTypes[i];
		}
		return null;
	}
}
