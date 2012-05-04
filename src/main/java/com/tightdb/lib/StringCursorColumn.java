package com.tightdb.lib;

import com.tightdb.TableBase;

public class StringCursorColumn<Cursor, Query> extends AbstractColumn<String, Cursor, Query> {

	public StringCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
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
