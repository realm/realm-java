package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.TableViewBase;
import com.tightdb.util;

public abstract class AbstractQuery<Query, Cursor, View extends AbstractView<Cursor, View, ?>> {

	private long currPos = -1;

	private final TableQuery query;
	private final TableBase table;
	private final EntityTypes<?, View, Cursor, Query> types;

	public AbstractQuery(EntityTypes<?, View, Cursor, Query> types, TableBase table, TableQuery query) {
		this.types = types;
		this.table = table;
		this.query = query;
	}

	public long count() {
		return query.count(table);
	}
	
	public long count(long start, long end, long limit) {
		return query.count(table, start, end, limit);
	}
	
	public long remove(long start, long end, long limit) {
		return query.remove(table, start, end, limit);
	}

	public long remove() {
		return query.remove(table);
	}

	public View findAll() {
		TableViewBase viewBase = query.findAll(table);
		return view(viewBase);
	}

	public Cursor findNext() {
		if (currPos < Long.MAX_VALUE) {
			currPos = query.findNext(table, currPos);
			if (currPos >= 0 && currPos < table.size()) {
				return cursor(table, currPos);
			} else {
				currPos = Long.MAX_VALUE;
				return null;
			}
		} else {
			return null;
		}
	}

	public Cursor findFirst() {
		TableViewBase viewBase = query.findAll(table, 0, util.INFINITE, 1);
		if (viewBase.size() > 0) {
			return cursor(viewBase, 0);
		} else {
			return null;
		}
	}

	public Cursor findLast() {
		// TODO: find more efficient way to search
		TableViewBase viewBase = query.findAll(table);
		long count = viewBase.size();
		if (count > 0) {
			return cursor(viewBase, count - 1);
		} else {
			return null;
		}
	}

	/**
	 * public Cursor findUnique() { TableViewBase viewBase =
	 * query.findAll(table, 0, table.size(), 2); switch (viewBase.size()) { case
	 * 0: throw new
	 * IllegalStateException("Expected exactly one result, but found none!");
	 * case 1: return cursor(viewBase, 0); default: throw new
	 * IllegalStateException("Expected exactly one result, but found more!"); }
	 * }
	 */

	public Query or() {
		query.or();
		return newQuery(query);
	}

	public Query group() {
		query.group();
		return newQuery(query);
	}

	public Query endGroup() {
		query.endGroup();
		return newQuery(query);
	}

	private Query newQuery(TableQuery q) {
		return createQuery(types.getQueryClass(), table, q);
	}

	public long clear() {
		View results = findAll();		// FIXME: Too expensive clear.
		long count = results.size();
		results.clear();
		return count;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected View view(TableViewBase viewBase) {
		return AbstractView.createView(types.getViewClass(), viewBase);
	}

	protected Cursor cursor(IRowsetBase rowset, long position) {
		return AbstractCursor.createCursor(types.getCursorClass(), rowset, position);
	}

	protected static <Q> Q createQuery(Class<Q> queryClass, TableBase tableBase, TableQuery tableQuery) {
		try {
			return queryClass.getConstructor(TableBase.class, TableQuery.class).newInstance(tableBase, tableQuery);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}
}
