package com.tightdb.lib;

import com.tightdb.TableQuery;


public class LongRowsetColumn<Cursor, Query> extends LongQueryColumn<Cursor, Query> implements RowsetColumn<Long> {

	public LongRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public LongRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public long sum() {
		return getView().sum(columnIndex);
	}

	public long maximum() {
		return getView().maximum(columnIndex);
	}

	public long minimum() {
		return getView().minimum(columnIndex);
	}

	@Override
	public Long[] getAll() {
		long count = rowset.size();
		Long[] values = new Long[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getLong(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Long value) {
		long count = rowset.size();
		for (int i = 0; i < count; i++) {
			rowset.setLong(columnIndex, i, value);
		}
	}

	public void setAll(long value) {
		setAll(new Long(value));
	}

}
