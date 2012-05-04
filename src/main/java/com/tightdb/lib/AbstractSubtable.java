package com.tightdb.lib;

import com.tightdb.SubTableBase;

public abstract class AbstractSubtable<Cursor, View, Query> extends AbstractTable<Cursor, View, Query> {

	protected final SubTableBase subtable;

	public AbstractSubtable(Class<Cursor> cursorClass, Class<View> viewClass, Class<Query> queryClass, SubTableBase subtable) {
		super(cursorClass, viewClass, queryClass);
		this.subtable = subtable;
	}

}
