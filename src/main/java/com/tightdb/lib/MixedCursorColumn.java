package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;

public class MixedCursorColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedCursorColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public MixedCursorColumn(TableBase table, int index, String name) {
		super(table, index, name);
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
