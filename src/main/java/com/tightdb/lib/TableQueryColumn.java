package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class TableQueryColumn<Cursor, Query, Subtable> extends AbstractColumn<Subtable, Cursor, Query> {

	protected Subtable subtable;
	protected final Class<Subtable> subtableClass;

	public TableQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name, Class<Subtable> subtableClass) {
		super(types, table, query, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public String getReadable() {
		return "subtable";
	}

}
