package com.tightdb.lib;

import com.tightdb.TableBase;

public class BooleanRowsetColumn<Cursor, Query> extends BooleanQueryColumn<Cursor, Query> implements RowsetColumn<Boolean> {

	public BooleanRowsetColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	@Override
	public Boolean[] getAll() {
		return null; //table.getBoolean(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void setAll(Boolean value) {
//		table.setBoolean(columnIndex, (int) cursor.getPosition(), value);
	}

}
