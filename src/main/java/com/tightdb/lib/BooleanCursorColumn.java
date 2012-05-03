package com.tightdb.lib;

import com.tightdb.TableBase;

public class BooleanCursorColumn<Cursor, Query> extends AbstractColumn<Boolean, Cursor, Query> {

	public BooleanCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public BooleanCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		super(types, table, index, name);
	}

	@Override
	public Boolean get() {
		return table.getBoolean(columnIndex, (int) cursor.getPosition());
	}

	@Override
	public void set(Boolean value) {
		table.setBoolean(columnIndex, (int) cursor.getPosition(), value);
	}
	
}
