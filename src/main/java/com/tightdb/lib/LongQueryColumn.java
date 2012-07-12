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

	public Query greaterThan(long value) {
		return query(getQuery().greaterThan(columnIndex, value));
	}

	public Query gt(long value) {
		return query(getQuery().greaterThan(columnIndex, value));
	}

	public Query greaterThanOrEqual(long value) {
		return query(getQuery().greaterThanOrEqual(columnIndex, value));
	}

	public Query gte(long value) {
		return query(getQuery().greaterThanOrEqual(columnIndex, value));
	}

	public Query lessThan(long value) {
		return query(getQuery().lessThan(columnIndex, value));
	}

	public Query lt(long value) {
		return query(getQuery().lessThan(columnIndex, value));
	}

	public Query lessThanOrEqual(long value) {
		return query(getQuery().lessThanOrEqual(columnIndex, value));
	}

	public Query lte(long value) {
		return query(getQuery().lessThanOrEqual(columnIndex, value));
	}

	public Query between(long from, long to) {
		return query(getQuery().between(columnIndex, from, to));
	}

}
