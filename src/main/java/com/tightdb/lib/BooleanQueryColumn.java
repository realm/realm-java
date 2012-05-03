package com.tightdb.lib;

import com.tightdb.TableBase;

public class BooleanQueryColumn<Cursor, Query> extends AbstractColumn<Boolean, Cursor, Query> {

	public BooleanQueryColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public BooleanQueryColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

}
