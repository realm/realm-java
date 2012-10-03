package com.tightdb.lib;

import com.tightdb.TableQuery;

public class StringRowsetColumn<Cursor, View, Query> extends StringQueryColumn<Cursor, View, Query> {

	public StringRowsetColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public StringRowsetColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public String[] getAll() {
		long count = rowset.size();
		String[] values = new String[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getString(columnIndex, i);
		}
		return values;
	}

	public void setAll(String value) {
		long count = rowset.size();
		for (int i = 0; i < count; i++) {
			rowset.setString(columnIndex, i, value);
		}
	}

	public Cursor findFirst(String value) {
		return cursor(rowset.findFirstString(columnIndex, value));
	}

	public View findAll(String value) {
		return view(rowset.findAllString(columnIndex, value));
	}
}
