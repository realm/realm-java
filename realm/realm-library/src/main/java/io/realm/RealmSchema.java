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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnInfo;
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
public class RealmSchema {

    private static final String TABLE_PREFIX = Table.TABLE_PREFIX;
    private static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";

    // Caches Dynamic Class objects given as Strings to Realm Tables
    private final Map<String, Table> dynamicClassToTable = new HashMap<String, Table>();
    // Caches Class objects (both model classes and proxy classes) to Realm Tables
    private final Map<Class<? extends RealmModel>, Table> classToTable = new HashMap<Class<? extends RealmModel>, Table>();
    // Caches Class objects (both model classes and proxy classes) to their Schema object
    private final Map<Class<? extends RealmModel>, RealmObjectSchema> classToSchema = new HashMap<Class<? extends RealmModel>, RealmObjectSchema>();
    // Caches Class Strings to their Schema object
    private final Map<String, RealmObjectSchema> dynamicClassToSchema = new HashMap<String, RealmObjectSchema>();

    private final BaseRealm realm;
    private long nativePtr;
    ColumnIndices columnIndices; // Cached field look up

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    RealmSchema(BaseRealm realm) {
        this.realm = realm;
        this.nativePtr = 0;
    }

    /**
     * Creates a wrappor to easily manipulate Object Store schemas. This constructor should only be called by
     * proxy classes during validation of schema.
     */
    RealmSchema() {
        // This is the case where the schema is created from the proxy classes.
        // dynamicClassToSchema is used to keep track of which model classes have been processed.
        this.realm = null;
        this.nativePtr = 0;
        // TODO: create a Object Store realm::Schema object and store the native pointer
    }


    RealmSchema(ArrayList<RealmObjectSchema> realmObjectSchemas) {
        long list[] = new long[realmObjectSchemas.size()];
        for (int i = 0; i < realmObjectSchemas.size(); i++) {
            list[i] = realmObjectSchemas.get(i).getNativePtr();
        }
        this.nativePtr = nativeCreateFromList(list);
        this.realm = null;
    }

    public long getNativePtr() {
        return this.nativePtr;
    }

    public void close() {
        if (nativePtr != 0) {
            Set<RealmObjectSchema> schemas = getAll();
            for (RealmObjectSchema schema : schemas) {
                schema.close();
            }
            nativeClose(nativePtr);
        }
    }

    /**
     * Returns the Realm schema for a given class.
     *
     * @param className name of the class
     * @return schema object for that class or {@code null} if the class doesn't exists.
     *
     */
    public RealmObjectSchema get(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        if (realm == null) {
            if (contains(className)) {
                return dynamicClassToSchema.get(className);
            } else {
                return null;
            }
        } else {
            String internalClassName = TABLE_PREFIX + className;
            if (realm.sharedRealm.hasTable(internalClassName)) {
                Table table = realm.sharedRealm.getTable(internalClassName);
                RealmObjectSchema.DynamicColumnMap columnIndices = new RealmObjectSchema.DynamicColumnMap(table);
                return new RealmObjectSchema(realm, table, columnIndices);
            } else {
                return null;
            }
        }
    }

