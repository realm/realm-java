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
 * Super-type of the fields that represent a string column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class StringTableOrViewColumn<Cursor, View, Query> extends StringQueryColumn<Cursor, View, Query> implements
        TableOrViewColumn<String> {

    public StringTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index,
            String name) {
        this(types, tableOrView, null, index, name);
    }

    public StringTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView,
            TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    public String[] getAll() {
        long count = tableOrView.size();
        String[] values = new String[(int) count];
        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getString(columnIndex, i);
        }
        return values;
    }

    public void setAll(String value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setString(columnIndex, i, value);
        }
    }

    public Cursor findFirst(String value) {
        return cursor(tableOrView.findFirstString(columnIndex, value));
    }

    public View findAll(String value) {
        return view(tableOrView.findAllString(columnIndex, value));
    }

    // experimental:

    public long count(String value) {
        return tableOrView.count(columnIndex, value);
    }

   public long lookup(String value) {
        return tableOrView.lookup(value);
    }

}
