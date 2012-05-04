package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class BooleanQueryColumn<Cursor, Query> extends AbstractColumn<Boolean, Cursor, Query> {

	public BooleanQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public BooleanQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

	public Query is(boolean value) {
		return query(getQuery().equals(columnIndex, value));
	}

	public Query isnt(boolean value) {
		return query(getQuery().equals(columnIndex, !value));
	}
	
}