    /**
     * Returns the {@link RealmObjectSchema} for all RealmObject classes that can be saved in this Realm.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    public Set<RealmObjectSchema> getAll() {
        if (realm == null) {
            long[] ptrs = nativeGetAll(nativePtr);
            Set<RealmObjectSchema> schemas = new LinkedHashSet<>(ptrs.length);
            for (int i = 0; i < ptrs.length; i++) {
                schemas.add(new RealmObjectSchema(ptrs[i]));
            }
            return schemas;
        } else {
            int tableCount = (int) realm.sharedRealm.size();
            Set<RealmObjectSchema> schemas = new LinkedHashSet<>(tableCount);
            for (int i = 0; i < tableCount; i++) {
                String tableName = realm.sharedRealm.getTableName(i);
                if (!Table.isModelTable(tableName)) {
                    continue;
                }
                Table table = realm.sharedRealm.getTable(tableName);
                RealmObjectSchema.DynamicColumnMap columnIndices = new RealmObjectSchema.DynamicColumnMap(table);
                schemas.add(new RealmObjectSchema(realm, table, columnIndices));
            }
            return schemas;
        }
    }

    /**
     * Adds a new class to the Realm.
     *
     * @param className name of the class.
     * @return a Realm schema object for that class.
     */
    public RealmObjectSchema create(String className) {
        // adding a class is always permitted
        checkEmpty(className, EMPTY_STRING_MSG);
        if (realm == null) {
            RealmObjectSchema realmObjectSchema = new RealmObjectSchema(className);
            dynamicClassToSchema.put(className, realmObjectSchema);
            return realmObjectSchema;
        } else {
            String internalTableName = TABLE_PREFIX + className;
            if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
                throw new IllegalArgumentException("Class name is to long. Limit is 57 characters: " + className.length());
            }
            if (realm.sharedRealm.hasTable(internalTableName)) {
                throw new IllegalArgumentException("Class already exists: " + className);
            }
            Table table = realm.sharedRealm.getTable(internalTableName);
            RealmObjectSchema.DynamicColumnMap columnIndices = new RealmObjectSchema.DynamicColumnMap(table);
            return new RealmObjectSchema(realm, table, columnIndices);
        }
    }

    /**
     * Removes a class from the Realm. All data will be removed. Removing a class while other classes point
     * to it will throw an {@link IllegalStateException}. Remove those classes or fields first.
     *
     * @param className name of the class to remove.
     */
    public void remove(String className) {
        realm.checkNotInSync(); // destructive modifications are not permitted
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalTableName = TABLE_PREFIX + className;
        checkHasTable(className, "Cannot remove class because it is not in this Realm: " + className);
        Table table = getTable(className);
        if (table.hasPrimaryKey()) {
            table.setPrimaryKey(null);
        }
        realm.sharedRealm.removeTable(internalTableName);
    }

    /**
     * Renames a class already in the Realm.
     *
     * @param oldClassName old class name.
     * @param newClassName new class name.
     * @return a schema object for renamed class.
     */
    public RealmObjectSchema rename(String oldClassName, String newClassName) {
        realm.checkNotInSync(); // destructive modifications are not permitted
        checkEmpty(oldClassName, "Class names cannot be empty or null");
        checkEmpty(newClassName, "Class names cannot be empty or null");
        String oldInternalName = TABLE_PREFIX + oldClassName;
        String newInternalName = TABLE_PREFIX + newClassName;
        checkHasTable(oldClassName, "Cannot rename class because it doesn't exist in this Realm: " + oldClassName);
        if (realm.sharedRealm.hasTable(newInternalName)) {
            throw new IllegalArgumentException(oldClassName + " cannot be renamed because the new class already exists: " + newClassName);
        }

        // Check if there is a primary key defined for the old class.
        Table oldTable = getTable(oldClassName);
        String pkField = null;
        if (oldTable.hasPrimaryKey()) {
            pkField = oldTable.getColumnName(oldTable.getPrimaryKey());
            oldTable.setPrimaryKey(null);
        }

        realm.sharedRealm.renameTable(oldInternalName, newInternalName);
        Table table = realm.sharedRealm.getTable(newInternalName);

        // Set the primary key for the new class if necessary
        if (pkField != null) {
            table.setPrimaryKey(pkField);
        }

        RealmObjectSchema.DynamicColumnMap columnIndices = new RealmObjectSchema.DynamicColumnMap(table);
        return new RealmObjectSchema(realm, table, columnIndices);
    }

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    public boolean contains(String className) {
        if (realm == null) {
            return dynamicClassToSchema.containsKey(className);
        } else {
            return realm.sharedRealm.hasTable(Table.TABLE_PREFIX + className);
        }
    }

    private void checkEmpty(String str, String error) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    private void checkHasTable(String className, String errorMsg) {
        String internalTableName = TABLE_PREFIX + className;
        if (!realm.sharedRealm.hasTable(internalTableName)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        final ColumnInfo columnInfo = columnIndices.getColumnInfo(clazz);
        if (columnInfo == null) {
            throw new IllegalStateException("No validated schema information found for " + realm.configuration.getSchemaMediator().getTableName(clazz));
        }
        return columnInfo;
    }

    Table getTable(String className) {
        className = Table.TABLE_PREFIX + className;
        Table table = dynamicClassToTable.get(className);
        if (table == null) {
            if (!realm.sharedRealm.hasTable(className)) {
                throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
            }
            table = realm.sharedRealm.getTable(className);
            dynamicClassToTable.put(className, table);
        }
        return table;
    }

    Table getTable(Class<? extends RealmModel> clazz) {
        Table table = classToTable.get(clazz);
        if (table == null) {
            Class<? extends RealmModel> originalClass = Util.getOriginalModelClass(clazz);
            if (isProxyClass(originalClass, clazz)) {
                // if passed 'clazz' is the proxy, try again with model class
                table = classToTable.get(originalClass);
            }
            if (table == null) {
                table = realm.sharedRealm.getTable(realm.configuration.getSchemaMediator().getTableName(originalClass));
                classToTable.put(originalClass, table);
            }
            if (isProxyClass(originalClass, clazz)) {
                // 'clazz' is the proxy class for 'originalClass'
                classToTable.put(clazz, table);
            }
        }
        return table;
    }

    RealmObjectSchema getSchemaForClass(Class<? extends RealmModel> clazz) {
        RealmObjectSchema classSchema = classToSchema.get(clazz);
        if (classSchema == null) {
            Class<? extends RealmModel> originalClass = Util.getOriginalModelClass(clazz);
            if (isProxyClass(originalClass, clazz)) {
                // if passed 'clazz' is the proxy, try again with model class
                classSchema = classToSchema.get(originalClass);
            }
            if (classSchema == null) {
                Table table = getTable(clazz);
                classSchema = new RealmObjectSchema(realm, table, columnIndices.getColumnInfo(originalClass).getIndicesMap());
                classToSchema.put(originalClass, classSchema);
            }
            if (isProxyClass(originalClass, clazz)) {
                // 'clazz' is the proxy class for 'originalClass'
                classToSchema.put(clazz, classSchema);
            }
        }
        return classSchema;
    }

    private static boolean isProxyClass(Class<? extends RealmModel> modelClass,
                                        Class<? extends RealmModel> testee) {
        return modelClass != testee;
    }

    RealmObjectSchema getSchemaForClass(String className) {
        className = Table.TABLE_PREFIX + className;
        RealmObjectSchema dynamicSchema = dynamicClassToSchema.get(className);
        if (dynamicSchema == null) {
            if (!realm.sharedRealm.hasTable(className)) {
                throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
            }
            Table table = realm.sharedRealm.getTable(className);
            RealmObjectSchema.DynamicColumnMap columnIndices = new RealmObjectSchema.DynamicColumnMap(table);
            dynamicSchema = new RealmObjectSchema(realm, table, columnIndices);
            dynamicClassToSchema.put(className, dynamicSchema);
        }
        return dynamicSchema;
    }

    static String getSchemaForTable(Table table) {
        return table.getName().substring(Table.TABLE_PREFIX.length());
    }

    static native long nativeCreateFromList(long[] objectSchemaPtrs);
    static native void nativeClose(long nativePtr);
    static native long[] nativeGetAll(long nativePtr);
}
