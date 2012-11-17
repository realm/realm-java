package com.tightdb.lib;

public class StringCursorColumn<Cursor, View, Query> extends AbstractColumn<String, Cursor, View, Query> {

	public StringCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public String get() {
		return cursor.rowset.getString(columnIndex, cursor.getPosition());
	}

	@Override
	public void set(String value) {
		cursor.rowset.setString(columnIndex, cursor.getPosition(), value);
	}

}
