package com.tightdb.lib;

public class StringCursorColumn<Cursor, Query> extends AbstractColumn<String, Cursor, Query> {

	public StringCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
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
