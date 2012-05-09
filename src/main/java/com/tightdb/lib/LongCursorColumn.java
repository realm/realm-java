package com.tightdb.lib;

public class LongCursorColumn<Cursor, Query> extends AbstractColumn<Long, Cursor, Query> {

	public LongCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public Long get() {
		return cursor.rowset.getLong(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void set(Long value) {
		cursor.rowset.setLong(columnIndex, (int) cursor.getPosition(), value);
	}

	public void set(long value) {
		set(Long.valueOf(value));
	}

}
