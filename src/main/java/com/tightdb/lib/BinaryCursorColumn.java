package com.tightdb.lib;

public class BinaryCursorColumn<Cursor, Query> extends AbstractColumn<byte[], Cursor, Query> {

	public BinaryCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public byte[] get() {
		return cursor.rowset.getBinaryData(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void set(byte[] value) {
		cursor.rowset.setBinaryData(columnIndex, (int) cursor.getPosition(), value);
	}

}
