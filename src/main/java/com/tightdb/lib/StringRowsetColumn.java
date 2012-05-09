package com.tightdb.lib;

import com.tightdb.TableQuery;


public class StringRowsetColumn<Cursor, Query> extends StringQueryColumn<Cursor, Query> {

	public StringRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public StringRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public String[] getAll() {
		int count = rowset.getCount();
		String[] values = new String[count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getString(columnIndex, i);
		}
		return values;
	}

	public void setAll(String value) {
		int count = rowset.getCount();
		for (int i = 0; i < count; i++) {
			rowset.setString(columnIndex, i, value);
		}
	}

}
