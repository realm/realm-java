package com.tightdb.lib;

import java.util.Date;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class DateRowsetColumn<Cursor, Query> extends DateQueryColumn<Cursor, Query> implements RowsetColumn<Date> {

	public DateRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

	@Override
	public Date[] getAll() {
		int count = table.getCount();
		Date[] values = new Date[count];
		for (int i = 0; i < count; i++) {
			values[i] = new Date(table.getLong(columnIndex, i));
		}
		return values;
	}

	@Override
	public void setAll(Date value) {
		int count = table.getCount();
		for (int i = 0; i < count; i++) {
			table.setLong(columnIndex, i, value.getTime());
		}
	}

}
