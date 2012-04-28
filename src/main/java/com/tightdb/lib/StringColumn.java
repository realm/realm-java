package com.tightdb.lib;

import com.tightdb.TableBase;


public class StringColumn<Cursor, Query> extends AbstractColumn<String, Cursor, Query> {

	public StringColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public StringColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	public Query startsWith(String value) {
		return null;
	}

	public Query endWith(String value) {
		return null;
	}

	public Query contains(String value) {
		return null;
	}

	public Query matches(String regex) {
		return null;
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
