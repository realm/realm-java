package com.tightdb.typed;

import com.tightdb.Mixed;
import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a mixed column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class MixedQueryColumn<Cursor, View, Query> extends AbstractColumn<Mixed, Cursor, View, Query> {

    public MixedQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

}
