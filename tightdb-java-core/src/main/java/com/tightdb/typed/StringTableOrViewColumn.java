package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Super-type of the fields that represent a string column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class StringTableOrViewColumn<Cursor, View, Query> extends StringQueryColumn<Cursor, View, Query> implements
        TableOrViewColumn<String> {

    public StringTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index,
            String name) {
        this(types, tableOrView, null, index, name);
    }

    public StringTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView,
            TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public String[] getAll() {
        long count = tableOrView.size();
        String[] values = new String[(int) count];
        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getString(columnIndex, i);
        }
        return values;
    }

    public void setAll(String value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setString(columnIndex, i, value);
        }
    }

    public Cursor findFirst(String value) {
        return cursor(tableOrView.findFirstString(columnIndex, value));
    }

    public View findAll(String value) {
        return view(tableOrView.findAllString(columnIndex, value));
    }

    // experimental:

    public long count(String value) {
        return tableOrView.count(columnIndex, value);
    }

   public long lookup(String value) {
        return tableOrView.lookup(value);
    }

}
