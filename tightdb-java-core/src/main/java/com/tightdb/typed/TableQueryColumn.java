package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class TableQueryColumn<Cursor, View, Query, Subtable> extends AbstractColumn<Subtable, Cursor, View, Query> {

	protected Subtable subtable;
	protected final Class<Subtable> subtableClass;

	public TableQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, tableOrView, query, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public String getReadableValue() {
		return "subtable";
	}

}
