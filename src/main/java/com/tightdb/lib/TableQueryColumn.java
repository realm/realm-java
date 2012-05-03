package com.tightdb.lib;

import com.tightdb.TableBase;

public class TableQueryColumn<Cursor, Query, Subtable> extends AbstractColumn<Subtable, Cursor, Query> {

	protected Subtable subtable;

	public TableQueryColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name, Class<Subtable> subtableClass) {
		super(table, cursor, index, name);
		try {
			subtable = subtableClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

	public TableQueryColumn(TableBase table, int index, String name, Class<Subtable> subtableClass) {
		super(table, index, name);
		try {
			subtable = subtableClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

	@Override
	public String getReadable() {
		return "subtable";
	}

}
