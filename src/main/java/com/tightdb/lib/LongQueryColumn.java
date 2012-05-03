package com.tightdb.lib;

import com.tightdb.TableBase;

public class LongQueryColumn<Cursor, Query> extends AbstractColumn<Long, Cursor, Query> {

	public LongQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public LongQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		super(types, table, index, name);
	}

	public Query greaterThan(int value) {
		return null;
	}

	public Query lessThan(int value) {
		return null;
	}

	public Query between(int from, int to) {
		return null;
	}

	public Query is(long value) {
		return null;
	}

}
