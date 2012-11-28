package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableOrViewBase;
import com.tightdb.TableQuery;
import com.tightdb.TableViewBase;
import com.tightdb.internal.util;

public abstract class AbstractQuery<Query, Cursor, View extends AbstractView<Cursor, View, ?>> {

	private Long currPos = null;

	private final TableQuery query;
	private final TableBase table; // TODO ??? needed?
	private final EntityTypes<?, View, Cursor, Query> types;

	public AbstractQuery(EntityTypes<?, View, Cursor, Query> types, TableBase table, TableQuery query) {
		this.types = types;
		this.table = table;
		this.query = query;
	}

	public long count() {
		return query.count();
	}
	
	public long count(long start, long end) {
		return query.count(start, end);
	}
	
	public long remove(long start, long end) {
		return query.remove(start, end);
	}

	public long remove() {
		return query.remove();
	}

	public View findAll() {
		return view(query.findAll());
	}

	public Cursor findNext() {
		// TODO: needed?
		if (currPos == null) {
			// first time
			currPos = query.findNext();
		} else {
			// next times
			if (currPos < Long.MAX_VALUE) {
				currPos = query.findNext(currPos);
			} else {
				return null;
			}
		}

		// if there is a result - return it, or return null otherwise
		if (currPos >= 0 && currPos < table.size()) {
			return cursor(table, currPos);
		} else {
			currPos = Long.MAX_VALUE;
			return null;
		}
	}

	public Cursor findFirst() {
		// TODO: needed
		TableViewBase viewBase = query.findAll(0, util.INFINITE, 1);
		if (viewBase.size() > 0) {
			return cursor(viewBase, 0);
		} else {
			return null;
		}
	}

	public Cursor findLast() {
		// TODO: find more efficient way to search
		TableViewBase viewBase = query.findAll();
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

	// FIXME: columnIndex is low-level
	public Query subTable(long columnIndex) {
		query.subTable(columnIndex);
		return newQuery(query);
	}

	public Query endSubTable() {
		query.endSubTable();
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

	protected Cursor cursor(TableOrViewBase tableOrView, long position) {
		return AbstractCursor.createCursor(types.getCursorClass(), tableOrView, position);
	}

	protected static <Q> Q createQuery(Class<Q> queryClass, TableBase tableBase, TableQuery tableQuery) {
		try {
			return queryClass.getConstructor(TableBase.class, TableQuery.class).newInstance(tableBase, tableQuery);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}
}
