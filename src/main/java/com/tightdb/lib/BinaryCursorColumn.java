package com.tightdb.lib;

import com.tightdb.TableBase;

public class BinaryCursorColumn<Cursor, Query> extends AbstractColumn<byte[], Cursor, Query> {

	public BinaryCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	@Override
	public byte[] get() {
		return table.getBinaryData(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void set(byte[] value) {
		table.setBinaryData(columnIndex, (int) cursor.getPosition(), value);
	}
	
}
