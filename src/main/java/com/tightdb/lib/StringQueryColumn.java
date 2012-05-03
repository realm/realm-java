package com.tightdb.lib;

import com.tightdb.TableBase;

public class StringQueryColumn<Cursor, Query> extends AbstractColumn<String, Cursor, Query> {

	public StringQueryColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	public Query startsWith(String value) {
		return null;
	}

	public Query endWith(String value) {
		return null;
	}

	public Query contains(String value) {
		return null;
	}

	public Query matches(String regex) {
		return null;
	}

}
