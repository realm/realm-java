package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public abstract class AbstractColumn<Type, Cursor, Query> {

	private final EntityTypes<?, ?, Cursor, Query> types;
	protected final TableBase table;
	protected final AbstractCursor<Cursor> cursor;
	protected final String name;
	protected final int columnIndex;
	protected final TableQuery query;

	public AbstractColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		this.types = types;
		this.table = table;
		this.query = null;
		this.cursor = cursor;
		this.columnIndex = index;
		this.name = name;
	}

	public AbstractColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		this.types = types;
		this.table = table;
		this.query = query;
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

	protected TableQuery getQuery() {
		return query != null ? query : new TableQuery(table);
	}

	protected Query query(TableQuery tableQuery) { // String info is temporary
		try {
			return types.getQueryClass().getConstructor(TableBase.class, TableQuery.class).newInstance(table, tableQuery);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}

}
