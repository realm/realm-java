package com.tightdb;

public enum ColumnType {
	ColumnTypeInt(0),
	ColumnTypeBool(1),
	ColumnTypeString(2),
	ColumnTypeDate(3),
	ColumnTypeBinary(4),
	ColumnTypeTable(5),
	ColumnTypeMixed(6),
	// Double refs
	ColumnTypeStringEnum(7);
	
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
