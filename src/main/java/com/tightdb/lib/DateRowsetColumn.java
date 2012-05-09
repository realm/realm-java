package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableQuery;

public class DateRowsetColumn<Cursor, Query> extends DateQueryColumn<Cursor, Query> implements RowsetColumn<Date> {

	public DateRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name) {
		this(types, rowset, null, index, name);
	}

	public DateRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

	@Override
	public Date[] getAll() {
		int count = rowset.getCount();
		Date[] values = new Date[count];
		for (int i = 0; i < count; i++) {
			values[i] = new Date(rowset.getLong(columnIndex, i));
		}
		return values;
	}

	@Override
	public void setAll(Date value) {
		int count = rowset.getCount();
		for (int i = 0; i < count; i++) {
			rowset.setLong(columnIndex, i, value.getTime());
		}
	}

}
