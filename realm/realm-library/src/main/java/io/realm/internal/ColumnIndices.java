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

import java.util.Map;

import io.realm.RealmModel;

/**
 * Utility class used to cache the mapping between object field names and their column indices.
 */
public class ColumnIndices {

    private final Map<Class<? extends RealmModel>, ColumnInfo> classes;

    public ColumnIndices(Map<Class<? extends RealmModel>, ColumnInfo> classes) {
        this.classes = classes;
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
}
