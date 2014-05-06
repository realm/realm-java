package com.realm.typed;

import com.realm.TableOrView;
import com.realm.TableQuery;

/**
 * Type of the fields that represent a double column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class DoubleQueryColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

    public DoubleQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query,
            int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equalTo(double value) {
        return query(getQuery().equalTo(columnIndex, value));
    }

    public Query notEqualTo(double value) {
        return query(getQuery().notEqualTo(columnIndex, value));
    }

    public Query greaterThan(double value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query greaterThanOrEqual(double value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query lessThan(double value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lessThanOrEqual(double value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query between(double from, double to) {
        return query(getQuery().between(columnIndex, from, to));
    }

    public double average() {
        return getQuery().averageDouble(columnIndex);
    }

    public double average(long start, long end, long limit) {
        return getQuery().averageDouble(columnIndex, start, end, limit);
    }

    public double sum() {
        return getQuery().sumDouble(columnIndex);
    }

    public double sum(long start, long end, long limit) {
        return getQuery().sumDouble(columnIndex, start, end, limit);
    }

    public double maximum() {
        return getQuery().maximumDouble(columnIndex);
    }

    public double maximum(long start, long end, long limit) {
        return getQuery().maximumDouble(columnIndex, start, end, limit);
    }

    public double minimum() {
        return getQuery().minimumDouble(columnIndex);
    }

    public double minimum(long start, long end, long limit) {
        return getQuery().minimumDouble(columnIndex, start, end, limit);
    }

}
