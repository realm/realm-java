package com.tightdb.lib;

import com.tightdb.TableBase;

public abstract class AbstractCursor<Cursor> {

	protected final TableBase table;
	protected final Class<Cursor> cursorClass;
	protected long position;

	public AbstractCursor(TableBase table, Class<Cursor> cursorClass, long position) {
		this.table = table;
		this.cursorClass = cursorClass;
		this.position = position;
	}

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

	public long getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return cursorClass.getSimpleName() + "[" + position + "]";
	}

}
