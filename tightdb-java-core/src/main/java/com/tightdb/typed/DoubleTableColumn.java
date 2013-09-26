package com.tightdb.typed;

import com.tightdb.TableOrView;
//import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a double column in the generated XyzTable
 * class for the Xyz entity.
 */
public class DoubleTableColumn<Cursor, View, Query> extends DoubleTableOrViewColumn<Cursor, View, Query> {

    public DoubleTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
        super(types, table, index, name);
    }

    /*public DoubleTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
            String name) {
        super(types, table, query, index, name);
    }*/

}
