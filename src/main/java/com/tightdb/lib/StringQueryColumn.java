package com.tightdb.lib;

import com.tightdb.TableQuery;

public class StringQueryColumn<Cursor, View, Query> extends AbstractColumn<String, Cursor, View, Query> {

	public StringQueryColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public Query equal(String value) {
		return query(getQuery().equal(columnIndex, value));
	}
	public Query eq(String value) {
		return query(getQuery().equal(columnIndex, value));
	}

	public Query notEqual(String value) {
		return query(getQuery().notEqual(columnIndex, value));
	}
	public Query neq(String value) {
		return query(getQuery().notEqual(columnIndex, value));
	}

	public Query startsWith(String value) {
		return query(getQuery().beginsWith(columnIndex, value));
	}

	public Query endWith(String value) {
		return query(getQuery().endsWith(columnIndex, value));
	}

	public Query contains(String value) {
		return query(getQuery().contains(columnIndex, value));
	}

}
