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
import io.realm.TableView;

/**
 * Type of the fields that represent a string column in the generated XyzView
 * class for the Xyz entity.
 */
public class StringViewColumn<Cursor, View, Query> extends StringTableOrViewColumn<Cursor, View, Query> {

    protected final TableView view;

    public StringViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, int index, String name) {
        super(types, view, index, name);
        this.view = (TableView)view;
    }

    /*public StringViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView view, TableQuery query, int index,
            String name) {
        super(types, view, query, index, name);
        this.view = (TableView)view;
    }*/
}
