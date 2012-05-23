package com.tightdb.lib;

import com.tightdb.TableQuery;

public class LongQueryColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

	public LongQueryColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public Query greaterThan(int value) {
		return query(getQuery().greater(columnIndex, value));
	}

	public Query greaterOrEqual(int value) {
		return query(getQuery().greaterEqual(columnIndex, value));
	}

	public Query lessThan(int value) {
		return query(getQuery().less(columnIndex, value));
	}

	public Query lessOrEqual(int value) {
		return query(getQuery().lessEqual(columnIndex, value));
	}

	public Query between(int from, int to) {
		return query(getQuery().between(columnIndex, from, to));
	}

	public Query is(long value) {
		return query(getQuery().equal(columnIndex, value));
	}

}
