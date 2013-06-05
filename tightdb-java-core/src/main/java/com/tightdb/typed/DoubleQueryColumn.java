package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a double column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class DoubleQueryColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

    public DoubleQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query,
            int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equal(double value) {
        return query(getQuery().equal(columnIndex, value));
    }

    public Query eq(double value) {
        return query(getQuery().eq(columnIndex, value));
    }

    public Query notEqual(double value) {
        return query(getQuery().notEqual(columnIndex, value));
    }

    public Query neq(double value) {
        return query(getQuery().neq(columnIndex, value));
    }

    public Query greaterThan(double value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query gt(double value) {
        return query(getQuery().gt(columnIndex, value));
    }

    public Query greaterThanOrEqual(double value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query gte(double value) {
        return query(getQuery().gte(columnIndex, value));
    }

    public Query lessThan(double value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lt(double value) {
        return query(getQuery().lt(columnIndex, value));
    }

    public Query lessThanOrEqual(double value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query lte(double value) {
        return query(getQuery().lte(columnIndex, value));
    }

    public Query between(double from, double to) {
        return query(getQuery().between(columnIndex, from, to));
    }

    public double average() {
        return getQuery().averageDouble(columnIndex);
    }

    public double average(long start, long end) {
        return getQuery().averageDouble(columnIndex, start, end);
    }

    public double sum() {
        return getQuery().sumDouble(columnIndex);
    }

    public double sum(long start, long end) {
        return getQuery().sumDouble(columnIndex, start, end);
    }

    public double maximum() {
        return getQuery().maximumDouble(columnIndex);
    }

    public double maximum(long start, long end) {
        return getQuery().maximumDouble(columnIndex, start, end);
    }

    public double minimum() {
        return getQuery().minimumDouble(columnIndex);
    }

    public double minimum(long start, long end) {
        return getQuery().minimumDouble(columnIndex, start, end);
    }

}
