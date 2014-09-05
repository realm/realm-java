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

import java.util.Date;

import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 * Type of the fields that represent a date column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class DateQueryColumn<Cursor, View, Query> extends AbstractColumn<Date, Cursor, View, Query> {

    public DateQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }


    public Query equalTo(Date value) {
        return query(getQuery().equalTo(columnIndex, value));
    }

    public Query notEqualTo(Date value) {
        return query(getQuery().notEqualTo(columnIndex, value));
    }

    public Query lessThan(Date value) {
        return query(getQuery().lessThan(columnIndex, value));
    }

    public Query lessThanOrEqual(Date value) {
        return query(getQuery().lessThanOrEqual(columnIndex, value));
    }

    public Query greaterThan(Date value) {
        return query(getQuery().greaterThan(columnIndex, value));
    }

    public Query greaterThanOrEqual(Date value) {
        return query(getQuery().greaterThanOrEqual(columnIndex, value));
    }

    public Query between(Date from, Date to) {
        return query(getQuery().between(columnIndex, from, to));
    }

}
