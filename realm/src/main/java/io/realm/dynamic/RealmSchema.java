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

package io.realm.dynamic;

import org.jetbrains.annotations.NotNull;

import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;

/**
 * Class for interacting with the Realm schema using a dynamic API. This makes it possible
 * to add, delete and change the types of RealmObjects known to this Realm.
 *
 * All changes must happen inside a write transaction for that Realm.
 *
 * @see io.realm.RealmMigration
 */
public class RealmSchema {

    private static final String TABLE_PREFIX = "class_"; // TODO Move to JNI Object store layer
    public static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";
    private final ImplicitTransaction realm;

    /**
     * Creates a wrapper to easily manipulate the current schema of a Realm.
     */
    public RealmSchema(ImplicitTransaction realm) {
        this.realm = realm;
    }

    /**
     * Returns the RealmClass schema for a given class.
     *
     * @param className Name of the class
     * @return Schema object for that class
     * @throws {@link RuntimeException} if class isn't in this Realm
     */
    public RealmObjectSchema getClass(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalClassName = TABLE_PREFIX + className;
        if (realm.hasTable(internalClassName)) {
            return new RealmObjectSchema(realm, realm.getTable(internalClassName));
        } else {
            throw new IllegalArgumentException("Class does not exist in this Realm: " + className);
        }
    }

    /**
     * Add a new class to the Realm.
     *
     * @param className Name of the class.
     * @return A schema object for that class.
     */
    public RealmObjectSchema addClass(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        String internalTableName = TABLE_PREFIX + className;
        if (realm.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        Table table = realm.getTable(TABLE_PREFIX + className);
        return new RealmObjectSchema(realm, table);
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
        if (!realm.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class doesn't exist in this Realm: " + className);
        }
        realm.removeTable(internalTableName);
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
        if (realm.hasTable(newInternalName)) {
            throw new IllegalArgumentException(oldName + " cannot be renamed because the new class already exists: " + newName);
        }
        realm.renameTable(oldInternalName, newInternalName);
        return new RealmObjectSchema(realm, realm.getTable(newInternalName));
    }

    private void checkEmpty(String str, String error) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }
}
