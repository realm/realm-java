package com.tightdb.typed;

import java.nio.ByteBuffer;

public class BinaryCursorColumn<Cursor, View, Query> extends AbstractColumn<ByteBuffer, Cursor, View, Query> {

	public BinaryCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public ByteBuffer get() {
		return cursor.tableOrView.getBinaryByteBuffer(columnIndex, cursor.getPosition());
	}

	@Override
	public void set(ByteBuffer value) {
		cursor.tableOrView.setBinaryByteBuffer(columnIndex, cursor.getPosition(), value);
	}

	@Override
	public String getReadableValue() {
		return "{binary}";
	}

}
