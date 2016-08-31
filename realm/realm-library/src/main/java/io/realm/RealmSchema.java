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
public final class RealmSchema {

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
    ColumnIndices columnIndices; // Cached field look up

    private long nativePtr;

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    RealmSchema() {
        realm = null;
        nativePtr = nativeCreateSchema();
    }

    RealmSchema(BaseRealm realm) {
        this.realm = realm;
        nativePtr = realm.sharedRealm.schema().getNativePtr();
    }

    RealmSchema(BaseRealm realm, ArrayList<RealmObjectSchema> realmObjectSchemas) {
        long[] nativeRealmObjectSchemaPtrs = new long[realmObjectSchemas.size()];
        for (int i = 0; i < realmObjectSchemas.size(); i++) {
            nativeRealmObjectSchemaPtrs[i] = realmObjectSchemas.get(i).getNativePtr();
        }
        nativePtr = nativeCreateSchemaFromArray(nativeRealmObjectSchemaPtrs);
        this.realm = realm;
    }

    public RealmSchema(long nativePtr) {
        this.realm = null;
        this.nativePtr = nativePtr;
    }

    public long getNativePtr() {
        return nativePtr;
    }

    /**
     * Close/delete native resources
     */
    public void close() {
        nativeClose(nativePtr);
        if (realm != null) {
            realm.close();
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
        // FIXME: if realm == null?
        if (hasObjectSchemaByName(className)) {
            RealmObjectSchema realmObjectSchema = getObjectSchemaByName(className);
            dynamicClassToSchema.put(className, realmObjectSchema);
            return realmObjectSchema;
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link RealmObjectSchema} for all RealmObject classes that can be saved in this Realm.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    public Set<RealmObjectSchema> getAll() {
        // FIXME: if realm == null?
        int count = (int)nativeSize(nativePtr);
        Set<RealmObjectSchema> schemas = new LinkedHashSet<RealmObjectSchema>(count);
        long[] nativeRealmObjectSchemaPtr = nativeGetRealmObjectSchemas(nativePtr);
        for (int i = 0; i < count; i++) {
            schemas.add(new RealmObjectSchema(nativeRealmObjectSchemaPtr[i]));
        }
        return schemas;
    }

    /**
     * Adds a new class to the Realm.
     *
     * @param className name of the class.
     * @return a Realm schema object for that class.
     */
    public RealmObjectSchema create(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        if (dynamicClassToSchema.containsKey(className)) {
            return dynamicClassToSchema.get(className);
        } else {
            RealmObjectSchema realmObjectSchema;
            if (realm == null) {
                realmObjectSchema = new RealmObjectSchema(className);
            } else {
                if (realm.sharedRealm.hasTable(Table.TABLE_PREFIX + className)) {
                    throw new IllegalArgumentException("Class already exists: " + className);
                }
                Table table = realm.sharedRealm.getTable(Table.TABLE_PREFIX + className);
                realmObjectSchema = realm.sharedRealm.schema().getObjectSchemaByName(className);
            }
            dynamicClassToSchema.put(className, realmObjectSchema);
            return realmObjectSchema;
        }
    }

    /**
     * Removes a class from the Realm. All data will be removed. Removing a class while other classes point
     * to it will throw an {@link IllegalStateException}. Remove those classes or fields first.
     *
     * @param className name of the class to remove.
     */
    public void remove(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        if (realm == null) {
            throw new IllegalArgumentException("You cannot remove classes from standalone schemas.");
        }
        // realm != null means migration/dynamic Realm
        String internalTableName = TABLE_PREFIX + className;
        checkHasTable(className, "Cannot remove class because it is not in this Realm: " + className);
        Table table = getTable(className);
        if (table.hasPrimaryKey()) {
            table.setPrimaryKey(null);
        }
        realm.sharedRealm.removeTable(internalTableName);
        dynamicClassToSchema.remove(className);
    }

    /**
     * Renames a class already in the Realm.
     *
     * @param oldClassName old class name.
     * @param newClassName new class name.
     * @return a schema object for renamed class.
     */
    public RealmObjectSchema rename(String oldClassName, String newClassName) {
        checkEmpty(oldClassName, "Class names cannot be empty or null");
        checkEmpty(newClassName, "Class names cannot be empty or null");

        if (hasObjectSchemaByName(oldClassName)) {
            RealmObjectSchema realmObjectSchema = getObjectSchemaByName(oldClassName);
            dynamicClassToSchema.remove(oldClassName);
            dynamicClassToSchema.put(newClassName, realmObjectSchema);
            if (realm == null) {
                realmObjectSchema.setClassName(newClassName);
                return realmObjectSchema;
            } else {
                realm.sharedRealm.renameTable(oldClassName, newClassName);
                return getObjectSchemaByName(newClassName); // FIXME: is object_schema updated?
            }
        } else {
            throw new IllegalArgumentException("Cannot rename class because it is not in this Realm: " + oldClassName);
        }
    }

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    public boolean contains(String className) {
        return hasObjectSchemaByName(className);
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
        String className = clazz.getName();
        return getSchemaForClass(className);
    }

    private static boolean isProxyClass(Class<? extends RealmModel> modelClass,
                                        Class<? extends RealmModel> testee) {
        return modelClass != testee;
    }

    RealmObjectSchema getSchemaForClass(String className) {
        if (hasObjectSchemaByName(className)) {
            return getObjectSchemaByName(className);
        } else {
            return null;
        }
    }

    void setColumnIndices(ColumnIndices columnIndices) {
        this.columnIndices = columnIndices;
    }

    static String getSchemaForTable(Table table) {
        return table.getName().substring(Table.TABLE_PREFIX.length());
    }

    /**
     * Checks if a schema has a named object schema.
     *
     * @param name the name of the object schema (model class)
     *
     * @return {@code true} if schema has object schema, {@code false} otherwise
     */
    public boolean hasObjectSchemaByName(String name) {
        if (dynamicClassToSchema.containsKey(name)) {
            return true;
        } else {
            if (realm == null) {
                return nativeHasObjectSchemaByName(nativePtr, name);
            } else {
                return realm.sharedRealm.hasTable(Table.TABLE_PREFIX + name);
            }
        }
    }

    public RealmObjectSchema getObjectSchemaByName(String name) {
        if (dynamicClassToSchema.containsKey(name)) {
            return dynamicClassToSchema.get(name);
        } else {
            if (realm == null) {
                return new RealmObjectSchema(name);
            } else {
                return new RealmObjectSchema(realm, name, null);
            }
        }
    }

    private static native long nativeCreateSchema();
    private static native long nativeCreateSchemaFromArray(long[] realmObjectSchemaPtrs);
    private static native void nativeClose(long nativePtr);
    private static native long nativeSize(long nativePtr);
    private static native boolean nativeHasObjectSchemaByName(long nativePtr, String name);
    private static native long nativeGetObjectSchemaByName(long nativePtr, String name);
    private static native long[] nativeGetRealmObjectSchemas(long nativePtr);
}
