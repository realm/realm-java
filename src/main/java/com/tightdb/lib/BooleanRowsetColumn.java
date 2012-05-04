package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class BooleanRowsetColumn<Cursor, Query> extends BooleanQueryColumn<Cursor, Query> implements RowsetColumn<Boolean> {

	public BooleanRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

	@Override
	public Boolean[] getAll() {
		int count = table.getCount();
		Boolean[] values = new Boolean[count];
		for (int i = 0; i < count; i++) {
			values[i] = table.getBoolean(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Boolean value) {
		int count = table.getCount();
		for (int i = 0; i < count; i++) {
			table.setBoolean(columnIndex, i, value);
		}
	}

}
