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
		long count = rowset.size();
		Date[] values = new Date[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = rowset.getDate(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Date value) {
		long count = rowset.size();
		for (int i = 0; i < count; i++) {
			rowset.setDate(columnIndex, i, value);
		}
	}

}
