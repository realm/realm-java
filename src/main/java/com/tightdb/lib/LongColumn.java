package com.tightdb.lib;

import com.tightdb.TableBase;

public class LongColumn<Cursor, Query> extends LongQueryColumn<Cursor, Query> {

	public LongColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public LongColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	public int sum() {
		return 0;
	}

	public int max() {
		return 0;
	}

	public int min() {
		return 0;
	}

	public int count() {
		return 0;
	}

	public int average() {
		return 0;
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
