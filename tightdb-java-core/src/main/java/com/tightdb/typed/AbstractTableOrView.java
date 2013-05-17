package com.tightdb.typed;

import java.util.Iterator;

import com.tightdb.TableOrView;

/**
 * Super-type of the generated XyzTable and XyzView classes for the Xyz entity,
 * having common operations for both table and view.
 */
public abstract class AbstractTableOrView<Cursor, View, Query> implements Iterable<Cursor> {

	protected final EntityTypes<?, View, Cursor, Query> types;
	protected final TableOrView tableOrView;

	public AbstractTableOrView(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView) {
		this.types = types;
		this.tableOrView = tableOrView;
	}

	public long size() {
		return tableOrView.size();
	}

	public boolean isEmpty() {
		return tableOrView.isEmpty();
	}

	public void clear() {
		tableOrView.clear();
	}

	public void remove(long rowIndex) {
		tableOrView.remove(rowIndex);
	}

	public void removeLast() {
		tableOrView.removeLast();
	}

/*	TODO:
 * public View range(long from, long to) {
		throw new UnsupportedOperationException();
	}
*/
	public Cursor at(long position) {
		return cursor(position);
	}

	public Cursor first() {
		return cursor(0);
	}

	public Cursor last() {
		return cursor(size() - 1);
	}

	protected Cursor cursor(long position) {
		return AbstractCursor.createCursor(types.getCursorClass(), tableOrView, position);
	}

	@Override
	public Iterator<Cursor> iterator() {
		return new TableOrViewIterator<Cursor>(this);
	}

	public abstract String getName();

	public String toJson() {
		return tableOrView.toJson();
	}
	
}
