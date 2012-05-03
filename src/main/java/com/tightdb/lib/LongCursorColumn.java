package com.tightdb.lib;

import com.tightdb.TableBase;

public class LongCursorColumn<Cursor, Query> extends AbstractColumn<Long, Cursor, Query> {

	public LongCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public LongCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		super(types, table, index, name);
	}

	@Override
	public Long get() {
		return table.getLong(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void set(Long value) {
		table.setLong(columnIndex, (int) cursor.getPosition(), value);
	}

	public void set(long value) {
		set(Long.valueOf(value));
	}

}
