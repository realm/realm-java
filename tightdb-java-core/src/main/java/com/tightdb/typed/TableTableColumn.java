package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class TableTableColumn<Cursor, View, Query, Subtable> extends
		TableTableOrViewColumn<Cursor, View, Query, Subtable> {

	public TableTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, table, index, name, subtableClass);
	}

	public TableTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
			String name, Class<Subtable> subtableClass) {
		super(types, table, query, index, name, subtableClass);
	}

}
