package com.tightdb.lib;

import java.util.Iterator;

import com.tightdb.TableBase;

public class TableCursorColumn<Cursor, Query, Subcursor, Subtable extends AbstractTable<Subcursor, ?, ?>> extends
		AbstractColumn<Subtable, Cursor, Query> implements Iterable<Subcursor> {

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

	/********************** CONVENIENCE METHODS FOR THE SUBTABLES ********************/

	public long size() {
		return get().size();
	}

	public boolean isEmpty() {
		return get().isEmpty();
	}

	public void clear() {
		get().clear();
	}

	public Subcursor at(long position) {
		return cursor(position);
	}

	public Subcursor first() {
		return cursor(0);
	}

	public Subcursor last() {
		return cursor(size() - 1);
	}

	protected Subcursor cursor(long position) {
		Subtable subtable = get();
		return AbstractCursor.createCursor(subtable.types.getCursorClass(), subtable.rowset, position);
	}

	@Override
	public Iterator<Subcursor> iterator() {
		return new RowsetIterator<Subcursor>(get());
	}

}
