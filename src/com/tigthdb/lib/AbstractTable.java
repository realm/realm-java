package com.tigthdb.lib;

import java.util.Iterator;

public abstract class AbstractTable<Cursor, View> implements Iterable<Cursor> {

	public View range(long from, long to) {
		return null;
	}

	public Iterator<Cursor> iterator() {
		return null;
	}

	public Cursor first() {
		return null;
	}

	public Cursor last() {
		return null;
	}

}
