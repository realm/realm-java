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

import io.realm.internal.Table;

/**
 * Mutable {@link RealmSchema}.
 */
class MutableRealmSchema extends RealmSchema {

    MutableRealmSchema(BaseRealm realm) {
        super(realm);
    }

    @Override
    public RealmObjectSchema get(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);

        String internalClassName = Table.getTableNameForClass(className);
        if (!realm.getSharedRealm().hasTable(internalClassName)) { return null; }
        Table table = realm.getSharedRealm().getTable(internalClassName);
        return new MutableRealmObjectSchema(realm, this, table);
    }

    @Override
    public RealmObjectSchema create(String className) {
        // Adding a class is always permitted.
        checkEmpty(className, EMPTY_STRING_MSG);

        String internalTableName = Table.getTableNameForClass(className);
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is too long. Limit is 56 characters: " + className.length());
        }
        return new MutableRealmObjectSchema(realm, this, realm.getSharedRealm().createTable(internalTableName));
    }

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

        return new MutableRealmObjectSchema(realm, this, table);
    }
}
