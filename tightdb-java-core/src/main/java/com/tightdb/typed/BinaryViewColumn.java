package com.tightdb.typed;

import com.tightdb.TableOrView;
//import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a binary column in the generated XyzView
 * class for the Xyz entity.
 */
public class BinaryViewColumn<Cursor, View, Query> extends BinaryTableOrViewColumn<Cursor, View, Query> {

    public BinaryViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name) {
        super(types, view, index, name);
    }

   /* public BinaryViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
            String name) {
        super(types, view, query, index, name);
    }*/

}
