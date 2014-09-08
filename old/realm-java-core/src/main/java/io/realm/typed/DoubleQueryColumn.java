/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.typed;

import io.realm.TableOrView;
import io.realm.TableQuery;

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
