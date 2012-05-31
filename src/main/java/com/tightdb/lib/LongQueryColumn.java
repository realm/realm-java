package com.tightdb.lib;

import com.tightdb.TableQuery;

public class LongQueryColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

	public LongQueryColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	public Query equal(long value) {
		return query(getQuery().equal(columnIndex, value));
	}
	public Query eq(long value) {
		return query(getQuery().equal(columnIndex, value));
	}
	
	public Query notQqual(long value) {
		return query(getQuery().notEqual(columnIndex, value));
	}
	public Query neq(long value) {
		return query(getQuery().notEqual(columnIndex, value));
	}
	
	public Query greaterThan(int value) {
		return query(getQuery().greaterThan(columnIndex, value));
	}
	public Query gt(int value) {
		return query(getQuery().greaterThan(columnIndex, value));
	}

	public Query greaterThanOrEqual(int value) {
		return query(getQuery().greaterThanOrEqual(columnIndex, value));
	}
	public Query gte(int value) {
		return query(getQuery().greaterThanOrEqual(columnIndex, value));
	}
	
	public Query lessThan(int value) {
		return query(getQuery().lessThan(columnIndex, value));
	}
	public Query lt(int value) {
		return query(getQuery().lessThan(columnIndex, value));
	}

	public Query lessThanOrEqual(int value) {
		return query(getQuery().lessThanOrEqual(columnIndex, value));
	}
	public Query lte(int value) {
		return query(getQuery().lessThanOrEqual(columnIndex, value));
	}
	
	public Query between(int from, int to) {
		return query(getQuery().between(columnIndex, from, to));
	}
	public long minimum() {
		return 0;
	}
}
