package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class DateQueryColumn<Cursor, Query> extends AbstractColumn<Date, Cursor, Query> {

	public DateQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public DateQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

	public Query before(Date value) {
		return query(getQuery().lessThan(columnIndex, value.getTime()));
	}

	public Query beforeOrEqual(Date value) {
		return query(getQuery().lessThanEqualTo(columnIndex, value.getTime()));
	}
	
	public Query after(Date value) {
		return query(getQuery().greater(columnIndex, value.getTime()));
	}

	public Query afterOrEqual(Date value) {
		return query(getQuery().greaterEqual(columnIndex, value.getTime()));
	}

	public Query between(Date from, Date to) {
		return query(getQuery().between(columnIndex, from.getTime(), to.getTime()));
	}

	public Query is(Date value) {
		return query(getQuery().equals(columnIndex, value.getTime()));
	}
	
}
