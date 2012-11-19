package com.tightdb.lib;

import com.tightdb.TableQuery;

public class TableQueryColumn<Cursor, View, Query, Subtable> extends AbstractColumn<Subtable, Cursor, View, Query> {

	protected Subtable subtable;
	protected final Class<Subtable> subtableClass;

	public TableQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrViewBase tableOrView, TableQuery query, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, tableOrView, query, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public String getReadableValue() {
		return "subtable";
	}

}
