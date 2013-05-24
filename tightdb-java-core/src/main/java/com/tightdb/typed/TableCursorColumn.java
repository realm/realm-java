package com.tightdb.typed;

import java.util.Iterator;

import com.tightdb.Table;

/**
 * Type of the fields that represent a nested table column in the generated XyzRow class
 * for the Xyz entity.
 */
public class TableCursorColumn<Cursor, View, Query, Subcursor, Subtable extends AbstractTable<Subcursor, ?, ?>> extends
		AbstractColumn<Subtable, Cursor, View, Query> implements Iterable<Subcursor> {

	private final Class<Subtable> subtableClass;

	public TableCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index,
			String name, Class<Subtable> subtableClass) {
		super(types, cursor, index, name);
		this.subtableClass = subtableClass;
	}

	@Override
	public Subtable get() {
		Table subTableBase = cursor.tableOrView.getSubTable(columnIndex, cursor.getPosition());
		return AbstractSubtable.createSubtable(subtableClass, subTableBase);
	}

	@Override
	public void set(Subtable value) {
                if (value != null) {
                        // FIXME: maybe implement this is future? (or replace it with set( Object[][] ) method?
                        throw new UnsupportedOperationException("Set() for Subtables is not implemented yet"); 
		}
	}

	@Override
	public String getReadableValue() {
		return "subtable";
	}

	public long size() {
		return cursor.tableOrView.getSubTableSize(columnIndex, cursor.getPosition());
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public void clear() {
		cursor.tableOrView.clearSubTable(columnIndex, cursor.getPosition());
	}

	/**
	 * This method is deprecated, use {@link #get(long)} instead.
	 */
	@Deprecated
	public Subcursor at(long position) {
		return subcursor(position);
	}

	public Subcursor get(long position) {
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
		return AbstractCursor.createCursor(subtable.types.getCursorClass(), subtable.tableOrView, position);
	}

	@Override
	public Iterator<Subcursor> iterator() {
		return new TableOrViewIterator<Subcursor>(get());
	}

}
