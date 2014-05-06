package com.realm.typed;

import com.realm.TableOrView;
//import com.realm.TableQuery;
import com.realm.TableView;
import com.realm.TableView.Order;

/**
 * Type of the fields that represent a long column in the generated XyzView
 * class for the Xyz entity.
 */
public class LongViewColumn<Cursor, View, Query> extends LongTableOrViewColumn<Cursor, View, Query> {

    public LongViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name) {
        super(types, view, index, name);
    }

    /*public LongViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
            String name) {
        super(types, view, query, index, name);
    }*/

    public void sort(Order order) {
        ( (TableView) this.tableOrView).sort(this.columnIndex, order);
    }

    public void sort() {
        ( (TableView) this.tableOrView).sort(this.columnIndex);
    }
}
