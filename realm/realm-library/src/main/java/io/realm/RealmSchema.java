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

import java.util.HashSet;
import java.util.Set;

import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Table;

/**
 * Class for interacting with the Realm schema using a dynamic API. This makes it possible
 * to add, delete and change the classes in the Relm.
 *
 * All changes must happen inside a write transaction for that Realm.
 *
 * @see io.realm.RealmMigration
 */
public final class RealmSchema {

    private static final String TABLE_PREFIX = "class_"; // TODO Move to JNI Object store layer
    private static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";
    private final ImplicitTransaction transaction;
    private final BaseRealm realm;

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    public RealmSchema(BaseRealm realm, ImplicitTransaction transaction) {
        this.realm = realm;
        this.transaction = transaction;
    }

    /**
     * Returns the Realm schema for a given class.
     *
     * @param className Name of the class
     * @return Schema object for that class or {@code null} if the class doesn't exists.
     *
     */
    public RealmObjectSchema getObjectSchema(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalClassName = TABLE_PREFIX + className;
        if (transaction.hasTable(internalClassName)) {
            return new RealmObjectSchema(realm, transaction, transaction.getTable(internalClassName));
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link RealmObjectSchema} for all model classes that can be saved in this Realm.
     *
     * @return The set of all classes in this Realm or no model classes can be saved in the Realm.
     */
    public Set<RealmObjectSchema> getAllClasses() {
        int tables = (int) transaction.size();
        Set<RealmObjectSchema> schemas = new HashSet<RealmObjectSchema>(tables);
        for (int i = 0; i < tables; i++) {
            String tableName = transaction.getTableName(i);
            if (Table.isMetaTable(tableName)) {
                continue;
            }
            Table table = transaction.getTable(tableName);
            schemas.add(new RealmObjectSchema(realm, transaction, table));
        }
        return schemas;
    }

    /**
     * Adds a new class to the Realm.
     *
     * @param className Name of the class.
     * @return A Realm schema object for that class.
     */
    public RealmObjectSchema createClass(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalTableName = TABLE_PREFIX + className;
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is to long. Limit is 57 characters: " + className.length());
        }
        if (transaction.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        Table table = transaction.getTable(internalTableName);
        return new RealmObjectSchema(realm, transaction, table);
    }

    /**
     * Removes a class from the Realm. All data will be removed. Removing a class while other classes point
     * to it will throw an {@link io.realm.exceptions.RealmException}. Remove those classes or fields first.
     *
     * @param className Name of the class to remove.
     */
    public void removeClass(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalTableName = TABLE_PREFIX + className;
        checkHasTable(className, "Cannot remove class because it is not in this Realm: " + className);
        transaction.removeTable(internalTableName);
    }

    /**
     * Renames a class already in the Realm.
     *
     * @param oldName Old class name.
     * @param newName New class name.
     * @return A schema object for renamed class.
     */
    public RealmObjectSchema renameClass(String oldName, String newName) {
        checkEmpty(oldName, "Class names cannot be empty or null");
        checkEmpty(newName, "Class names cannot be empty or null");
        String oldInternalName = TABLE_PREFIX + oldName;
        String newInternalName = TABLE_PREFIX + newName;
        checkHasTable(oldName, "Cannot rename class because it doesn't exist in this Realm: " + oldName);
        if (transaction.hasTable(newInternalName)) {
            throw new IllegalArgumentException(oldName + " cannot be renamed because the new class already exists: " + newName);
        }
        transaction.renameTable(oldInternalName, newInternalName);
        return new RealmObjectSchema(realm, transaction, transaction.getTable(newInternalName));
    }

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className Class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    public boolean hasClass(String className) {
        return transaction.hasTable(Table.TABLE_PREFIX + className);
    }

    private void checkEmpty(String str, String error) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    private void checkHasTable(String className, String errorMsg) {
        String internalTableName = TABLE_PREFIX + className;
        if (!transaction.hasTable(internalTableName)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
