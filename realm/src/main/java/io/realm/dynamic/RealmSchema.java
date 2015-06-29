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

package io.realm.dynamic;

import io.realm.Realm;
import io.realm.exceptions.RealmException;
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
    public static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";
    private final ImplicitTransaction transaction;
    private final Realm realm;

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    public RealmSchema(Realm realm, ImplicitTransaction transaction) {
        this.realm = realm;
        this.transaction = transaction;
    }

    /**
     * Returns the Realm schema for a given class.
     *
     * @param className Name of the class
     * @return Schema object for that class
     * @throws RuntimeException if class isn't in this Realm
     */
    public RealmObjectSchema getClass(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalClassName = TABLE_PREFIX + className;
        if (transaction.hasTable(internalClassName)) {
            return new RealmObjectSchema(realm, transaction, transaction.getTable(internalClassName));
        } else {
            throw new IllegalArgumentException("Class does not exist in this Realm: " + className);
        }
    }

    /**
     * Adds a new class to the Realm.
     *
     * @param className Name of the class.
     * @return A Realm schema object for that class.
     */
    public RealmObjectSchema addClass(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalTableName = TABLE_PREFIX + className;
        if (transaction.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        Table table = transaction.getTable(TABLE_PREFIX + className);
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
        try {
            transaction.removeTable(internalTableName);
        } catch (RuntimeException e) {
            throw new RealmException("Class is referenced by other classes. Remove those first.");
        }
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
