package com.tightdb.lib;

import com.tightdb.TableBase;

public class BinaryRowsetColumn<Cursor, Query> extends BinaryQueryColumn<Cursor, Query> implements RowsetColumn<byte[]> {

	public BinaryRowsetColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	@Override
	public byte[][] getAll() {
		return null; //table.getBinaryData(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void setAll(byte[] value) {
		table.setBinaryData(columnIndex, (int) cursor.getPosition(), value);
	}

}
