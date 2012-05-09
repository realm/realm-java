package com.tightdb.lib;

import java.util.Iterator;

public abstract class AbstractRowset<Cursor, View> implements Iterable<Cursor> {

	protected final EntityTypes<?, View, Cursor, ?> types;
	protected final IRowsetBase rowset;

	public AbstractRowset(EntityTypes<?, View, Cursor, ?> types, IRowsetBase rowset) {
		this.types = types;
		this.rowset = rowset;
	}

	public abstract int size();

	public boolean isEmpty() {
		return size() == 0;
	}

	public abstract void clear();

	public View range(long from, long to) {
		throw new UnsupportedOperationException();
	}

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
		Cursor cursor;

		try {
			cursor = types.getCursorClass().getDeclaredConstructor(IRowsetBase.class, long.class).newInstance(rowset, position);
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate a cursor!", e);
		}

		return cursor;
	}

	@Override
	public Iterator<Cursor> iterator() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

}
