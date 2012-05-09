package com.tightdb.lib;

import com.tightdb.TableQuery;

public class BooleanQueryColumn<Cursor, Query> extends AbstractColumn<Boolean, Cursor, Query> {

	public BooleanQueryColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public Query is(boolean value) {
		return query(getQuery().equals(columnIndex, value));
	}

	public Query isnt(boolean value) {
		return query(getQuery().equals(columnIndex, !value));
	}

}
