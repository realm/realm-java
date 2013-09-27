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

    public Query equalTo(long value) {
        return query(getQuery().equalTo(columnIndex, value));
    }

    public Query notEqualTo(long value) {
        return query(getQuery().notEqualTo(columnIndex, value));
    }

    public Query greaterThan(long value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query greaterThanOrEqual(long value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query lessThan(long value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lessThanOrEqual(long value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query between(long from, long to) {
        return query(getQuery().between(columnIndex, from, to));
    }

    public double average() {
        return getQuery().average(columnIndex);
    }
    public double average(long start, long end, long limit) {
        return getQuery().average(columnIndex, start, end, limit);
    }

    public long sum() {
        return getQuery().sum(columnIndex);
    }
    public long sum(long start, long end, long limit) {
        return getQuery().sum(columnIndex, start, end, limit);
    }

    public long maximum() {
        return getQuery().maximum(columnIndex);
    }
    public long maximum(long start, long end, long limit) {
        return getQuery().maximum(columnIndex, start, end, limit);
    }

    public long minimum() {
        return getQuery().minimum(columnIndex);
    }
    public long minimum(long start, long end, long limit) {
        return getQuery().minimum(columnIndex, start, end, limit);
    }

}
