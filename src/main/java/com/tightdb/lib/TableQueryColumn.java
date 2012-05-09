package com.tightdb.lib;

import com.tightdb.TableQuery;

public class TableQueryColumn<Cursor, Query, Subtable> extends AbstractColumn<Subtable, Cursor, Query> {

	protected Subtable subtable;
	protected final Class<Subtable> subtableClass;

	public TableQueryColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, rowset, query, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public String getReadable() {
		return "subtable";
	}

}
