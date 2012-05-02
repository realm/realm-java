package com.tightdb.lib;

public abstract class AbstractSubtable<Cursor, View> extends AbstractTable<Cursor, View> {

	public AbstractSubtable(Class<Cursor> cursorClass, Class<View> viewClass) {
		super(cursorClass, viewClass);
	}

}
