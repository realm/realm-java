package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.TableViewBase;

public abstract class AbstractQuery<Cursor, View> {

	private final TableQuery query;
	private final TableBase table;
	private final EntityTypes<?, View, Cursor, ?> types;

	public AbstractQuery(EntityTypes<?, View, Cursor, ?> types, TableBase table, TableQuery query2) {
		this.types = types;
		this.table = table;
		query = new TableQuery(table);
	}

	public View findAll() {
		TableViewBase viewBase = query.findAll(table, 0, table.getCount(), Integer.MAX_VALUE);
		return view(viewBase);
	}

	public Cursor findFirst() {
		return null;
	}

	public Cursor findLast() {
		return null;
	}

	public Cursor findUnique() {
		return null;
	}

	// FIXME: we need other class
	public Cursor or() {
		return null;
	}

	public long remove() {
		return 0;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected View view(TableViewBase viewBase) {
		try {
			return types.getViewClass().getConstructor(TableViewBase.class).newInstance(viewBase);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}

}
