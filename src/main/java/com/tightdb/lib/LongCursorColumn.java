package com.tightdb.lib;

public class LongCursorColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

	public LongCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public Long get() {
		return cursor.rowset.getLong(columnIndex, cursor.getPosition());
	}

	@Override
	public void set(Long value) {
		cursor.rowset.setLong(columnIndex, cursor.getPosition(), value);
	}

	public void set(long value) {
		set(Long.valueOf(value));
	}

}
