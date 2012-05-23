package com.tightdb.lib;

import java.nio.ByteBuffer;

public class BinaryCursorColumn<Cursor, View, Query> extends AbstractColumn<ByteBuffer, Cursor, View, Query> {

	public BinaryCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public ByteBuffer get() {
		return cursor.rowset.getBinary(columnIndex, cursor.getPosition());
	}

	@Override
	public void set(ByteBuffer value) {
		cursor.rowset.setBinary(columnIndex, cursor.getPosition(), value);
	}

	@Override
	public String getReadableValue() {
		return "{binary}";
	}

}
