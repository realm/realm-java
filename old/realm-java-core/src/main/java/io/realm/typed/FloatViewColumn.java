package io.realm.typed;

import io.realm.TableOrView;
//import TableQuery;

/**
 * Type of the fields that represent a float column in the generated XyzView
 * class for the Xyz entity.
 */
public class FloatViewColumn<Cursor, View, Query> extends FloatTableOrViewColumn<Cursor, View, Query> {

    public FloatViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name) {
        super(types, view, index, name);
    }

  /*  public FloatViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
            String name) {
        super(types, view, query, index, name);
    }*/
}
