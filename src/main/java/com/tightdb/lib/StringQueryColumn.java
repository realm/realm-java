package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class StringQueryColumn<Cursor, Query> extends AbstractColumn<String, Cursor, Query> {

	public StringQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

	public Query is(String value) {
		return query(getQuery().equals(columnIndex, value));
	}

	public Query isnt(String value) {
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
