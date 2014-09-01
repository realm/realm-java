package io.realm.typed;

import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 * Type of the fields that represent a boolean column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class BooleanQueryColumn<Cursor, View, Query> extends AbstractColumn<Boolean, Cursor, View, Query> {

    public BooleanQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equalTo(boolean value) {
        return query(getQuery().equalTo(columnIndex, value));
    }

    public Query notEqual(boolean value) {
        return query(getQuery().equalTo(columnIndex, !value));
    }

}
