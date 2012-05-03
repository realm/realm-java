package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableBase;

public class DateQueryColumn<Cursor, Query> extends AbstractColumn<Date, Cursor, Query> {

	public DateQueryColumn(TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(table, cursor, index, name);
	}

	public DateQueryColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

}
