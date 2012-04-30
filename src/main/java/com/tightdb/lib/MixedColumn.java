package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;

public class MixedColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public MixedColumn(TableBase table, int index, String name) {
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
