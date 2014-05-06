package com.realm.typed;

import com.realm.TableOrView;
import com.realm.TableQuery;

/**
 * Type of the fields that represent a binary column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class BinaryQueryColumn<Cursor, View, Query> extends AbstractColumn<byte[], Cursor, View, Query> {

    public BinaryQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

}
