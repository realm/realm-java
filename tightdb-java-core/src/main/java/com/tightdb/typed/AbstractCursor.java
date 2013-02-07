package com.tightdb.typed;

import com.tightdb.TableOrView;

public abstract class AbstractCursor<Cursor> {

	protected final long position;
	protected final EntityTypes<?, ?, Cursor, ?> types;
	protected final TableOrView tableOrView;

	public AbstractCursor(EntityTypes<?, ?, Cursor, ?> types,
			TableOrView tableOrView, long position) {
		this.types = types;
		this.tableOrView = tableOrView;
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
			return createCursor(types.getCursorClass(), tableOrView, pos);
		} else {
			return null;
		}
	}

	public Cursor after(long delta) {
		long pos = position + delta;
		if (isValidIndex(pos)) {
			return createCursor(types.getCursorClass(), tableOrView, pos);
		} else {
			return null;
		}
	}

	private boolean isValidIndex(long position) {
		return 0 <= position && position < tableOrView.size();
	}

	public long getPosition() {
		return position;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		AbstractColumn<?, ?, ?, ?>[] columns = columns();

		for (int i = 0; i < columns.length; i++) {
			AbstractColumn<?, ?, ?, ?> column = columns[i];
			sb.append(String.format("%s=%s", column.getName(),
					column.getReadableValue()));
			if (i < columns.length - 1) {
				sb.append(", ");
			}
		}

		return types.getCursorClass().getSimpleName() + " {" + sb + "}";
	}

	public AbstractColumn<?, ?, ?, ?>[] columns() {
		return null;
	}

	protected AbstractColumn<?, ?, ?, ?>[] getColumnsArray(
			AbstractColumn<?, ?, ?, ?>... columns) {
		return columns;
	}

	protected static <C> C createCursor(Class<C> cursorClass,
			TableOrView targetTableOrView, long position) {
		try {
			return cursorClass.getDeclaredConstructor(TableOrView.class,
					long.class).newInstance(targetTableOrView, position);
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate a cursor!", e);
		}
	}

}
