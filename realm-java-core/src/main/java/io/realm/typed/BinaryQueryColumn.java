package io.realm.typed;

import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 * Type of the fields that represent a binary column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class BinaryQueryColumn<Cursor, View, Query> extends AbstractColumn<byte[], Cursor, View, Query> {

    public BinaryQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

}
