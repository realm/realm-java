package com.tightdb.lib;

import com.tightdb.TableQuery;

public class BooleanRowsetColumn<Cursor, Query> extends BooleanQueryColumn<Cursor, Query> implements RowsetColumn<Boolean> {

	public BooleanRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public BooleanRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	@Override
	public Boolean[] getAll() {
		long count = rowset.size();
		Boolean[] values = new Boolean[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getBoolean(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Boolean value) {
		long count = rowset.size();
		for (int i = 0; i < count; i++) {
			rowset.setBoolean(columnIndex, i, value);
		}
	}

}
