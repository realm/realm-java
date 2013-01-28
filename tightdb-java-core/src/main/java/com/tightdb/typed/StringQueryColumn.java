package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class StringQueryColumn<Cursor, View, Query> extends AbstractColumn<String, Cursor, View, Query> {

	public StringQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

	public Query equal(String value) {
		return query(getQuery().equal(columnIndex, value));
	}
	public Query eq(String value) {
		return query(getQuery().eq(columnIndex, value));
	}
	public Query equal(String value, boolean caseSensitive) {
		return query(getQuery().equal(columnIndex, value, caseSensitive));
	}
	public Query eq(String value, boolean caseSensitive) {
		return query(getQuery().eq(columnIndex, value, caseSensitive));
	}

	public Query notEqual(String value) {
		return query(getQuery().notEqual(columnIndex, value));
	}
	public Query neq(String value) {
		return query(getQuery().neq(columnIndex, value));
	}
	public Query notEqual(String value, boolean caseSensitive) {
		return query(getQuery().notEqual(columnIndex, value, caseSensitive));
	}
	public Query neq(String value, boolean caseSensitive) {
		return query(getQuery().neq(columnIndex, value, caseSensitive));
	}

	public Query startsWith(String value) {
		return query(getQuery().beginsWith(columnIndex, value));
	}
	public Query startsWith(String value, boolean caseSensitive) {
		return query(getQuery().beginsWith(columnIndex, value, caseSensitive));
	}

	public Query endsWith(String value) {
		return query(getQuery().endsWith(columnIndex, value));
	}
	public Query endsWith(String value, boolean caseSensitive) {
		return query(getQuery().endsWith(columnIndex, value, caseSensitive));
	}

	public Query contains(String value) {
		return query(getQuery().contains(columnIndex, value));
	}
	public Query contains(String value, boolean caseSensitive) {
		return query(getQuery().contains(columnIndex, value, caseSensitive));
	}

}
