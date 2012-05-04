package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class BinaryQueryColumn<Cursor, Query> extends AbstractColumn<byte[], Cursor, Query> {

	public BinaryQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public BinaryQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

}
