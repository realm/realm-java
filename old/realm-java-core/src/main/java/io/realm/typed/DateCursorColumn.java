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

import java.text.DateFormat;
import java.util.Date;

/**
 * Type of the fields that represent a date column in the generated XyzRow class
 * for the Xyz entity.
 */
public class DateCursorColumn<Cursor, View, Query> extends AbstractColumn<Date, Cursor, View, Query> {

    private static final DateFormat FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public DateCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
        super(types, cursor, index, name);
    }

    @Override
    public Date get() {
        return cursor.tableOrView.getDate(columnIndex, cursor.getPosition());
    }

    @Override
    public void set(Date value) {
        cursor.tableOrView.setDate(columnIndex, cursor.getPosition(), value);
    }

    @Override
    public String getReadableValue() {
        return FORMATTER.format(get());
    }

}
