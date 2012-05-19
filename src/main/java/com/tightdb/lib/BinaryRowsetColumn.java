package com.tightdb.lib;

import com.tightdb.TableQuery;

public class BinaryRowsetColumn<Cursor, Query> extends BinaryQueryColumn<Cursor, Query> implements RowsetColumn<byte[]> {

	public BinaryRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public BinaryRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	@Override
	public byte[][] getAll() {
		long count = rowset.size();
		byte[][] values = new byte[(int) count][];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getBinary(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(byte[] value) {
		long count = rowset.size();
		for (int i = 0; i < count; i++) {
			rowset.setBinary(columnIndex, i, value);
		}
	}

}
