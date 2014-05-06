package com.realm.typed;

import com.realm.Mixed;
import com.realm.TableOrView;
import com.realm.TableQuery;

/**
 * Type of the fields that represent a mixed column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class MixedQueryColumn<Cursor, View, Query> extends AbstractColumn<Mixed, Cursor, View, Query> {

    public MixedQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

}
