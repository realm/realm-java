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

/**
 * Type of the fields that represent a long column in the generated XyzRow class
 * for the Xyz entity.
 */
public class LongCursorColumn<Cursor, View, Query> extends AbstractColumn<Long, Cursor, View, Query> {

    public LongCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index,
            String name) {
        super(types, cursor, index, name);
    }

    @Override
    public Long get() {
        return cursor.tableOrView.getLong(columnIndex, cursor.getPosition());
    }

    @Override
    public void set(Long value) {
        cursor.tableOrView.setLong(columnIndex, cursor.getPosition(), value);
    }

    public void set(long value) {
        set(Long.valueOf(value));
    }

}
