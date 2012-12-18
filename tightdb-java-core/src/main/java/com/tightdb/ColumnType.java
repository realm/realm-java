package com.tightdb;

import java.nio.ByteBuffer;

// Make sure numbers match with <tightdb/column_type.hpp>

public enum ColumnType {
	ColumnTypeInt(0),
	ColumnTypeBool(1),
	ColumnTypeString(2),
        // FIXME: Try to get rid of this one!
        ColumnTypeStringEnum(3),        // This is NOT a user selectable datatype - You can not create a table containing this type
	ColumnTypeBinary(4),
	ColumnTypeTable(5),
	ColumnTypeMixed(6),
	ColumnTypeDate(7);

	private ColumnType(int index) {
		this.index = index;
	}
	private int index;

	public boolean matchObject(Object obj) {
		switch (this.index) {
		case 0: return (obj instanceof Long || obj instanceof Integer || obj instanceof Short || 
				obj instanceof Byte);
		case 1: return (obj instanceof Boolean);
		case 2: return (obj instanceof String);
		case 4: return (obj instanceof byte[] || obj instanceof ByteBuffer);
		case 5: return (obj == null || obj instanceof Object[][]);
		case 6: return (obj instanceof Long || obj instanceof Integer || obj instanceof Short || 
				obj instanceof Byte || obj instanceof Boolean || obj instanceof String ||
				obj instanceof byte[] || obj instanceof ByteBuffer ||
				obj == null || obj instanceof Object[][] ||
				obj instanceof java.util.Date);
		case 7: return (obj instanceof java.util.Date);
		default: throw new RuntimeException("Invalid index in ColumnType.");
		}
	}
	
	public static ColumnType getColumnTypeForIndex(int index) {
		ColumnType[] columnTypes = values();
		for (int i=0; i<columnTypes.length; i++) {
			if (columnTypes[i].index == index)
				return columnTypes[i];
		}
		return null;
	}
}
