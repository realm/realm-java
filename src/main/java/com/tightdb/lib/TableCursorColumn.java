package com.tightdb.lib;

import com.tightdb.TableBase;

public class TableCursorColumn<Cursor, Query, Subtable> extends AbstractColumn<Subtable, Cursor, Query> {

	private final Class<Subtable> subtableClass;

	public TableCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, cursor, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public Subtable get() {
		TableBase subTableBase = cursor.rowset.getSubTable(columnIndex, cursor.getPosition());
		return AbstractSubtable.createSubtable(subtableClass, subTableBase);
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
