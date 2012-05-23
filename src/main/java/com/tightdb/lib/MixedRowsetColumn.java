package com.tightdb.lib;

import com.tightdb.Mixed;
import com.tightdb.TableQuery;

public class MixedRowsetColumn<Cursor, Query> extends MixedQueryColumn<Cursor, Query> implements RowsetColumn<Mixed> {

	public MixedRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public MixedRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	@Override
	public Mixed[] getAll() {
		long count = rowset.size();
		Mixed[] values = new Mixed[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getMixed(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Mixed value) {
		long count = rowset.size();
		for (int i = 0; i < count; i++) {
			rowset.setMixed(columnIndex, i, value);
		}
	}

}
