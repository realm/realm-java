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
import io.realm.internal.util.Pair;


/**
 * Utility class used to cache the mapping between object field names and their column indices.
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
    // MultiKeyMap of <Class, String> -> ColumnInfo
    // Right now we maintain 3 copies. One public and 2 internal ones.
    private final Map<Pair<Class<? extends RealmModel>, String>, ColumnInfo> classesToColumnInfo;
    private final Map<Class<? extends RealmModel>, ColumnInfo> classes;
    private final Map<String, ColumnInfo> classesByName;

    private final boolean mutable;
    private long schemaVersion;

    /**
     * Create a mutable ColumnIndices initialized with the ColumnInfo objects in the passed map.
     *
     * @param schemaVersion the schema version
     * @param classesMap a map of table classes to their column info
     * @throws IllegalArgumentException if any of the ColumnInfo object is immutable.
     */
    public ColumnIndices(long schemaVersion, Map<Pair<Class<? extends RealmModel>, String>, ColumnInfo> classesMap) {
        this(schemaVersion, new HashMap<>(classesMap), true);
        for (Map.Entry<Pair<Class<? extends RealmModel>, String>, ColumnInfo> entry : classesMap.entrySet()) {
            ColumnInfo columnInfo = entry.getValue();
            if (mutable != columnInfo.isMutable()) {
                throw new IllegalArgumentException("ColumnInfo mutability does not match ColumnIndices");
            }
            Pair<Class<? extends RealmModel>, String> classDescription = entry.getKey();
            this.classes.put(classDescription.first, columnInfo);
            this.classesByName.put(classDescription.second, columnInfo);
        }
    }

    /**
     * Create a copy of the passed ColumnIndices with the specified mutability.
     *
     * @param other the ColumnIndices object to copy
     * @param mutable if false the object is effectively final.
     */
    public ColumnIndices(ColumnIndices other, boolean mutable) {
        this(other.schemaVersion, new HashMap<Pair<Class<? extends RealmModel>, String>, ColumnInfo>(other.classesToColumnInfo.size()), mutable);
        for (Map.Entry<Pair<Class<? extends RealmModel>, String>, ColumnInfo> entry : other.classesToColumnInfo.entrySet()) {
            ColumnInfo columnInfo = entry.getValue().copy(mutable);
            Pair<Class<? extends RealmModel>, String> key = entry.getKey();
            this.classes.put(key.first, columnInfo);
            this.classesByName.put(key.second, columnInfo);
            this.classesToColumnInfo.put(key, columnInfo);
        }
    }

    private ColumnIndices(long schemaVersion, Map<Pair<Class<? extends RealmModel>, String>, ColumnInfo> classesMap, boolean mutable) {
        this.schemaVersion = schemaVersion;
        this.classesToColumnInfo = classesMap;
        this.mutable = mutable;
        this.classes = new HashMap<>(classesMap.size());
        this.classesByName = new HashMap<>(classesMap.size());
    }

    /**
     * Get the schema version.
     *
     * @return the schema version.
     */
    public long getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Returns the {@link ColumnInfo} for the passed class or ({@code null} if there is no such class).
     *
     * @param clazz the class for which to get the ColumnInfo.
     * @return the corresponding {@link ColumnInfo} object, or {@code null} if not found.
     */
    public ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        return classes.get(clazz);
    }

    /**
     * Returns the {@link ColumnInfo} for the passed class ({@code null} if there is no such class).
     *
     * @param className the simple name of the class for which to get the ColumnInfo.
     * @return the corresponding {@link ColumnInfo} object, or {@code null} if not found.
     */
    public ColumnInfo getColumnInfo(String className) {
        return classesByName.get(className);
    }

    /**
     * Convenience method to return the column index for a given field on a class
     * or {@code -1} if no such field exists.
     *
     * @param clazz the class to search.
     * @param fieldName the name of the field whose index is needed.
     * @return the index in clazz of the field fieldName.
     * @deprecated Use {@code getColumnInfo().getColumnIndex()} instead.
     */
    @Deprecated
    public long getColumnIndex(Class<? extends RealmModel> clazz, String fieldName) {
        final ColumnInfo columnInfo = getColumnInfo(clazz);
        if (columnInfo == null) {
            return -1;
        }
        return columnInfo.getColumnIndex(fieldName);
    }

    /**
     * Make this instance contain a (non-strict) subset of the data in the passed ColumnIndices.
     * The schemaVersion and every ColumnInfo object held by this instance will be updated to be
     * the same the corresponding data in the passed instance or IllegalStateException will be thrown.
     * It is allowable for the passed ColumnIndices to contain information this instance does not.
     * <p>
     * NOTE: copying does not change this instance's mutablity state.
     *
     * @param src the instance to copy.
     * @throws UnsupportedOperationException if this instance is immutable.
     * @throws IllegalStateException if this object contains information for a table that the source does not.
     */
    public void copyFrom(ColumnIndices src) {
        if (!mutable) {
            throw new UnsupportedOperationException("Attempt to modify immutable cache");
        }
        for (Map.Entry<String, ColumnInfo> entry : classesByName.entrySet()) {
            final ColumnInfo otherColumnInfo = src.classesByName.get(entry.getKey());
            if (otherColumnInfo == null) {
                throw new IllegalStateException("Failed to copy ColumnIndices cache for class: " + entry.getKey());
            }
            entry.getValue().copyFrom(otherColumnInfo);
        }
        this.schemaVersion = src.schemaVersion;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("ColumnIndices[");
        buf.append(schemaVersion).append(",");
        buf.append(mutable).append(",");
        if (classes != null) {
            boolean commaNeeded = false;
            for (Map.Entry<String, ColumnInfo> entry : classesByName.entrySet()) {
                if (commaNeeded) { buf.append(","); }
                buf.append(entry.getKey()).append("->").append(entry.getValue());
                commaNeeded = true;
            }
        }
        return buf.append("]").toString();
    }
}
