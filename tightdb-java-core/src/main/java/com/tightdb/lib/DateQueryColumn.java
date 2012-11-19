package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableQuery;

public class DateQueryColumn<Cursor, View, Query> extends AbstractColumn<Date, Cursor, View, Query> {

	public DateQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrViewBase tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}
// FIXME: Add methods
	
//	public Query before(Date value) {
//		return query(getQuery().less(columnIndex, value.getTime()));
//	}
//
//	public Query beforeOrEqual(Date value) {
//		return query(getQuery().lessEqual(columnIndex, value.getTime()));
//	}
//	
//	public Query after(Date value) {
//		return query(getQuery().greater(columnIndex, value.getTime()));
//	}
//
//	public Query afterOrEqual(Date value) {
//		return query(getQuery().greaterEqual(columnIndex, value.getTime()));
//	}
//
//	public Query between(Date from, Date to) {
//		return query(getQuery().between(columnIndex, from.getTime(), to.getTime()));
//	}
//
//	public Query is(Date value) {
//		return query(getQuery().equal(columnIndex, value));
//	}
	
}
