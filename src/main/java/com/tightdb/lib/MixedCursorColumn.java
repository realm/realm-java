package com.tightdb.lib;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.Mixed;

public class MixedCursorColumn<Cursor, Query> extends AbstractColumn<Mixed, Cursor, Query> {

	public MixedCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public Mixed get() {
		return cursor.rowset.getMixed(columnIndex, (int) cursor.getPosition());
	}
	
	@Override
	public void set(Mixed value) {
		cursor.rowset.setMixed(columnIndex, (int) cursor.getPosition(), value);
	}

	public void set(String value) {
		cursor.rowset.setMixed(columnIndex, (int) cursor.getPosition(), TightDB.mixedValue(value));
	}

	public void set(boolean value) {
		cursor.rowset.setMixed(columnIndex, (int) cursor.getPosition(), TightDB.mixedValue(value));
	}

	public void set(long value) {
		cursor.rowset.setMixed(columnIndex, (int) cursor.getPosition(), TightDB.mixedValue(value));
	}

	public void set(Date value) {
		cursor.rowset.setMixed(columnIndex, (int) cursor.getPosition(), TightDB.mixedValue(value));
	}

	public void set(ByteBuffer value) {
		cursor.rowset.setMixed(columnIndex, (int) cursor.getPosition(), TightDB.mixedValue(value));
	}

	public void set(byte[] value) {
		cursor.rowset.setMixed(columnIndex, (int) cursor.getPosition(), TightDB.mixedValue(value));
	}

}
