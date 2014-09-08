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
//import TableQuery;

/**
 * Type of the fields that represent a nested table column in the generated XyzTable
 * class for the Xyz entity.
 */
public class TableTableColumn<Cursor, View, Query, Subtable> extends
        TableTableOrViewColumn<Cursor, View, Query, Subtable> {

    public TableTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name,
            Class<Subtable> subtableClass) {
        super(types, table, index, name, subtableClass);
    }

    /*public TableTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
            String name, Class<Subtable> subtableClass) {
        super(types, table, query, index, name, subtableClass);
    }*/

}
