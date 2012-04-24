package com.tigthdb.lib;

public abstract class AbstractCursor<Cursor> {

	public Cursor next() {
		return null;
	}

	public Cursor previous() {
		return null;
	}

	public Cursor before(long delta) {
		return null;
	}

	public Cursor after(long delta) {
		return null;
	}

}
