package com.tightdb.typed;

import com.tightdb.TableOrView;
//import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a boolean column in the generated XyzTable
 * class for the Xyz entity.
 */
public class BooleanTableColumn<Cursor, View, Query> extends BooleanTableOrViewColumn<Cursor, View, Query> {

    public BooleanTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
        super(types, table, index, name);
    }

  /*  public BooleanTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
            String name) {
        super(types, table, query, index, name);
    }*/

}
