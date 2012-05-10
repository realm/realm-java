package com.tightdb.lib;

import com.tightdb.SubTableBase;

public class TableCursorColumn<Cursor, Query, Subtable> extends AbstractColumn<Subtable, Cursor, Query> {

	private final Class<Subtable> subtableClass;

	public TableCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, cursor, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public Subtable get() {
		SubTableBase sub = cursor.rowset.getSubTable(columnIndex, (int) cursor.getPosition());

		try {
			Subtable subtable = subtableClass.getConstructor(SubTableBase.class).newInstance(sub);
			return subtable;
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

	@Override
	public void set(Subtable value) {
		throw new UnsupportedOperationException(); // FIXME: maybe implement
													// this is future?
	}

	@Override
	public String getReadableValue() {
		return "subtable";
	}

}
