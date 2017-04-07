/*
 * Copyright 2017 Realm Inc.
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

import io.realm.internal.Table;
import io.realm.internal.Util;


/**
 * Class for interacting with the Realm schema using a dynamic API. This makes it possible
 * to add, delete and change the classes in the Realm.
 * <p>
 * All changes must happen inside a write transaction for the particular Realm.
 *
 * @see io.realm.RealmMigration
 */
class StandardRealmSchema extends RealmSchema {

    static final String TABLE_PREFIX = Table.TABLE_PREFIX;
    private static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";

    // Caches Dynamic Class objects given as Strings to Realm Tables
    private final Map<String, Table> dynamicClassToTable = new HashMap<>();
    // Caches Class objects (both model classes and proxy classes) to Realm Tables
    private final Map<Class<? extends RealmModel>, Table> classToTable = new HashMap<>();
    // Caches Class objects (both model classes and proxy classes) to their Schema object
    private final Map<Class<? extends RealmModel>, StandardRealmObjectSchema> classToSchema = new HashMap<>();
    // Caches Class Strings to their Schema object
    private final Map<String, StandardRealmObjectSchema> dynamicClassToSchema = new HashMap<>();

    private final BaseRealm realm;

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    StandardRealmSchema(BaseRealm realm) {
        this.realm = realm;
    }

    @Override
    public void close() { }

    /**
     * Returns the Realm schema for a given class.
     *
     * @param className name of the class
     * @return schema object for that class or {@code null} if the class doesn't exists.
     */
    @Override
    public RealmObjectSchema get(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);

        String internalClassName = TABLE_PREFIX + className;
        if (!realm.getSharedRealm().hasTable(internalClassName)) { return null; }

