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

import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnInfo;
import io.realm.internal.Table;
import io.realm.internal.Util;
import io.realm.internal.util.Pair;

/**
 * Class for interacting with the Realm schema using a dynamic API. This makes it possible
 * to add, delete and change the classes in the Realm.
 * <p>
 * All changes must happen inside a write transaction for the particular Realm.
 *
 * @see RealmMigration
 */
public class RealmSchema {
    static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";

    // Caches Dynamic Class objects given as Strings to Realm Tables
    private final Map<String, Table> dynamicClassToTable = new HashMap<>();
    // Caches Class objects (both model classes and proxy classes) to Realm Tables
    private final Map<Class<? extends RealmModel>, Table> classToTable = new HashMap<>();
    // Caches Class objects (both model classes and proxy classes) to their Schema object
    private final Map<Class<? extends RealmModel>, RealmObjectSchema> classToSchema = new HashMap<>();
    // Caches Class Strings to their Schema object
    private final Map<String, RealmObjectSchema> dynamicClassToSchema = new HashMap<>();

    private final BaseRealm realm;
    // Cached field look up
    private ColumnIndices columnIndices;

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    RealmSchema(BaseRealm realm) {
        this.realm = realm;
    }

    /**
     * @deprecated {@link RealmSchema} doesn't have to be released manually.
     */
    @Deprecated
    public void close() {
    }

    /**
     * Returns the Realm schema for a given class.
     *
     * @param className name of the class
     * @return schema object for that class or {@code null} if the class doesn't exists.
     */
    public RealmObjectSchema get(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);

