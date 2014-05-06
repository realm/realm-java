package com.realm.typed;

import com.realm.TableOrView;
import com.realm.TableQuery;

/**
 * Type of the fields that represent a string column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class StringQueryColumn<Cursor, View, Query> extends AbstractColumn<String, Cursor, View, Query> {

    public StringQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equalTo(String value) {
        return query(getQuery().equalTo(columnIndex, value));
    }
    public Query equalTo(String value, boolean caseSensitive) {
        return query(getQuery().equalTo(columnIndex, value, caseSensitive));
    }

    public Query notEqualTo(String value) {
        return query(getQuery().notEqualTo(columnIndex, value));
    }
    public Query notEqualTo(String value, boolean caseSensitive) {
        return query(getQuery().notEqualTo(columnIndex, value, caseSensitive));
    }

    public Query startsWith(String value) {
        return query(getQuery().beginsWith(columnIndex, value));
    }
    public Query startsWith(String value, boolean caseSensitive) {
        return query(getQuery().beginsWith(columnIndex, value, caseSensitive));
    }

    public Query endsWith(String value) {
        return query(getQuery().endsWith(columnIndex, value));
    }
    public Query endsWith(String value, boolean caseSensitive) {
        return query(getQuery().endsWith(columnIndex, value, caseSensitive));
    }

    public Query contains(String value) {
        return query(getQuery().contains(columnIndex, value));
    }
    public Query contains(String value, boolean caseSensitive) {
        return query(getQuery().contains(columnIndex, value, caseSensitive));
    }

}
