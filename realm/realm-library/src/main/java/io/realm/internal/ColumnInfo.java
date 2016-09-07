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

import java.util.Map;

import io.realm.exceptions.RealmMigrationNeededException;

public abstract class ColumnInfo {
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

    public Map<String, Long> getIndicesMap() {
        return indicesMap;
    }

    protected final void setIndicesMap(Map<String, Long> indicesMap) {
        this.indicesMap = indicesMap;
    }

    /**
     * Copies the column index value from other {@link ColumnInfo} object.
     *
     * @param other The class of {@code other} must be exactly the the same with this instance.
     *              Of course it must not be {@code null}.
     * @throws IllegalArgumentException if {@code other} has different class than this.
     */
    public abstract void copyFrom(ColumnInfo other);

    /**
     * Creates a copy of this instance.
     *
     * @return a copy.
     */
    public abstract ColumnInfo copy();
}
