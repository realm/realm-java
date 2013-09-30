package com.tightdb.typed;

import java.util.Date;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a date column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class DateQueryColumn<Cursor, View, Query> extends AbstractColumn<Date, Cursor, View, Query> {

    public DateQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }


    public Query equalTo(Date value) {
        return query(getQuery().equalTo(columnIndex, value));
    }

    public Query notEqualTo(Date value) {
        return query(getQuery().notEqualTo(columnIndex, value));
    }

    public Query lessThan(Date value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lessThanOrEqual(Date value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query greaterThan(Date value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query greaterThanOrEqual(Date value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query between(Date from, Date to) {
        return query(getQuery().between(columnIndex, from, to));
    }

}
