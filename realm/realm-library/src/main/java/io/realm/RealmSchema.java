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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnInfo;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.Util;
import io.realm.internal.util.Pair;

/**
 * Class for interacting with the Realm schema. This makes it possible to inspect, add, delete and change the classes in
 * the Realm.
 * <p>
 * {@link Realm#getSchema()} returns an immutable {@code RealmSchema} which can only be used for inspecting. Use
 * {@link DynamicRealm#getSchema()} to get a mutable schema.
 * <p>
 * All changes must happen inside a write transaction for the particular Realm.
 *
 * @see RealmMigration
 */
public abstract class RealmSchema {
    static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";

    // Caches Dynamic Class objects given as Strings to Realm Tables
    private final Map<String, Table> dynamicClassToTable = new HashMap<>();
    // Caches Class objects (both model classes and proxy classes) to Realm Tables
    private final Map<Class<? extends RealmModel>, Table> classToTable = new HashMap<>();
    // Caches Class objects (both model classes and proxy classes) to their Schema object
    private final Map<Class<? extends RealmModel>, RealmObjectSchema> classToSchema = new HashMap<>();
    // Caches Class Strings to their Schema object
    private final Map<String, RealmObjectSchema> dynamicClassToSchema = new HashMap<>();

    final BaseRealm realm;
    // Cached field look up
    private final ColumnIndices columnIndices;

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    RealmSchema(BaseRealm realm, @Nullable ColumnIndices columnIndices) {
        this.realm = realm;
        this.columnIndices = columnIndices;
    }

    /**
     * Returns the {@link RealmObjectSchema} for a given class. If this {@link RealmSchema} is immutable, an immutable
     * {@link RealmObjectSchema} will be returned. Otherwise, it returns an mutable {@link RealmObjectSchema}.
     *
     * @param className name of the class
     * @return schema object for that class or {@code null} if the class doesn't exists.
     */
    @Nullable
    public abstract RealmObjectSchema get(String className);

    /**
     * Returns the {@link RealmObjectSchema}s for all RealmObject classes that can be saved in this Realm. If this
     * {@link RealmSchema} is immutable, an immutable {@link RealmObjectSchema} set will be returned. Otherwise, it
     * returns an mutable {@link RealmObjectSchema} set.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    public abstract Set<RealmObjectSchema> getAll();

    /**
     * Adds a new class to the Realm.
     *
     * @param className name of the class.
     * @return a Realm schema object for that class.
     * @throws UnsupportedOperationException if this {@link RealmSchema} is immutable.
     */
    public abstract RealmObjectSchema create(String className);

    /**
     * Adds a new class to the Realm with a primary key field defined.
     *
     * @param className           name of the class.
     * @param primaryKeyFieldName name of the primary key field.
     * @param fieldType           type of field to add. Only {@code byte}, {@code short}, {@code int}, {@code long}
     *                            and their boxed types or the {@code String} is supported.
     * @param attributes          set of attributes for this field. This method implicitly adds
     *                            {@link FieldAttribute#PRIMARY_KEY} and {@link FieldAttribute#INDEXED} attributes to
     *                            the field.
     * @throws UnsupportedOperationException if this {@link RealmSchema} is immutable.
     * @return a Realm schema object for that class.
     */
    public abstract RealmObjectSchema createWithPrimaryKeyField(String className, String primaryKeyFieldName, Class<?> fieldType,
                                                       FieldAttribute... attributes);

    /**
     * Removes a class from the Realm. All data will be removed. Removing a class while other classes point
     * to it will throw an {@link IllegalStateException}. Removes those classes or fields first.
     *
     * @param className name of the class to remove.
     * @throws UnsupportedOperationException if this {@link RealmSchema} is immutable.
     */
    public abstract void remove(String className);

    /**
     * Renames a class already in the Realm.
     *
     * @param oldClassName old class name.
     * @param newClassName new class name.
     * @return a schema object for renamed class.
     * @throws UnsupportedOperationException if this {@link RealmSchema} is immutable.
     */
    public abstract RealmObjectSchema rename(String oldClassName, String newClassName);

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    public boolean contains(String className) {
        return realm.getSharedRealm().hasTable(Table.getTableNameForClass(className));
    }