        Table table = realm.getSharedRealm().getTable(internalClassName);
        StandardRealmObjectSchema.DynamicColumnMap columnIndices = new StandardRealmObjectSchema.DynamicColumnMap(table);
        return new StandardRealmObjectSchema(realm, table, columnIndices);
    }

    /**
     * Returns the {@link StandardRealmObjectSchema} for all RealmObject classes that can be saved in this Realm.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    @Override
    public Set<RealmObjectSchema> getAll() {
        int tableCount = (int) realm.getSharedRealm().size();
        Set<RealmObjectSchema> schemas = new LinkedHashSet<>(tableCount);
        for (int i = 0; i < tableCount; i++) {
            String tableName = realm.getSharedRealm().getTableName(i);
            if (!Table.isModelTable(tableName)) {
                continue;
            }
            Table table = realm.getSharedRealm().getTable(tableName);
            StandardRealmObjectSchema.DynamicColumnMap columnIndices = new StandardRealmObjectSchema.DynamicColumnMap(table);
            schemas.add(new StandardRealmObjectSchema(realm, table, columnIndices));
        }
        return schemas;
    }

    /**
     * Adds a new class to the Realm.
     *
     * @param className name of the class.
     * @return a Realm schema object for that class.
     */
    @Override
    public RealmObjectSchema create(String className) {
        // Adding a class is always permitted.
        checkEmpty(className, EMPTY_STRING_MSG);

        String internalTableName = TABLE_PREFIX + className;
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is too long. Limit is 56 characters: " + className.length());
        }
        if (realm.getSharedRealm().hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        Table table = realm.getSharedRealm().getTable(internalTableName);
        StandardRealmObjectSchema.DynamicColumnMap columnIndices = new StandardRealmObjectSchema.DynamicColumnMap(table);
        return new StandardRealmObjectSchema(realm, table, columnIndices);
    }

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    @Override
    public boolean contains(String className) {
        return realm.getSharedRealm().hasTable(Table.TABLE_PREFIX + className);
    }

    /**
     * Removes a class from the Realm. All data will be removed. Removing a class while other classes point
     * to it will throw an {@link IllegalStateException}. Removes those classes or fields first.
     *
     * @param className name of the class to remove.
     */
    @Override
    public void remove(String className) {
        realm.checkNotInSync(); // Destructive modifications are not permitted.
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalTableName = TABLE_PREFIX + className;
        checkHasTable(className, "Cannot remove class because it is not in this Realm: " + className);
        Table table = getTable(className);
        if (table.hasPrimaryKey()) {
            table.setPrimaryKey(null);
        }
        realm.getSharedRealm().removeTable(internalTableName);
    }

    /**
     * Renames a class already in the Realm.
     *
     * @param oldClassName old class name.
     * @param newClassName new class name.
     * @return a schema object for renamed class.
     */
    @Override
    public RealmObjectSchema rename(String oldClassName, String newClassName) {
        realm.checkNotInSync(); // Destructive modifications are not permitted.
        checkEmpty(oldClassName, "Class names cannot be empty or null");
        checkEmpty(newClassName, "Class names cannot be empty or null");
        String oldInternalName = TABLE_PREFIX + oldClassName;
        String newInternalName = TABLE_PREFIX + newClassName;
        checkHasTable(oldClassName, "Cannot rename class because it doesn't exist in this Realm: " + oldClassName);
        if (realm.getSharedRealm().hasTable(newInternalName)) {
            throw new IllegalArgumentException(oldClassName + " cannot be renamed because the new class already exists: " + newClassName);
        }

        // Checks if there is a primary key defined for the old class.
        Table oldTable = getTable(oldClassName);
        String pkField = null;
        if (oldTable.hasPrimaryKey()) {
            pkField = oldTable.getColumnName(oldTable.getPrimaryKey());
            oldTable.setPrimaryKey(null);
        }

        realm.getSharedRealm().renameTable(oldInternalName, newInternalName);
        Table table = realm.getSharedRealm().getTable(newInternalName);

        // Sets the primary key for the new class if necessary.
        if (pkField != null) {
            table.setPrimaryKey(pkField);
        }

        StandardRealmObjectSchema.DynamicColumnMap columnIndices = new StandardRealmObjectSchema.DynamicColumnMap(table);
        return new StandardRealmObjectSchema(realm, table, columnIndices);
    }

    private void checkEmpty(String str, String error) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    private void checkHasTable(String className, String errorMsg) {
        String internalTableName = TABLE_PREFIX + className;
        if (!realm.getSharedRealm().hasTable(internalTableName)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    @Override
    Table getTable(String className) {
        className = Table.TABLE_PREFIX + className;
        Table table = dynamicClassToTable.get(className);
        if (table != null) { return table; }

        if (!realm.getSharedRealm().hasTable(className)) {
            throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
        }
        table = realm.getSharedRealm().getTable(className);
        dynamicClassToTable.put(className, table);

        return table;
    }

    @Override
    Table getTable(Class<? extends RealmModel> clazz) {
        Table table = classToTable.get(clazz);
        if (table != null) { return table; }

        Class<? extends RealmModel> originalClass = Util.getOriginalModelClass(clazz);
        if (isProxyClass(originalClass, clazz)) {
            // If passed 'clazz' is the proxy, try again with model class.
            table = classToTable.get(originalClass);
        }
        if (table == null) {
            table = realm.getSharedRealm().getTable(realm.getConfiguration().getSchemaMediator().getTableName(originalClass));
            classToTable.put(originalClass, table);
        }
        if (isProxyClass(originalClass, clazz)) {
            // 'clazz' is the proxy class for 'originalClass'.
            classToTable.put(clazz, table);
        }

        return table;
    }

    @Override
    StandardRealmObjectSchema getSchemaForClass(Class<? extends RealmModel> clazz) {
        StandardRealmObjectSchema classSchema = classToSchema.get(clazz);
        if (classSchema != null) { return classSchema; }

        Class<? extends RealmModel> originalClass = Util.getOriginalModelClass(clazz);
        if (isProxyClass(originalClass, clazz)) {
            // If passed 'clazz' is the proxy, try again with model class.
            classSchema = classToSchema.get(originalClass);
        }
        if (classSchema == null) {
            Table table = getTable(clazz);
            classSchema = new StandardRealmObjectSchema(realm, table, getColumnInfo(originalClass).getIndicesMap());
            classToSchema.put(originalClass, classSchema);
        }
        if (isProxyClass(originalClass, clazz)) {
            // 'clazz' is the proxy class for 'originalClass'.
            classToSchema.put(clazz, classSchema);
        }
        return classSchema;
    }

    @Override
    StandardRealmObjectSchema getSchemaForClass(String className) {
        className = Table.TABLE_PREFIX + className;
        StandardRealmObjectSchema dynamicSchema = dynamicClassToSchema.get(className);
        if (dynamicSchema == null) {
            if (!realm.getSharedRealm().hasTable(className)) {
                throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
            }
            Table table = realm.getSharedRealm().getTable(className);
            StandardRealmObjectSchema.DynamicColumnMap columnIndices = new StandardRealmObjectSchema.DynamicColumnMap(table);
            dynamicSchema = new StandardRealmObjectSchema(realm, table, columnIndices);
            dynamicClassToSchema.put(className, dynamicSchema);
        }
        return dynamicSchema;
    }
}
