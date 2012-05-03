package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableBase;

public class DateQueryColumn<Cursor, Query> extends AbstractColumn<Date, Cursor, Query> {

	public DateQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public DateQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		super(types, table, index, name);
	}

}
