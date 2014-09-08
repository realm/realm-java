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
