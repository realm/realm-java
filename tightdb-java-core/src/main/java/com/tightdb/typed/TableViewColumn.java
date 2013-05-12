package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class TableViewColumn<Cursor, View, Query, Subtable> extends
		TableTableOrViewColumn<Cursor, View, Query, Subtable> {

	public TableViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, view, index, name, subtableClass);
	}

	public TableViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
			String name, Class<Subtable> subtableClass) {
		super(types, view, query, index, name, subtableClass);
	}

}
