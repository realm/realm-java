package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;

public class MixedRowsetColumn<Cursor, Query> extends MixedQueryColumn<Cursor, Query> implements RowsetColumn<Serializable> {

	public MixedRowsetColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	@Override
	public Serializable[] getAll() {
		int count = table.getCount();
		String[] values = new String[count];
		for (int i = 0; i < count; i++) {
			values[i] = table.getString(columnIndex, i);
		}
		return values;
		// return TDBUtils.deserialize(table.getBinaryData(columnIndex, (int)
		// cursor.getPosition()));
	}

	@Override
	public void setAll(Serializable value) {
		int count = table.getCount();
		for (int i = 0; i < count; i++) {
			table.setBinaryData(columnIndex, i, TDBUtils.serialize(value));
		}
	}

}
