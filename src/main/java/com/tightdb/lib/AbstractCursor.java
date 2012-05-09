package com.tightdb.lib;

public abstract class AbstractCursor<Cursor> {

	protected long position;
	protected final EntityTypes<?, ?, Cursor, ?> types;
	protected final IRowsetBase rowset;

	public AbstractCursor(EntityTypes<?, ?, Cursor, ?> types, IRowsetBase rowset, long position) {
		this.types = types;
		this.rowset = rowset;
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
		return types.getCursorClass().getSimpleName() + "[" + position + "]";
	}

	public AbstractColumn<?, ?, ?>[] columns() {
		return null;
	}

	protected AbstractColumn<?, ?, ?>[] getColumnsArray(AbstractColumn<?, ?, ?>... columns) {
		return columns;
	}

}
