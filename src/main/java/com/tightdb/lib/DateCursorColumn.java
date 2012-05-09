package com.tightdb.lib;

import java.text.DateFormat;
import java.util.Date;

public class DateCursorColumn<Cursor, Query> extends AbstractColumn<Date, Cursor, Query> {

	private static final DateFormat FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	public DateCursorColumn(EntityTypes<?, ?, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public Date get() {
		return new Date(cursor.rowset.getLong(columnIndex, (int) cursor.getPosition()));
	}

	@Override
	public void set(Date value) {
		cursor.rowset.setLong(columnIndex, (int) cursor.getPosition(), value.getTime());
	}

	@Override
	public String getReadable() {
		return FORMATTER.format(get());
	}

}
