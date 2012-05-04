package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public abstract class AbstractQuery<Cursor, View> {

	private final TableQuery query;
	private final TableBase table;

	public AbstractQuery(TableBase table, TableQuery query2) {
		this.table = table;
		query = new TableQuery(table);
	}

	public View findAll() {
		return null;
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
}
