package com.realm.typed;

import com.realm.TableOrView;
import com.realm.TableQuery;

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
