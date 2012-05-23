package com.tightdb.lib;

import java.util.Iterator;

import com.tightdb.TableBase;

public class TableCursorColumn<Cursor, View, Query, Subcursor, Subtable extends AbstractTable<Subcursor, ?, ?>> extends
		AbstractColumn<Subtable, Cursor, View, Query> implements Iterable<Subcursor> {

	private final Class<Subtable> subtableClass;

	public TableCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name,
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
		return subcursor(position);
	}

	public Subcursor first() {
		return subcursor(0);
	}

	public Subcursor last() {
		return subcursor(size() - 1);
	}

	protected Subcursor subcursor(long position) {
		Subtable subtable = get();
		return AbstractCursor.createCursor(subtable.types.getCursorClass(), subtable.rowset, position);
	}

	@Override
	public Iterator<Subcursor> iterator() {
		return new RowsetIterator<Subcursor>(get());
	}

}
