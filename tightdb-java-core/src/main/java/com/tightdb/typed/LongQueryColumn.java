package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a long column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class LongQueryColumn<Cursor, View, Query> extends
        AbstractColumn<Long, Cursor, View, Query> {

    public LongQueryColumn(EntityTypes<?, View, Cursor, Query> types,
            TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equal(long value) {
        return query(getQuery().equal(columnIndex, value));
    }

    public Query eq(long value) {
        return query(getQuery().eq(columnIndex, value));
    }

    public Query notEqual(long value) {
        return query(getQuery().notEqual(columnIndex, value));
    }

    public Query neq(long value) {
        return query(getQuery().neq(columnIndex, value));
    }

    public Query greaterThan(long value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query gt(long value) {
        return query(getQuery().gt(columnIndex, value));
    }

    public Query greaterThanOrEqual(long value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query gte(long value) {
        return query(getQuery().gte(columnIndex, value));
    }

    public Query lessThan(long value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lt(long value) {
        return query(getQuery().lt(columnIndex, value));
    }

    public Query lessThanOrEqual(long value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query lte(long value) {
        return query(getQuery().lte(columnIndex, value));
    }

    public Query between(long from, long to) {
        return query(getQuery().between(columnIndex, from, to));
    }

    public double average() {
        return getQuery().averageInt(columnIndex);
    }
    public double average(long start, long end, long limit) {
        return getQuery().averageInt(columnIndex, start, end, limit);
    }

    public long sum() {
        return getQuery().sumInt(columnIndex);
    }
    public long sum(long start, long end, long limit) {
        return getQuery().sumInt(columnIndex, start, end, limit);
    }

    public long maximum() {
        return getQuery().maximumInt(columnIndex);
    }
    public long maximum(long start, long end, long limit) {
        return getQuery().maximumInt(columnIndex, start, end, limit);
    }

    public long minimum() {
        return getQuery().minimumInt(columnIndex);
    }
    public long minimum(long start, long end, long limit) {
        return getQuery().minimumInt(columnIndex, start, end, limit);
    }

}
