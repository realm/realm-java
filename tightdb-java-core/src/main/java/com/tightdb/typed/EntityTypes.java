package com.tightdb.typed;

/**
 * Holder of the generated XyzTable, XyzView, XyzRow and XyzQuery classes, used
 * to transfer them to the columns, so they can instantiate them as needed.
 */
public class EntityTypes<Tbl, View, Cursor, Query> {

	private final Class<Tbl> tableClass;
	private final Class<View> viewClass;
	private final Class<Cursor> cursorClass;
	private final Class<Query> queryClass;

	public EntityTypes(Class<Tbl> tableClass, Class<View> viewClass, Class<Cursor> cursorClass, Class<Query> queryClass) {
		this.tableClass = tableClass;
		this.viewClass = viewClass;
		this.cursorClass = cursorClass;
		this.queryClass = queryClass;
	}

	public Class<Tbl> getTableClass() {
		return tableClass;
	}

	public Class<View> getViewClass() {
		return viewClass;
	}

	public Class<Cursor> getCursorClass() {
		return cursorClass;
	}

	public Class<Query> getQueryClass() {
		return queryClass;
	}

}
