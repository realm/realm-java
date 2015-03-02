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
import java.util.Set;

import io.realm.RealmObject;

/**
 * Utility class used to cache the mapping between object field names and their column indices.
 */
public class ColumnIndicies {

    private Map<Class<? extends RealmObject>, Map<String, Long>> classes = new HashMap<Class<? extends RealmObject>, Map<String, Long>>();

    /**
     * Add mapping for a given field.
     *
     * @param clazz         RealmObject containing the field.
     * @param fieldName     Field name in the object.
     * @param columnIndex   Column position in the table.
     */
    public void add(Class<? extends RealmObject> clazz, String fieldName, long columnIndex) {
        Map<String, Long> classMap = classes.get(clazz);
        if (classMap == null) {
            classMap = new HashMap<String, Long>();
            classes.put(clazz, classMap);
        }

        classMap.put(fieldName, columnIndex);
    }

    /**
     * Return mappings for the given class or null if no mapping exists.
     */
    public Map<String, Long> getClassFields(Class<? extends RealmObject> clazz) {
        return classes.get(clazz);
    }

    /**
     * Returns the column index for a given field on a clazz or -1 if no such field exists.
     */
    public long getColumnIndex(Class<? extends RealmObject> clazz, String fieldName) {
        Map<String, Long> mapping = classes.get(clazz);
        if (mapping != null) {
            Long index = mapping.get(fieldName);
            return (index != null) ? index : -1;
        } else {
            return -1;
        }
    }
}
