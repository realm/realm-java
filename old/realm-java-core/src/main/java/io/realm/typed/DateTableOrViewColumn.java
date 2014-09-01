package io.realm.typed;

import java.util.Date;

import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 * Super-type of the fields that represent a date column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class DateTableOrViewColumn<Cursor, View, Query> extends DateQueryColumn<Cursor, View, Query> implements TableOrViewColumn<Date> {

    public DateTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index, String name) {
        this(types, tableOrView, null, index, name);
    }

    public DateTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    @Override
    public Date[] getAll() {
        long count = tableOrView.size();
        Date[] values = new Date[(int) count];
        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getDate(columnIndex, i);
        }
        return values;
    }

    @Override
    public void setAll(Date value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setDate(columnIndex, i, value);
        }
    }

    public Cursor findFirst(Date value) {
        return cursor(tableOrView.findFirstDate(columnIndex, value));
    }

    public View findAll(Date value) {
        return view(tableOrView.findAllDate(columnIndex, value));
    }

}
