package com.tightdb.lib;

import java.text.DateFormat;
import java.util.Date;

public class DateCursorColumn<Cursor, View, Query> extends AbstractColumn<Date, Cursor, View, Query> {

	private static final DateFormat FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	public DateCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public Date get() {
		return cursor.rowset.getDate(columnIndex, cursor.getPosition());
	}

	@Override
	public void set(Date value) {
		cursor.rowset.setDate(columnIndex, cursor.getPosition(), value);
	}

	@Override
	public String getReadableValue() {
		return FORMATTER.format(get());
	}

}
