package io.realm.typed;

import io.realm.TableOrView;
//import TableQuery;
import io.realm.TableView;

/**
 * Type of the fields that represent a string column in the generated XyzView
 * class for the Xyz entity.
 */
public class StringViewColumn<Cursor, View, Query> extends StringTableOrViewColumn<Cursor, View, Query> {

    protected final TableView view;

    public StringViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name) {
        super(types, view, index, name);
        this.view = (TableView)view;
    }

    /*public StringViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
            String name) {
        super(types, view, query, index, name);
        this.view = (TableView)view;
    }*/
}
