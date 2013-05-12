package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class DateTableColumn<Cursor, View, Query> extends DateTableOrViewColumn<Cursor, View, Query> {

	public DateTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
		super(types, table, index, name);
	}

	public DateTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
			String name) {
		super(types, table, query, index, name);
	}

}
