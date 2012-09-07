package com.tightdb.lib;

import java.util.Iterator;

public abstract class AbstractRowset<Cursor, View, Query> implements Iterable<Cursor> {

	protected final EntityTypes<?, View, Cursor, Query> types;
	protected final IRowsetBase rowset;

	public AbstractRowset(EntityTypes<?, View, Cursor, Query> types, IRowsetBase rowset) {
		this.types = types;
		this.rowset = rowset;
	}

	public long size() {
		return rowset.size();
	}

	public boolean isEmpty() {
		return rowset.isEmpty();
	}

	public void clear() {
		rowset.clear();
	}

	public void remove(long rowIndex) {
		rowset.remove(rowIndex);
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
		return AbstractCursor.createCursor(types.getCursorClass(), rowset, position);
	}

	@Override
	public Iterator<Cursor> iterator() {
		return new RowsetIterator<Cursor>(this);
	}

	public abstract String getName();

	public String toJson() {
		return rowset.toJson();
	}
	
}
