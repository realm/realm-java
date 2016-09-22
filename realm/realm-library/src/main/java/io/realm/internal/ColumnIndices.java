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

package io.realm.internal;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmModel;

/**
 * Utility class used to cache the mapping between object field names and their column indices.
 */
public final class ColumnIndices implements Cloneable {
    private long schemaVersion;
    private Map<Class<? extends RealmModel>, ColumnInfo> classes;

    public ColumnIndices(long schemaVersion, Map<Class<? extends RealmModel>, ColumnInfo> classes) {
        this.schemaVersion = schemaVersion;
        this.classes = classes;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Returns {@link ColumnInfo} for the given class or {@code null} if no mapping exists.
     */
    public ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        return classes.get(clazz);
    }

    /**
     * Returns the column index for a given field on a clazz or {@code -1} if no such field exists.
     */
    public long getColumnIndex(Class<? extends RealmModel> clazz, String fieldName) {
        final ColumnInfo columnInfo = classes.get(clazz);
        if (columnInfo != null) {
            Long index = columnInfo.getIndicesMap().get(fieldName);
            return (index != null) ? index : -1;
        } else {
            return -1;
        }
    }

    @Override
    public ColumnIndices clone() {
        try {
            final ColumnIndices clone = (ColumnIndices) super.clone();
            clone.classes = duplicateColumnInfoMap();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Class<? extends RealmModel>, ColumnInfo> duplicateColumnInfoMap() {
        final Map<Class<? extends RealmModel>, ColumnInfo> copy = new HashMap<>();
        for (Map.Entry<Class<? extends RealmModel>, ColumnInfo> entry : classes.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().clone());
        }
        return copy;
    }

    public void copyFrom(ColumnIndices other, RealmProxyMediator mediator) {
        for (Map.Entry<Class<? extends RealmModel>, ColumnInfo> entry : classes.entrySet()) {
            final ColumnInfo otherColumnInfo = other.getColumnInfo(entry.getKey());
            if (otherColumnInfo == null) {
                throw new IllegalStateException("Failed to copy ColumnIndices cache: "
                        + Table.tableNameToClassName(mediator.getTableName(entry.getKey())));
            }
            entry.getValue().copyColumnInfoFrom(otherColumnInfo);
        }
        this.schemaVersion = other.schemaVersion;
    }
}
