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


    public Query equal(Date value) {
        return query(getQuery().equal(columnIndex, value));
    }

    public Query eq(Date value) {
        return query(getQuery().eq(columnIndex, value));
    }

    public Query notEqual(Date value) {
        return query(getQuery().notEqual(columnIndex, value));
    }

    public Query neq(Date value) {
        return query(getQuery().neq(columnIndex, value));
    }

    public Query lessThan(Date value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lt(Date value) {
        return query(getQuery().lt(columnIndex, value));
    }

    public Query lessThanOrEqual(Date value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query lte(Date value) {
        return query(getQuery().lte(columnIndex, value));
    }

    public Query greaterThan(Date value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query gt(Date value) {
        return query(getQuery().gt(columnIndex, value));
    }

    public Query greaterThanOrEqual(Date value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query gte(Date value) {
        return query(getQuery().gte(columnIndex, value));
    }

    public Query between(Date from, Date to) {
        return query(getQuery().between(columnIndex, from, to));
    }

}
