package com.realm.typed;

import com.realm.TableOrView;
//import com.realm.TableQuery;

/**
 * Type of the fields that represent a date column in the generated XyzTable
 * class for the Xyz entity.
 */
public class DateTableColumn<Cursor, View, Query> extends DateTableOrViewColumn<Cursor, View, Query> {

    public DateTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
        super(types, table, index, name);
    }

    /*public DateTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
            String name) {
        super(types, table, query, index, name);
    }*/

}
