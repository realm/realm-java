package com.tightdb.lib;

import java.nio.ByteBuffer;

public class BinaryCursorColumn<Cursor, Query> extends AbstractColumn<ByteBuffer, Cursor, Query> {

	public BinaryCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public ByteBuffer get() {
		return cursor.rowset.getBinary(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void set(ByteBuffer value) {
		cursor.rowset.setBinary(columnIndex, (int) cursor.getPosition(), value);
	}

	@Override
	public String getReadableValue() {
		return "{binary}";
	}
	
}
