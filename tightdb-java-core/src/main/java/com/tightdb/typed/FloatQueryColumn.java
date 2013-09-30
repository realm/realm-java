package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a float column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class FloatQueryColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

    public FloatQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query,
            int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equal(float value) {
        return query(getQuery().equal(columnIndex, value));
    }

    public Query eq(float value) {
        return query(getQuery().eq(columnIndex, value));
    }

    public Query notEqual(float value) {
        return query(getQuery().notEqual(columnIndex, value));
    }

    public Query neq(float value) {
        return query(getQuery().neq(columnIndex, value));
    }

    public Query greaterThan(float value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query gt(float value) {
        return query(getQuery().gt(columnIndex, value));
    }

    public Query greaterThanOrEqual(float value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query gte(float value) {
        return query(getQuery().gte(columnIndex, value));
    }

    public Query lessThan(float value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lt(float value) {
        return query(getQuery().lt(columnIndex, value));
    }

    public Query lessThanOrEqual(float value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query lte(float value) {
        return query(getQuery().lte(columnIndex, value));
    }

    public Query between(float from, float to) {
        return query(getQuery().between(columnIndex, from, to));
    }

    public double average() {
        return getQuery().averageFloat(columnIndex);
    }

    public double average(long start, long end, long limit) {
        return getQuery().averageFloat(columnIndex, start, end, limit);
    }

    public double sum() {
        return getQuery().sumFloat(columnIndex);
    }

    public double sum(long start, long end, long limit) {
        return getQuery().sumFloat(columnIndex, start, end, limit);
    }

    public float maximum() {
        return getQuery().maximumFloat(columnIndex);
    }

    public float maximum(long start, long end, long limit) {
        return getQuery().maximumFloat(columnIndex, start, end, limit);
    }

    public float minimum() {
        return getQuery().minimumFloat(columnIndex);
    }

    public float minimum(long start, long end, long limit) {
        return getQuery().minimumFloat(columnIndex, start, end, limit);
    }

}