        String internalClassName = Table.getTableNameForClass(className);
        if (!realm.getSharedRealm().hasTable(internalClassName)) { return null; }
        Table table = realm.getSharedRealm().getTable(internalClassName);
        return new RealmObjectSchema(realm, this, table);
    }

    /**
     * Returns the {@link RealmObjectSchema}s for all RealmObject classes that can be saved in this Realm.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    public Set<RealmObjectSchema> getAll() {
        int tableCount = (int) realm.getSharedRealm().size();
        Set<RealmObjectSchema> schemas = new LinkedHashSet<>(tableCount);
        for (int i = 0; i < tableCount; i++) {
            String tableName = realm.getSharedRealm().getTableName(i);
            if (!Table.isModelTable(tableName)) {
                continue;
            }
            schemas.add(new RealmObjectSchema(realm, this, realm.getSharedRealm().getTable(tableName)));
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
        // Adding a class is always permitted.
        checkEmpty(className, EMPTY_STRING_MSG);

        String internalTableName = Table.getTableNameForClass(className);
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is too long. Limit is 56 characters: " + className.length());
        }
        return new RealmObjectSchema(realm, this, realm.getSharedRealm().createTable(internalTableName));
    }

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
     * @return a Realm schema object for that class.
     */
    public RealmObjectSchema createWithPrimaryKeyField(String className, String primaryKeyFieldName, Class<?> fieldType,
                                                       FieldAttribute... attributes) {
        checkEmpty(className, EMPTY_STRING_MSG);
        RealmObjectSchema.checkLegalName(primaryKeyFieldName);
        String internalTableName = checkAndGetTableNameFromClassName(className);

        RealmObjectSchema.FieldMetaData metadata = RealmObjectSchema.getSupportedSimpleFields().get(fieldType);
        if (metadata == null || (metadata.realmType != RealmFieldType.STRING &&
                metadata.realmType != RealmFieldType.INTEGER)) {
            throw new IllegalArgumentException(String.format("Realm doesn't support primary key field type '%s'.",
                    fieldType));
        }
        boolean isStringField = (metadata.realmType == RealmFieldType.STRING);

        boolean nullable = metadata.defaultNullable;
        if (RealmObjectSchema.containsAttribute(attributes, FieldAttribute.REQUIRED)) {
            nullable = false;
        }

        return new RealmObjectSchema(realm, this,
                realm.getSharedRealm().createTableWithPrimaryKey(internalTableName, primaryKeyFieldName,
                        isStringField, nullable));
    }

    /**
     * Removes a class from the Realm. All data will be removed. Removing a class while other classes point
     * to it will throw an {@link IllegalStateException}. Removes those classes or fields first.
     *
     * @param className name of the class to remove.
     */
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

        return new RealmObjectSchema(realm, this, table);
    }

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    public boolean contains(String className) {
        return realm.getSharedRealm().hasTable(Table.getTableNameForClass(className));
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

    private String checkAndGetTableNameFromClassName(String className) {
        String internalTableName = Table.getTableNameForClass(className);
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is too long. Limit is 56 characters: " + className.length());
        }
        return internalTableName;
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
            table = realm.getSharedRealm().getTable(realm.getConfiguration().getSchemaMediator().getTableName(originalClass));
            classToTable.put(originalClass, table);
        }
        if (isProxyClass(originalClass, clazz)) {
            // 'clazz' is the proxy class for 'originalClass'.
            classToTable.put(clazz, table);
        }

        return table;
    }

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
            classSchema = new RealmObjectSchema(realm, this, table, getColumnInfo(originalClass));
            classToSchema.put(originalClass, classSchema);
        }
        if (isProxyClass(originalClass, clazz)) {
            // 'clazz' is the proxy class for 'originalClass'.
            classToSchema.put(clazz, classSchema);
        }

        return classSchema;
    }

    RealmObjectSchema getSchemaForClass(String className) {
        String tableName = Table.getTableNameForClass(className);
        RealmObjectSchema dynamicSchema = dynamicClassToSchema.get(tableName);
        if (dynamicSchema == null) {
            if (!realm.getSharedRealm().hasTable(tableName)) {
                throw new IllegalArgumentException("The class " + className + " doesn't exist in this Realm.");
            }
            dynamicSchema = new RealmObjectSchema(realm, this, realm.getSharedRealm().getTable(tableName));
            dynamicClassToSchema.put(tableName, dynamicSchema);
        }
        return dynamicSchema;
    }

    /**
     * Set the column index cache for this schema.
     *
     * @param columnIndices the column index cache
     */
    final void setInitialColumnIndices(ColumnIndices columnIndices) {
        if (this.columnIndices != null) {
            throw new IllegalStateException("An instance of ColumnIndices is already set.");
        }
        this.columnIndices = new ColumnIndices(columnIndices, true);
    }

    /**
     * Set the column index cache for this schema.
     *
     * @param version the schema version
     * @param columnInfoMap the column info map
     */
    final void setInitialColumnIndices(long version, Map<Pair<Class<? extends RealmModel>, String>, ColumnInfo> columnInfoMap) {
        if (this.columnIndices != null) {
            throw new IllegalStateException("An instance of ColumnIndices is already set.");
        }
        columnIndices = new ColumnIndices(version, columnInfoMap);
    }

    /**
     * Updates all {@link ColumnInfo} elements in {@code columnIndices}.
     * <p>
     * The ColumnInfo elements are shared between all {@link RealmObject}s created by the Realm instance
     * which owns this RealmSchema. Updating them also means updating indices information in those {@link RealmObject}s.
     *
     * @param schemaVersion new schema version.
     */
    void updateColumnIndices(ColumnIndices schemaVersion) {
        columnIndices.copyFrom(schemaVersion);
    }

    final boolean isProxyClass(Class<? extends RealmModel> modelClass, Class<? extends RealmModel> testee) {
        return modelClass.equals(testee);
    }

    /**
     * Sometimes you need ColumnIndicies that can be passed between threads.
     * Setting the mutable flag false creates an instance that is effectively final.
     *
     * @return a new, thread-safe copy of this Schema's ColumnIndices.
     * @see ColumnIndices for the effectively final contract.
     */
    final ColumnIndices getImmutableColumnIndicies() {
        checkIndices();
        return new ColumnIndices(columnIndices, false);
    }

    final boolean haveColumnInfo() {
        return columnIndices != null;
    }

    final long getSchemaVersion() {
        checkIndices();
        return columnIndices.getSchemaVersion();
    }

    final ColumnInfo getColumnInfo(Class<? extends RealmModel> clazz) {
        checkIndices();
        return columnIndices.getColumnInfo(clazz);
    }

    protected final ColumnInfo getColumnInfo(String className) {
        checkIndices();
        return columnIndices.getColumnInfo(className);
    }

    private void checkIndices() {
        if (!haveColumnInfo()) {
            throw new IllegalStateException("Attempt to use column index before set.");
        }
    }
}
