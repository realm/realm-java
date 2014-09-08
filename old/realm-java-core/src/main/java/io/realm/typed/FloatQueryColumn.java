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
 * Type of the fields that represent a float column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class FloatQueryColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

    public FloatQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query,
            int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equalTo(float value) {
        return query(getQuery().equalTo(columnIndex, value));
    }

    public Query notEqualTo(float value) {
        return query(getQuery().notEqualTo(columnIndex, value));
    }

    public Query greaterThan(float value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query greaterThanOrEqual(float value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query lessThan(float value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lessThanOrEqual(float value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
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
