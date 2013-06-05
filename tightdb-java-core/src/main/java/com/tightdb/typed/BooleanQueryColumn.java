package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a boolean column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class BooleanQueryColumn<Cursor, View, Query> extends AbstractColumn<Boolean, Cursor, View, Query> {

    public BooleanQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equal(boolean value) {
        return query(getQuery().equal(columnIndex, value));
    }
    public Query eq(boolean value) {
        return query(getQuery().eq(columnIndex, value));
    }

    public Query notEqual(boolean value) {
        return query(getQuery().equal(columnIndex, !value));
    }
    public Query neq(boolean value) {
        return query(getQuery().eq(columnIndex, !value));
    }
}
