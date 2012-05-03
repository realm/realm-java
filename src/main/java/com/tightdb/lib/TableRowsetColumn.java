package com.tightdb.lib;

import com.tightdb.TableBase;

public class TableRowsetColumn<Cursor, Query, Subtable> extends TableQueryColumn<Cursor, Query, Subtable> {

	public TableRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name, Class<Subtable> subtableClass) {
		super(types, table, cursor, index, name, subtableClass);
	}
	
	public TableRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name, Class<Subtable> subtableClass) {
		super(types, table, index, name, subtableClass);
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
