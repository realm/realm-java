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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import io.realm.RealmModel;
import io.realm.exceptions.RealmException;

/**
 * Utility class used to cache the mapping between object field names and their column indices. The
 * {@code ColumnIndices} instance is dedicated to a single {@link io.realm.BaseRealm} instance. Different Realm
 * instances will never share the same column indices cache. The column info cache is loaded lazily. A
 * {@link ColumnInfo} will be added to the cache when the relevant Realm object gets accessed.
 * <p>
 * This class can be mutated, after construction, in two ways:
 * <ul>
 * <li>the {@code copyFrom} method</li>
 * <li>mutating one of the ColumnInfo object to which this instance holds a reference</li>
 * </ul>
 * Immutable instances of this class protect against the first possibility by throwing on calls
 * to {@code copyFrom}.  {@see ColumnInfo} for its mutability contract.
 *
 * There are two, redundant, lookup methods, for schema members: by Class and by String.
 * Query lookups must be done by the name of the class and use the String-keyed lookup table.
 * Although it would be possible to use the same table to look up ColumnInfo by Class, the
 * class lookup is very fast and on a hot path, so we maintain the redundant table.
 */
public final class ColumnIndices {
    // Class to ColumnInfo map
    private final Map<Class<? extends RealmModel>, ColumnInfo> classToColumnInfoMap =
            new HashMap<Class<? extends RealmModel>, ColumnInfo>();
    // Class name to ColumnInfo map. All the elements in this map should be existing in classToColumnInfoMap.
    private final Map<String, ColumnInfo> simpleClassNameToColumnInfoMap =
            new HashMap<String, ColumnInfo>();

    private final RealmProxyMediator mediator;
    // Due to the nature of Object Store's Realm::m_schema, OsSharedRealm's OsObjectSchemaInfo object is fixed after set.
    private final OsSchemaInfo osSchemaInfo;


    /**
     * Create a mutable ColumnIndices initialized with the ColumnInfo objects in the passed map.
     *
     * @param mediator the {@link RealmProxyMediator} used for the corresponding Realm.
     * @param osSchemaInfo the corresponding Realm's {@link OsSchemaInfo}.
     */
    public ColumnIndices(RealmProxyMediator mediator, OsSchemaInfo osSchemaInfo) {
        this.mediator = mediator;
        this.osSchemaInfo = osSchemaInfo;
    }

    /**
     * Returns the {@link ColumnInfo} for the passed class.
     *
     * @param clazz the class for which to get the ColumnInfo.
     * @return the corresponding {@link ColumnInfo} object.
     * @throws io.realm.exceptions.RealmException if the class cannot be found in the schema.
     */
    @Nonnull
    public ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        ColumnInfo columnInfo = classToColumnInfoMap.get(clazz);
        if (columnInfo == null) {
            columnInfo = mediator.createColumnInfo(clazz, osSchemaInfo);
            classToColumnInfoMap.put(clazz, columnInfo);
        }
        return columnInfo;
    }

    /**
     * Returns the {@link ColumnInfo} for the provided internal class name.
     *
     * @param simpleClassName the simple name of the class for which to get the ColumnInfo.
     * @return the corresponding {@link ColumnInfo} object.
     * @throws io.realm.exceptions.RealmException if the class cannot be found in the schema.
     */
    @Nonnull
    public ColumnInfo getColumnInfo(String simpleClassName) {
        ColumnInfo columnInfo = simpleClassNameToColumnInfoMap.get(simpleClassName);
        if (columnInfo == null) {
            Set<Class<? extends RealmModel>> modelClasses = mediator.getModelClasses();
            for (Class<? extends RealmModel> modelClass : modelClasses) {
                if (mediator.getSimpleClassName(modelClass).equals(simpleClassName)) {
                    columnInfo = getColumnInfo(modelClass);
                    simpleClassNameToColumnInfoMap.put(simpleClassName, columnInfo);
                    break;
                }
            }
        }
        if (columnInfo == null) {
            throw new RealmException(
                    String.format(Locale.US, "'%s' doesn't exist in current schema.", simpleClassName));
        }
        return columnInfo;
    }

    /**
     * Refreshes all the existing {@link ColumnInfo} in the cache.
     */
    public void refresh() {
        for (Map.Entry<Class<? extends RealmModel>, ColumnInfo> entry : classToColumnInfoMap.entrySet()) {
            ColumnInfo newColumnInfo = mediator.createColumnInfo(entry.getKey(), osSchemaInfo);
            entry.getValue().copyFrom(newColumnInfo);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("ColumnIndices[");
        boolean commaNeeded = false;
        for (Map.Entry<Class<? extends RealmModel>, ColumnInfo> entry : classToColumnInfoMap.entrySet()) {
            if (commaNeeded) { buf.append(","); }
            buf.append(entry.getKey().getSimpleName()).append("->").append(entry.getValue());
            commaNeeded = true;
        }
        return buf.append("]").toString();
    }
}