    void checkNotEmpty(String str, String error) {
        //noinspection ConstantConditions
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    void checkHasTable(String className, String errorMsg) {
        String internalTableName = Table.getTableNameForClass(className);
        if (!realm.getSharedRealm().hasTable(internalTableName)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    Table getTable(String className) {
        String tableName = Table.getTableNameForClass(className);
        Table table = dynamicClassToTable.get(tableName);
        if (table != null) { return table; }

        table = realm.getSharedRealm().getTable(tableName);
        dynamicClassToTable.put(tableName, table);

        return table;
    }

    Table getTable(Class<? extends RealmModel> clazz) {
        Table table = classToTable.get(clazz);
        if (table != null) { return table; }

        Class<? extends RealmModel> originalClass = Util.getOriginalModelClass(clazz);
        if (isProxyClass(originalClass, clazz)) {
            // If passed 'clazz' is the proxy, try again with model class.
            table = classToTable.get(originalClass);
        }
        if (table == null) {
            String tableName = Table.getTableNameForClass(
                    realm.getConfiguration().getSchemaMediator().getSimpleClassName(originalClass));
            table = realm.getSharedRealm().getTable(tableName);
            classToTable.put(originalClass, table);
        }
        if (isProxyClass(originalClass, clazz)) {
            // 'clazz' is the proxy class for 'originalClass'.
            classToTable.put(clazz, table);
        }

        return table;
    }

    // Returns an immutable RealmObjectSchema for internal usage only.
    RealmObjectSchema getSchemaForClass(Class<? extends RealmModel> clazz) {
        RealmObjectSchema classSchema = classToSchema.get(clazz);
        if (classSchema != null) { return classSchema; }

        Class<? extends RealmModel> originalClass = Util.getOriginalModelClass(clazz);
        if (isProxyClass(originalClass, clazz)) {
            // If passed 'clazz' is the proxy, try again with model class.
            classSchema = classToSchema.get(originalClass);
        }
        if (classSchema == null) {
            Table table = getTable(clazz);
            classSchema = new ImmutableRealmObjectSchema(realm, this, table, getColumnInfo(originalClass));
            classToSchema.put(originalClass, classSchema);
        }
        if (isProxyClass(originalClass, clazz)) {
            // 'clazz' is the proxy class for 'originalClass'.
            classToSchema.put(clazz, classSchema);
        }

        return classSchema;
    }

    // Returns an immutable RealmObjectSchema for internal usage only.
    RealmObjectSchema getSchemaForClass(String className) {
        String tableName = Table.getTableNameForClass(className);
        RealmObjectSchema dynamicSchema = dynamicClassToSchema.get(tableName);
        if (dynamicSchema == null || !dynamicSchema.getTable().isValid() || !dynamicSchema.getClassName().equals(className)) {
            if (!realm.getSharedRealm().hasTable(tableName)) {
                throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
            }
            dynamicSchema = new ImmutableRealmObjectSchema(realm, this, realm.getSharedRealm().getTable(tableName));
            dynamicClassToSchema.put(tableName, dynamicSchema);
        }
        return dynamicSchema;
    }

    private boolean isProxyClass(Class<? extends RealmModel> modelClass, Class<? extends RealmModel> testee) {
        return modelClass.equals(testee);
    }

    final boolean haveColumnInfo() {
        return columnIndices != null;
    }

    final ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        checkIndices();
        return columnIndices.getColumnInfo(clazz);
    }

    protected final ColumnInfo getColumnInfo(String className) {
        checkIndices();
        return columnIndices.getColumnInfo(className);
    }

    final void putToClassNameToSchemaMap(String name, RealmObjectSchema objectSchema) {
        dynamicClassToSchema.put(name, objectSchema);
    }

    final RealmObjectSchema removeFromClassNameToSchemaMap(String name) {
        return dynamicClassToSchema.remove(name);
    }

    private void checkIndices() {
        if (!haveColumnInfo()) {
            throw new IllegalStateException("Attempt to use column index before set.");
        }
    }

    /**
     * Called when schema changed. Clear all cached tables and refresh column indices.
     */
    void refresh() {
        if (columnIndices != null) {
            columnIndices.refresh();
        }
        dynamicClassToTable.clear();
        classToTable.clear();
        classToSchema.clear();
        dynamicClassToSchema.clear();
    }
}
