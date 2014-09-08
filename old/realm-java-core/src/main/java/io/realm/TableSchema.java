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

package io.realm;


public interface TableSchema {

    TableSchema getSubtableSchema(long columnIndex);

    long addColumn(ColumnType type, String name);

    void removeColumn(long columnIndex);

    void renameColumn(long columnIndex, String newName);

    /*
    // FIXME the column information classes should be here as well.
    // There is currently no path based implementation in core, so we should consider adding them with Spec, or wait for a core implementation.

    long getColumnCount();

    String getColumnName(long columnIndex);

    long getColumnIndex(String name);

    ColumnType getColumnType(long columnIndex);
    */
}
