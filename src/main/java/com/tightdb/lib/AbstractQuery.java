package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public abstract class AbstractQuery<Cursor, View> {

	private TableQuery query;

	public AbstractQuery(TableBase table) {
		query = new TableQuery(table);
	}

	public AbstractQuery() {
		// FIXME: not finished
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

}
