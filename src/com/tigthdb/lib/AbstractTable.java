package com.tigthdb.lib;

public abstract class AbstractTable<Cursor, View> extends AbstractRowset<Cursor, View> implements
		Iterable<Cursor> {

	public View range(long from, long to) {
		return null;
	}

	public Cursor first() {
		return null;
	}

	public Cursor last() {
		return null;
	}

}
