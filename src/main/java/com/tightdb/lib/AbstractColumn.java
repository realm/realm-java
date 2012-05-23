package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public abstract class AbstractColumn<Type, Cursor, Query> {

	protected final EntityTypes<?, ?, Cursor, Query> types;
	protected final AbstractCursor<Cursor> cursor;
	protected final String name;
	protected final int columnIndex;
	protected final TableQuery query;
	protected final IRowsetBase rowset;

	public AbstractColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		this(types, cursor.rowset, cursor, index, name);
	}

	public AbstractColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, AbstractCursor<Cursor> cursor, int index, String name) {
		this.types = types;
		this.rowset = rowset;
		this.query = null;
		this.cursor = cursor;
		this.columnIndex = index;
		this.name = name;
	}

	public AbstractColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		this.types = types;
		this.rowset = rowset;
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
		return types.getTableClass().getSimpleName() + "." + name;
	}

	public String getName() {
		return name;
	}

	public String getReadableValue() {
		try {
			return String.valueOf(get());
		} catch (Exception e) {
			return "ERROR!";
		}
	}

	private TableBase tableOrNull() {
		if (rowset instanceof TableBase) {
			return (TableBase) rowset;
		} else {
			throw new IllegalStateException("Cannot construct a query from a view!");
		}
	}

	protected TableQuery getQuery() {
		return query != null ? query : new TableQuery();
	}

	protected Query query(TableQuery tableQuery) {
		try {
			return types.getQueryClass().getConstructor(TableBase.class, TableQuery.class).newInstance(tableOrNull(), tableQuery);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}

	protected Cursor cursor(long position) {
		return position >= 0 ? AbstractCursor.createCursor(types.getCursorClass(), rowset, position) : null;
	}

}
