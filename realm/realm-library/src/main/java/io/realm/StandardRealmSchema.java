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

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.internal.ColumnInfo;
import io.realm.internal.NativeObject;
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

        String internalClassName = Table.getTableNameForClass(className);
        if (!realm.getSharedRealm().hasTable(internalClassName)) { return null; }
        Table table = realm.getSharedRealm().getTable(internalClassName);
        return new StandardRealmObjectSchema(realm, this, table);
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
            schemas.add(new StandardRealmObjectSchema(realm, this, realm.getSharedRealm().getTable(tableName)));
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

        String internalTableName = Table.getTableNameForClass(className);
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is too long. Limit is 56 characters: " + className.length());
        }
        if (realm.getSharedRealm().hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        return new StandardRealmObjectSchema(realm, this, realm.getSharedRealm().getTable(internalTableName));
    }

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    @Override
    public boolean contains(String className) {
        return realm.getSharedRealm().hasTable(Table.getTableNameForClass(className));
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
        String internalTableName = Table.getTableNameForClass(className);
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
        String oldInternalName = Table.getTableNameForClass(oldClassName);
        String newInternalName = Table.getTableNameForClass(newClassName);
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

        return new StandardRealmObjectSchema(realm, this, table);
    }

    private void checkEmpty(String str, String error) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    private void checkHasTable(String className, String errorMsg) {
        String internalTableName = Table.getTableNameForClass(className);
        if (!realm.getSharedRealm().hasTable(internalTableName)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    @Override
    Table getTable(String className) {
        String tableName = Table.getTableNameForClass(className);
        Table table = dynamicClassToTable.get(tableName);
        if (table != null) { return table; }

        if (!realm.getSharedRealm().hasTable(tableName)) {
            throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
        }
        table = realm.getSharedRealm().getTable(tableName);
        dynamicClassToTable.put(tableName, table);

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
            classSchema = new StandardRealmObjectSchema(realm, this, table, getColumnInfo(originalClass));
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
        String tableName = Table.getTableNameForClass(className);
        StandardRealmObjectSchema dynamicSchema = dynamicClassToSchema.get(tableName);
        if (dynamicSchema == null) {
            if (!realm.getSharedRealm().hasTable(tableName)) {
                throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
            }
            dynamicSchema = new StandardRealmObjectSchema(realm, this, realm.getSharedRealm().getTable(tableName));
            dynamicClassToSchema.put(tableName, dynamicSchema);
        }
        return dynamicSchema;
    }

    /**
     * @inheritDoc
     *
     * TODO:
     * I suspect that choosing the parsing strategy based on whether there is a ref to a ColumnIndices
     * around or not, is bad architecture.  Almost certainly, there should be a schema that has
     * ColumnIndices and one that does not and the strategies below should belong to the first
     * and second, respectively.  --gbm
     */
    @Override
    long[][] getColumnIndices(Table table, String fieldDescription, RealmFieldType... validColumnTypes) {
        return (haveColumnInfo())
                ? getColumnIndicesCached(table.getClassName(), fieldDescription, validColumnTypes)
                : getColumnIndicesDynamic(table, fieldDescription, validColumnTypes);
    }

    private long[][] getColumnIndicesCached(String tableName, String fieldDescription, RealmFieldType... validColumnTypes) {
        List<String> fields = parseFieldDescription(fieldDescription);
        int nFields = fields.size();
        if (nFields <= 0) {
            throw new IllegalArgumentException("Invalid query: Empty field descriptor");
        }

        long[][] columnInfo = new long[2][];
        columnInfo[0] = new long[nFields];
        columnInfo[1] = new long[nFields];

        String currentTable = tableName;

        ColumnInfo tableInfo;
        String columnName = null;
        RealmFieldType columnType = null;
        long columnIndex;
        for (int i = 0; i < nFields; i++) {
            columnName = fields.get(i);
            if ((columnName == null) || (columnName.length() <= 0)) {
                throw new IllegalArgumentException(
                        "Invalid query: Field descriptor contains an empty field.  A field description may not begin with or contain adjacent periods ('.').");
            }

            tableInfo = getColumnInfo(currentTable);
            if (tableInfo == null) {
                throw new IllegalArgumentException(
                        String.format("Invalid query: table '%s' not found in this schema.", currentTable));
            }

            columnIndex = tableInfo.getColumnIndex(columnName);
            if (columnIndex < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid query: field '%s' not found in table '%s'.", columnName, currentTable));
            }

            columnType = tableInfo.getColumnType(columnName);
            // all but the last field must be a link type
            if (i < nFields - 1) {
                verifyColumnType(currentTable, columnName, columnType, RealmFieldType.OBJECT, RealmFieldType.LIST, RealmFieldType.LINKING_OBJECTS);
                currentTable = tableInfo.getLinkedTable(columnName);
            }
            columnInfo[0][i] = columnIndex;
            columnInfo[1][i] = (columnType != RealmFieldType.LINKING_OBJECTS)
                    ? NativeObject.NULLPTR
                    : getNativeTablePtr(currentTable);
        }

        verifyColumnType(tableName, columnName, columnType, validColumnTypes);

        return columnInfo;
    }

    // Backlinks are not supported here.
    private long[][] getColumnIndicesDynamic(Table table, String fieldDescription, RealmFieldType... validColumnTypes) {
        List<String> fields = parseFieldDescription(fieldDescription);
        int nFields = fields.size();
        if (nFields <= 0) {
            throw new IllegalArgumentException("Invalid query: Empty field descriptor");
        }

        long[][] columnInfo = new long[2][];
        columnInfo[0] = new long[nFields];
        columnInfo[1] = new long[nFields];

        Table currentTable = table;

        String tableName = null;
        String columnName = null;
        RealmFieldType columnType = null;
        long columnIndex;
        for (int i = 0; i < nFields; i++) {
            columnName = fields.get(i);
            if ((columnName == null) || (columnName.length() <= 0)) {
                throw new IllegalArgumentException(
                        "Invalid query: Field descriptor contains an empty field.  A field description may not begin with or contain adjacent periods ('.').");
            }
            // "Invalid query:  field descriptor '" + fieldDescription + "': "

            tableName = currentTable.getClassName();

            columnIndex = currentTable.getColumnIndex(columnName);
            if (columnIndex < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid query: field '%s' not found in table '%s'.", columnName, tableName));
            }

            columnType = currentTable.getColumnType(columnIndex);
            // all but the last field must be a link type
            if (i < nFields - 1) {
                verifyColumnType(tableName, columnName, columnType, RealmFieldType.OBJECT, RealmFieldType.LIST);
                currentTable = currentTable.getLinkTarget(columnIndex);
            }

            columnInfo[0][i] = columnIndex;
            columnInfo[1][i] = NativeObject.NULLPTR;
        }

        verifyColumnType(tableName, columnName, columnType, validColumnTypes);

        return columnInfo;
    }

    private void verifyColumnType(String tableName, String columnName, RealmFieldType columnType, RealmFieldType... validColumnTypes) {
        if ((validColumnTypes == null) || (validColumnTypes.length <= 0)) {
            return;
        }

        for (int i = 0; i < validColumnTypes.length; i++) {
            if (validColumnTypes[i] == columnType) {
                return;
            }
        }

        throw new IllegalArgumentException(String.format(
                "Invalid query: field '%s' in table '%s' is of invalid type '%s'.",
                columnName, tableName, columnType.toString()));
    }

    private long getNativeTablePtr(String targetTable) {
        return getTable(targetTable).getNativePtr();
    }
}
