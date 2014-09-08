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
 * Type of the fields that represent a boolean column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class BooleanQueryColumn<Cursor, View, Query> extends AbstractColumn<Boolean, Cursor, View, Query> {

    public BooleanQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public Query equalTo(boolean value) {
        return query(getQuery().equalTo(columnIndex, value));
    }

    public Query notEqual(boolean value) {
        return query(getQuery().equalTo(columnIndex, !value));
    }

}
