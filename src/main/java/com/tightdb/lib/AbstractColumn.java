package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public abstract class AbstractColumn<Type, Cursor, Query> {

	protected final TableBase table;
	protected final AbstractCursor<Cursor> cursor;
	protected final String name;
	protected final int columnIndex;
	private final EntityTypes<?, ?, Cursor, Query> types;

	public AbstractColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		this.types = types;
		this.table = table;
		this.cursor = cursor;
		this.columnIndex = index;
		this.name = name;
	}

	public AbstractColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		this.types = types;
		this.table = table;
		this.cursor = null;
		this.columnIndex = index;
		this.name = name;
	}

	protected Type get() {
		throw new UnsupportedOperationException("Cannot get the column's value!");
	}

	protected void set(Type value) {
		throw new UnsupportedOperationException("Cannot set the column's value!");
	}

	public Query is(Type value) {
		return query("is:" + value); // FIXME: remove from here
	}

	public Query isnt(Type value) {
		return query("isnt:" + value); // FIXME: remove from here
	}

	@Override
	public String toString() {
		return cursor + "." + name;
	}

	public String getName() {
		return name;
	}

	public String getReadable() {
		try {
			return String.valueOf(get());
		} catch (Exception e) {
			return "ERROR!";
		}
	}

	private Query query(String info) { // String info is temporary PoC
		try {
			info = getName() + ":" + info;
			return types.getQueryClass().getConstructor(String.class).newInstance(info);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}

}
