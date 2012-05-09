package com.tightdb.lib;

import com.tightdb.TableQuery;

public class BinaryQueryColumn<Cursor, Query> extends AbstractColumn<byte[], Cursor, Query> {

	public BinaryQueryColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

}
