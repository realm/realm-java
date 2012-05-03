package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;

public class MixedRowsetColumn<Cursor, Query> extends MixedQueryColumn<Cursor, Query> implements RowsetColumn<Serializable> {

	public MixedRowsetColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	@Override
	public Serializable[] getAll() {
		return null;
		// return TDBUtils.deserialize(table.getBinaryData(columnIndex, (int)
		// cursor.getPosition()));
	}

	@Override
	public void setAll(Serializable value) {
		// table.setBinaryData(columnIndex, (int) cursor.getPosition(),
		// TDBUtils.serialize(value));
	}

}
