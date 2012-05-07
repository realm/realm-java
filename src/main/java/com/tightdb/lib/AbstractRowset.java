package com.tightdb.lib;

import java.util.Iterator;

public abstract class AbstractRowset<Cursor, View> implements Iterable<Cursor> {

	public abstract int size();

	public boolean isEmpty() {
		return size() == 0;
	}

	public abstract void clear();
	
	@Override
	public Iterator<Cursor> iterator() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
}
