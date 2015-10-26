/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal;

import java.util.Collections;
import java.util.Map;

import io.realm.exceptions.RealmMigrationNeededException;

public class ColumnInfo {
    private Map<String, Long> indicesMap;

    protected final long getValidColumnIndex(String realmPath, Table table,
                                             String className, String columnName) {
        final long columnIndex = table.getColumnIndex(columnName);
        if (columnIndex == -1) {
            throw new RealmMigrationNeededException(realmPath,
                    "Field '" + columnName + "' not found for type " + className);
        }
        return columnIndex;
    }

    protected final void setIndicesMap(Map<String, Long> indicesMap) {
        this.indicesMap = Collections.unmodifiableMap(indicesMap);
    }

    public Map<String, Long> getIndicesMap() {
        return indicesMap;
    }
}
