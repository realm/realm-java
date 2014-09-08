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
 * Holder of the generated XyzTable, XyzView, XyzRow and XyzQuery classes, used
 * to transfer them to the columns, so they can instantiate them as needed.
 */
public class EntityTypes<Tbl, View, Cursor, Query> {

    private final Class<Tbl> tableClass;
    private final Class<View> viewClass;
    private final Class<Cursor> cursorClass;
    private final Class<Query> queryClass;

    public EntityTypes(Class<Tbl> tableClass, Class<View> viewClass, Class<Cursor> cursorClass, Class<Query> queryClass) {
        this.tableClass = tableClass;
        this.viewClass = viewClass;
        this.cursorClass = cursorClass;
        this.queryClass = queryClass;
    }

    public Class<Tbl> getTableClass() {
        return tableClass;
    }

    public Class<View> getViewClass() {
        return viewClass;
    }

    public Class<Cursor> getCursorClass() {
        return cursorClass;
    }

    public Class<Query> getQueryClass() {
        return queryClass;
    }

}
