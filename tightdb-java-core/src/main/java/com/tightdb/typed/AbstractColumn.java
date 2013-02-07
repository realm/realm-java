package com.tightdb.typed;

import com.tightdb.Table;
import com.tightdb.TableOrView;
import com.tightdb.TableQuery;
import com.tightdb.TableView;

public abstract class AbstractColumn<Type, Cursor, View, Query> {

	protected final EntityTypes<?, View, Cursor, Query> types;
	protected final AbstractCursor<Cursor> cursor;
	protected final String name;
	protected final int columnIndex;
	protected final TableQuery query;
	protected final TableOrView tableOrView;

	public AbstractColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		this(types, cursor.tableOrView, cursor, index, name);
	}

	public AbstractColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, AbstractCursor<Cursor> cursor, int index, String name) {
		this.types = types;
		this.tableOrView = tableOrView;
		this.query = null;
		this.cursor = cursor;
		this.columnIndex = index;
		this.name = name;
	}

	public AbstractColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
		this.types = types;
		this.tableOrView = tableOrView;
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

	protected Table tableOrNull() {
		if (tableOrView instanceof Table) {
			return (Table) tableOrView;
		} else {
			throw new IllegalStateException("Cannot construct a query from a view!");
		}
	}

	protected TableQuery getQuery() {
		Table table = tableOrNull();
		return query != null ? query : table.where();
	}

	protected Query query(TableQuery tableQuery) {
		try {
			return types.getQueryClass().getConstructor(Table.class, TableQuery.class).newInstance(tableOrNull(), tableQuery);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}

	protected Cursor cursor(long position) {
		return (position >= 0 && position < tableOrView.size()) ? AbstractCursor.createCursor(types.getCursorClass(), tableOrView, position) : null;
	}

	protected View view(TableView viewBase) {
		return AbstractView.createView(types.getViewClass(), viewBase);
	}

}
