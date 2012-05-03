package com.tightdb.lib;

import com.tightdb.TableBase;

public class StringRowsetColumn<Cursor, Query> extends StringQueryColumn<Cursor, Query> {

	public StringRowsetColumn(TableBase table, int index, String name) {
		super(table, index, name);
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
