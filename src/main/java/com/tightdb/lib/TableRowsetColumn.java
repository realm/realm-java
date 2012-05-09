package com.tightdb.lib;

import com.tightdb.TableQuery;


public class TableRowsetColumn<Cursor, Query, Subtable> extends TableQueryColumn<Cursor, Query, Subtable> {

	public TableRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name, Class<Subtable> subtableClass) {
		this(types, rowset, null, index, name, subtableClass);
	}
	
	public TableRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name, Class<Subtable> subtableClass) {
		super(types, rowset, query, index, name, subtableClass);
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
