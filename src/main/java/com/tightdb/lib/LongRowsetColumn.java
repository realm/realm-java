package com.tightdb.lib;

import com.tightdb.TableBase;

public class LongRowsetColumn<Cursor, Query> extends LongQueryColumn<Cursor, Query> implements RowsetColumn<Long> {

	public LongRowsetColumn(TableBase table, int index, String name) {
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
	public Long[] getAll() {
		return null; //table.getLong(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void setAll(Long value) {
		//table.setLong(columnIndex, (int) cursor.getPosition(), value);
	}

	public void setAll(long value) {
		set(Long.valueOf(value));
	}

}
