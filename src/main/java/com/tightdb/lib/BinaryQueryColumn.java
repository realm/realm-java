package com.tightdb.lib;

import com.tightdb.TableBase;

public class BinaryQueryColumn<Cursor, Query> extends AbstractColumn<byte[], Cursor, Query> {

	public BinaryQueryColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public BinaryQueryColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

}
