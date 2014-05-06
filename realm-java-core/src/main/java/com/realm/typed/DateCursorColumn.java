package com.realm.typed;

import java.text.DateFormat;
import java.util.Date;

/**
 * Type of the fields that represent a date column in the generated XyzRow class
 * for the Xyz entity.
 */
public class DateCursorColumn<Cursor, View, Query> extends AbstractColumn<Date, Cursor, View, Query> {

    private static final DateFormat FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public DateCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
        super(types, cursor, index, name);
    }

    @Override
    public Date get() {
        return cursor.tableOrView.getDate(columnIndex, cursor.getPosition());
    }

    @Override
    public void set(Date value) {
        cursor.tableOrView.setDate(columnIndex, cursor.getPosition(), value);
    }

    @Override
    public String getReadableValue() {
        return FORMATTER.format(get());
    }

}
