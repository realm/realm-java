package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class DoubleViewColumn<Cursor, View, Query> extends DoubleTableOrViewColumn<Cursor, View, Query> {

	public DoubleViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name) {
		super(types, view, index, name);
	}

	public DoubleViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
			String name) {
		super(types, view, query, index, name);
	}

}
