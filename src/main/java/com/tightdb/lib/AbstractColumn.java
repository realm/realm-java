package com.tightdb.lib;

import java.util.List;

import com.tightdb.TableBase;

public abstract class AbstractColumn<Type, Cursor, Query> {

	protected final TableBase table;
	protected final AbstractCursor<Cursor> cursor;
	protected final String name;
	protected final int columnIndex;

	public AbstractColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		this.table = table;
		this.cursor = cursor;
		this.columnIndex = index;
		this.name = name;
	}

	public AbstractColumn(TableBase table, int index, String name) {
		this.table = table;
		this.cursor = null;
		this.columnIndex = index;
		this.name = name;
	}

	public abstract Type get();
	public abstract void set(Type value);

	public List<Type> all() {
		return null;
	}

	public Query is(Type value) {
		return null;
	}

	public Query isnt(Type value) {
		return null;
	}

	@Override
	public String toString() {
		return cursor + "." + name;
	}

}
