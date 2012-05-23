package com.tightdb.lib;

import java.nio.ByteBuffer;

import com.tightdb.TableQuery;

public class BinaryQueryColumn<Cursor, View, Query> extends AbstractColumn<ByteBuffer, Cursor, View, Query> {

	public BinaryQueryColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

}
