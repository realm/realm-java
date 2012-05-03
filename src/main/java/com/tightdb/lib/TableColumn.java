package com.tightdb.lib;

import com.tightdb.TableBase;

public class TableColumn<Cursor, Query, Subtable> extends TableQueryColumn<Cursor, Query, Subtable> {

	public TableColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name, Class<Subtable> subtableClass) {
		super(table, cursor, index, name, subtableClass);
	}
	
	public TableColumn(TableBase table, int index, String name, Class<Subtable> subtableClass) {
		super(table, index, name, subtableClass);
	}

	@Override
	public Subtable get() {
		return subtable;
	}

	@Override
	public void set(Subtable value) {
		throw new UnsupportedOperationException(); // FIXME: implement this
	}

}
