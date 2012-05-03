package com.tightdb.lib;

public abstract class AbstractSubtable<Cursor, View, Query> extends AbstractTable<Cursor, View, Query> {

	public AbstractSubtable(Class<Cursor> cursorClass, Class<View> viewClass, Class<Query> queryClass) {
		super(cursorClass, viewClass, queryClass);
	}

}
