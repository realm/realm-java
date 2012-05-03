package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;

public class MixedCursorColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public MixedCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		super(types, table, index, name);
	}

	@Override
	public Serializable get() {
		return TDBUtils.deserialize(table.getBinaryData(columnIndex, (int) cursor.getPosition()));
	}

	@Override
	public void set(Serializable value) {
		table.setBinaryData(columnIndex, (int) cursor.getPosition(), TDBUtils.serialize(value));
	}
	
}
