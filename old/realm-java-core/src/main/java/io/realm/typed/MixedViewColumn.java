package io.realm.typed;

import io.realm.TableOrView;
//import TableQuery;

/**
 * Type of the fields that represent a mixed column in the generated XyzView
 * class for the Xyz entity.
 */
public class MixedViewColumn<Cursor, View, Query> extends MixedTableOrViewColumn<Cursor, View, Query> {

    public MixedViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name) {
        super(types, view, index, name);
    }

    /*public MixedViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
            String name) {
        super(types, view, query, index, name);
    }*/

}
