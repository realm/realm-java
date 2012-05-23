package com.tightdb.lib;

import java.nio.ByteBuffer;

import com.tightdb.TableQuery;

public class BinaryRowsetColumn<Cursor, View, Query> extends BinaryQueryColumn<Cursor, View, Query> implements RowsetColumn<ByteBuffer> {

	public BinaryRowsetColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public BinaryRowsetColumn(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	@Override
	public ByteBuffer[] getAll() {
		long count = rowset.size();
		ByteBuffer[] values = new ByteBuffer[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getBinary(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(ByteBuffer value) {
		long count = rowset.size();
		for (int i = 0; i < count; i++) {
			rowset.setBinary(columnIndex, i, value);
		}
	}

}
