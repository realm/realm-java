package com.tightdb.lib;

import com.tightdb.TableBase;

public class TableColumn<Cursor, Query, Subtable> extends AbstractColumn<Subtable, Cursor, Query> {

	private Subtable subtable;

	public TableColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name, Class<Subtable> subtableClass) {
		super(table, cursor, index, name);
		try {
			subtable = subtableClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}
	
	public TableColumn(TableBase table, int index, String name, Class<Subtable> subtableClass) {
		super(table, index, name);
		try {
			subtable = subtableClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

	@Override
	public Subtable get() {
		return subtable;
	}

	@Override
	public void set(Subtable value) {
		throw new UnsupportedOperationException(); // FIXME: implement this
	}

	@Override
	public String getReadable() {
		return "subtable";
	}

}
