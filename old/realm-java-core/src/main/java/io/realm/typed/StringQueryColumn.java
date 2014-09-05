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
 * Type of the fields that represent a string column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class StringQueryColumn<Cursor, View, Query> extends AbstractColumn<String, Cursor, View, Query> {

    public StringQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equalTo(String value) {
        return query(getQuery().equalTo(columnIndex, value));
    }
    public Query equalTo(String value, boolean caseSensitive) {
        return query(getQuery().equalTo(columnIndex, value, caseSensitive));
    }

    public Query notEqualTo(String value) {
        return query(getQuery().notEqualTo(columnIndex, value));
    }
    public Query notEqualTo(String value, boolean caseSensitive) {
        return query(getQuery().notEqualTo(columnIndex, value, caseSensitive));
    }

    public Query startsWith(String value) {
        return query(getQuery().beginsWith(columnIndex, value));
    }
    public Query startsWith(String value, boolean caseSensitive) {
        return query(getQuery().beginsWith(columnIndex, value, caseSensitive));
    }

    public Query endsWith(String value) {
        return query(getQuery().endsWith(columnIndex, value));
    }
    public Query endsWith(String value, boolean caseSensitive) {
        return query(getQuery().endsWith(columnIndex, value, caseSensitive));
    }

    public Query contains(String value) {
        return query(getQuery().contains(columnIndex, value));
    }
    public Query contains(String value, boolean caseSensitive) {
        return query(getQuery().contains(columnIndex, value, caseSensitive));
    }

}
