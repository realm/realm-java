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

package io.realm;

import java.util.Map;
import java.util.Set;

import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnInfo;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;


/**
 * Class for interacting with the Realm schema using a dynamic API. This makes it possible
 * to add, delete and change the classes in the Realm.
 * <p>
 * All changes must happen inside a write transaction for the particular Realm.
 *
 * @see RealmMigration
 */
public abstract class RealmSchema {
    private ColumnIndices columnIndices; // Cached field look up

    public abstract void close();

    /**
     * Returns the Realm schema for a given class.
     *
     * @param className name of the class
     * @return schema object for that class or {@code null} if the class doesn't exists.
     */
    public abstract RealmObjectSchema get(String className);

    /**
     * Returns the {@link RealmObjectSchema} for all RealmObject classes that can be saved in this Realm.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    public abstract Set<RealmObjectSchema> getAll();

    /**
     * Adds a new class to the Realm.
     *
     * @param className name of the class.
     * @return a Realm schema object for that class.
     */
    public abstract RealmObjectSchema create(String className);

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    public abstract boolean contains(String className);

    final void setColumnIndices(ColumnIndices columnIndices) {
        this.columnIndices = columnIndices.clone();
    }

    final void setColumnIndices(long version, Map<Class<? extends RealmModel>, ColumnInfo> columnInfoMap) {
        columnIndices = new ColumnIndices(version, columnInfoMap);
    }

    void setColumnIndices(ColumnIndices cacheForCurrentVersion, RealmProxyMediator mediator) {
        columnIndices.copyFrom(cacheForCurrentVersion, mediator);
    }

    final ColumnIndices getColumnIndices() {
        checkIndices();
        return columnIndices.clone();
    }

    final ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        checkIndices();
        return columnIndices.getColumnInfo(clazz);
    }

    final long getSchemaVersion() {
        checkIndices();
        return this.columnIndices.getSchemaVersion();
    }

    final boolean isProxyClass(Class<? extends RealmModel> modelClass, Class<? extends RealmModel> testee) {
        return modelClass.equals(testee);
    }

    static String getSchemaForTable(Table table) {
        return table.getName().substring(Table.TABLE_PREFIX.length());
    }

    private void checkIndices() {
        if (this.columnIndices == null) {
            throw new IllegalStateException("Attempt to use column index before set.");
        }
    }
}
