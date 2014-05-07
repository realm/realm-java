package io.realm.typed;

import io.realm.TableOrView;
//import TableQuery;

/**
 * Type of the fields that represent a long column in the generated XyzTable
 * class for the Xyz entity.
 */
public class LongTableColumn<Cursor, View, Query> extends LongTableOrViewColumn<Cursor, View, Query> {

    public LongTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
        super(types, table, index, name);
    }

    /*public LongTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
            String name) {
        super(types, table, query, index, name);
    }*/

}
