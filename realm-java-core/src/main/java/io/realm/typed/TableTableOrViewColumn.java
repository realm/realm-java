package io.realm.typed;

import java.lang.reflect.Array;

import io.realm.Table;
import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 * Super-type of the fields that represent a nested table column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class TableTableOrViewColumn<Cursor, View, Query, Subtable> extends TableQueryColumn<Cursor, View, Query, Subtable> {

    public TableTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index, String name, Class<Subtable> subtableClass) {
        this(types, tableOrView, null, index, name, subtableClass);
    }

    public TableTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name,
            Class<Subtable> subtableClass) {
        super(types, tableOrView, query, index, name, subtableClass);
    }

    @SuppressWarnings("unchecked")
    public Subtable[] getAll() {
        long count = tableOrView.size();
        Subtable[] values = (Subtable[]) Array.newInstance(subtableClass, (int) count);
        for (int i = 0; i < count; i++) {
            Table subtableBase = tableOrView.getSubtable(columnIndex, i);
            values[i] = AbstractSubtable.createSubtable(subtableClass, subtableBase);
        }
        return values;
    }

}
