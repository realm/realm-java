package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableQuery;

public class MixedRowsetColumn<Cursor, Query> extends MixedQueryColumn<Cursor, Query> implements RowsetColumn<Serializable> {

	public MixedRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public MixedRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	@Override
	public Serializable[] getAll() {
		int count = rowset.getCount();
		String[] values = new String[count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getString(columnIndex, i);
		}
		return values;
		// return TDBUtils.deserialize(table.getBinaryData(columnIndex, (int)
		// cursor.getPosition()));
	}

	@Override
	public void setAll(Serializable value) {
		int count = rowset.getCount();
		for (int i = 0; i < count; i++) {
			rowset.setBinaryData(columnIndex, i, TDBUtils.serialize(value));
		}
	}

}
