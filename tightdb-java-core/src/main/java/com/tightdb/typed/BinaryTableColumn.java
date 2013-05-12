package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class BinaryTableColumn<Cursor, View, Query> extends BinaryTableOrViewColumn<Cursor, View, Query> {

	public BinaryTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
		super(types, table, index, name);
	}

	public BinaryTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
			String name) {
		super(types, table, query, index, name);
	}

}
