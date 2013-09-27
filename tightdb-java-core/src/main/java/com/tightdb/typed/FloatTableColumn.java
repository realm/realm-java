package com.tightdb.typed;

import com.tightdb.TableOrView;
//import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a float column in the generated XyzTable
 * class for the Xyz entity.
 */
public class FloatTableColumn<Cursor, View, Query> extends FloatTableOrViewColumn<Cursor, View, Query> {

    public FloatTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
        super(types, table, index, name);
    }

    /*public FloatTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
            String name) {
        super(types, table, query, index, name);
    }*/

}
