package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableBase;

public class DateRowsetColumn<Cursor, Query> extends DateQueryColumn<Cursor, Query> implements RowsetColumn<Date> {

	public DateRowsetColumn(TableBase table, int index, String name) {
		super(table, index, name);
	}

	@Override
	public Date[] getAll() {
		return null; //new Date(table.getLong(columnIndex, (int) cursor.getPosition()));
	}

	@Override
	public void setAll(Date value) {
//		table.setLong(columnIndex, (int) cursor.getPosition(), value.getTime());
	}

}
