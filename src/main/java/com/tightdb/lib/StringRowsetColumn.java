package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class StringRowsetColumn<Cursor, Query> extends StringQueryColumn<Cursor, Query> {

	public StringRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

	public String[] getAll() {
		int count = table.getCount();
		String[] values = new String[count];
		for (int i = 0; i < count; i++) {
			values[i] = table.getString(columnIndex, i);
		}
		return values;
	}

	public void setAll(String value) {
		int count = table.getCount();
		for (int i = 0; i < count; i++) {
			table.setString(columnIndex, i, value);
		}
	}

}
