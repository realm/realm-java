package com.tightdb.lib;

public abstract class AbstractCursor<Cursor> {

	protected final long position;
	protected final EntityTypes<?, ?, Cursor, ?> types;
	protected final IRowsetBase rowset;

	public AbstractCursor(EntityTypes<?, ?, Cursor, ?> types, IRowsetBase rowset, long position) {
		this.types = types;
		this.rowset = rowset;
		this.position = position;
	}

	public Cursor next() {
		return after(1);
	}

	public Cursor previous() {
		return before(1);
	}

	public Cursor before(long delta) {
		long pos = position - delta;
		if (isValidIndex(pos)) {
			return createCursor(types.getCursorClass(), rowset, pos);
		} else {
			return null;
		}
	}

	public Cursor after(long delta) {
		long pos = position + delta;
		if (isValidIndex(pos)) {
			return createCursor(types.getCursorClass(), rowset, pos);
		} else {
			return null;
		}
	}

	private boolean isValidIndex(long position) {
		return 0 <= position && position < rowset.size();
	}

	public long getPosition() {
		return position;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		AbstractColumn<?, ?, ?>[] columns = columns();

		for (int i = 0; i < columns.length; i++) {
			AbstractColumn<?, ?, ?> column = columns[i];
			sb.append(String.format("%s=%s", column.getName(), column.getReadableValue()));
			if (i < columns.length - 1) {
				sb.append(", ");
			}
		}

		return types.getCursorClass().getSimpleName() + " {" + sb + "}";
	}

	public AbstractColumn<?, ?, ?>[] columns() {
		return null;
	}

	protected AbstractColumn<?, ?, ?>[] getColumnsArray(AbstractColumn<?, ?, ?>... columns) {
		return columns;
	}

	protected static <C> C createCursor(Class<C> cursorClass, IRowsetBase targetRowset, long position) {
		try {
			return cursorClass.getDeclaredConstructor(IRowsetBase.class, long.class).newInstance(targetRowset, position);
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate a cursor!", e);
		}
	}

}
