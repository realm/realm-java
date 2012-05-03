package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;

public class MixedQueryColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedQueryColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public MixedQueryColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

}
