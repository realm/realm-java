package com.tightdb.lib;

import com.tightdb.SubTableBase;
import com.tightdb.TableBase;

public class TableCursorColumn<Cursor, Query, Subtable> extends AbstractColumn<Subtable, Cursor, Query> {

	private final Class<Subtable> subtableClass;

	public TableCursorColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name, Class<Subtable> subtableClass) {
		super(types, table, cursor, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public Subtable get() {
		SubTableBase sub = table.getSubTable(columnIndex, (int) cursor.getPosition());
		
		try {
			Subtable subtable = subtableClass.getConstructor(SubTableBase.class).newInstance(sub);
			return subtable;
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

	@Override
	public void set(Subtable value) {
		throw new UnsupportedOperationException(); // FIXME: maybe implement this is future?
	}
	
	@Override
	public String getReadable() {
		return "subtable";
	}

}
