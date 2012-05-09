package com.tightdb.lib;

import java.io.Serializable;

public class MixedCursorColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public Serializable get() {
		return TDBUtils.deserialize(cursor.rowset.getBinaryData(columnIndex, (int) cursor.getPosition()));
	}

	@Override
	public void set(Serializable value) {
		cursor.rowset.setBinaryData(columnIndex, (int) cursor.getPosition(), TDBUtils.serialize(value));
	}

}
