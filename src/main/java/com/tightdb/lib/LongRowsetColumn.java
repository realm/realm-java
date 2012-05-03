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

	public int average() {
		return 0;
	}

	@Override
	public Long[] getAll() {
		int count = table.getCount();
		Long[] values = new Long[count];
		for (int i = 0; i < count; i++) {
			values[i] = table.getLong(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Long value) {
		int count = table.getCount();
		for (int i = 0; i < count; i++) {
			table.setLong(columnIndex, i, value);
		}
	}

	public void setAll(long value) {
		setAll(new Long(value));
	}

}
