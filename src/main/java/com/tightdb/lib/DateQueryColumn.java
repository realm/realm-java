package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class DateQueryColumn<Cursor, Query> extends AbstractColumn<Date, Cursor, Query> {

	public DateQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public DateQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

}
