package com.tightdb.lib;

import com.tightdb.TableBase;

public class BinaryCursorColumn<Cursor, Query> extends AbstractColumn<byte[], Cursor, Query> {

	public BinaryCursorColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public BinaryCursorColumn(TableBase table, int index, String name) {
		super(table, index, name);
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
