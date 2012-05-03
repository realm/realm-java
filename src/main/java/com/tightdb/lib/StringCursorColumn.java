package com.tightdb.lib;

import com.tightdb.TableBase;

public class StringCursorColumn<Cursor, Query> extends AbstractColumn<String, Cursor, Query> {

	public StringCursorColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public StringCursorColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	@Override
	public String get() {
		return table.getString(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void set(String value) {
		table.setString(columnIndex, (int) cursor.getPosition(), value);
	}

}
