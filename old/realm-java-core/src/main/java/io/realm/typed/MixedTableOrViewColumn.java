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

import io.realm.Mixed;
import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 * Super-type of the fields that represent a mixed column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class MixedTableOrViewColumn<Cursor, View, Query> extends MixedQueryColumn<Cursor, View, Query> implements TableOrViewColumn<Mixed> {

    public MixedTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index, String name) {
        this(types, tableOrView, null, index, name);
    }

    public MixedTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    @Override
    public Mixed[] getAll() {
        long count = tableOrView.size();
        Mixed[] values = new Mixed[(int) count];
        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getMixed(columnIndex, i);
        }
        return values;
    }

    @Override
    public void setAll(Mixed value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setMixed(columnIndex, i, value);
        }
    }

}
