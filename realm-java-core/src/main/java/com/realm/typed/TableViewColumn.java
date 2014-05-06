package com.realm.typed;

import com.realm.TableOrView;
//import com.realm.TableQuery;

/**
 * Type of the fields that represent a nested table column in the generated XyzView
 * class for the Xyz entity.
 */
public class TableViewColumn<Cursor, View, Query, Subtable> extends
        TableTableOrViewColumn<Cursor, View, Query, Subtable> {

    public TableViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name,
            Class<Subtable> subtableClass) {
        super(types, view, index, name, subtableClass);
    }

    /*public TableViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
            String name, Class<Subtable> subtableClass) {
        super(types, view, query, index, name, subtableClass);
    }*/

}
