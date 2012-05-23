package com.tightdb.lib;

import com.tightdb.TableQuery;

public class BooleanQueryColumn<Cursor, View, Query> extends AbstractColumn<Boolean, Cursor, View, Query> {

	public BooleanQueryColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public Query is(boolean value) {
		return query(getQuery().equal(columnIndex, value));
	}

	public Query isnt(boolean value) {
		return query(getQuery().equal(columnIndex, !value));
	}

}
