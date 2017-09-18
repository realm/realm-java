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

import java.util.Locale;

import io.realm.internal.Table;

/**
 * Mutable {@link RealmSchema}.
 */
class MutableRealmSchema extends RealmSchema {

    MutableRealmSchema(BaseRealm realm) {
        super(realm, null);
    }

    @Override
    public RealmObjectSchema get(String className) {
        checkNotEmpty(className, EMPTY_STRING_MSG);

        String internalClassName = Table.getTableNameForClass(className);
        if (!realm.getSharedRealm().hasTable(internalClassName)) { return null; }
        Table table = realm.getSharedRealm().getTable(internalClassName);
        return new MutableRealmObjectSchema(realm, this, table);
    }

    @Override
    public RealmObjectSchema create(String className) {
        // Adding a class is always permitted.
        checkNotEmpty(className, EMPTY_STRING_MSG);

        String internalTableName = Table.getTableNameForClass(className);
        if (className.length() > Table.CLASS_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(Locale.US,
                            "Class name is too long. Limit is %1$d characters: %2$s",
                            Table.CLASS_NAME_MAX_LENGTH,
                            className.length()));
        }
        return new MutableRealmObjectSchema(realm, this, realm.getSharedRealm().createTable(internalTableName));
    }

    @Override
    public RealmObjectSchema createWithPrimaryKeyField(String className, String primaryKeyFieldName, Class<?> fieldType,
                                                       FieldAttribute... attributes) {
        checkNotEmpty(className, EMPTY_STRING_MSG);
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
        if (MutableRealmObjectSchema.containsAttribute(attributes, FieldAttribute.REQUIRED)) {
            nullable = false;
        }

        return new MutableRealmObjectSchema(realm, this,
                realm.getSharedRealm().createTableWithPrimaryKey(internalTableName, primaryKeyFieldName,
                        isStringField, nullable));
    }

    @Override
    public void remove(String className) {
        realm.checkNotInSync(); // Destructive modifications are not permitted.
        checkNotEmpty(className, EMPTY_STRING_MSG);
        String internalTableName = Table.getTableNameForClass(className);
        checkHasTable(className, "Cannot remove class because it is not in this Realm: " + className);
        Table table = getTable(className);
        if (table.hasPrimaryKey()) {
            table.setPrimaryKey(null);
        }
        realm.getSharedRealm().removeTable(internalTableName);
        removeFromClassNameToSchemaMap(internalTableName);
    }

    @Override
    public RealmObjectSchema rename(String oldClassName, String newClassName) {
        realm.checkNotInSync(); // Destructive modifications are not permitted.
        checkNotEmpty(oldClassName, "Class names cannot be empty or null");
        checkNotEmpty(newClassName, "Class names cannot be empty or null");
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

        RealmObjectSchema objectSchema = removeFromClassNameToSchemaMap(oldInternalName);
        if (objectSchema == null || !objectSchema.getTable().isValid() || !objectSchema.getClassName().equals(newClassName)) {
            objectSchema = new MutableRealmObjectSchema(realm, this, table);
        }
        putToClassNameToSchemaMap(newInternalName, objectSchema);

        return objectSchema;
    }

    private String checkAndGetTableNameFromClassName(String className) {
        if (className.length() > Table.CLASS_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(Locale.US,
                            "Class name is too long. Limit is %1$d characters: %2$s",
                            Table.CLASS_NAME_MAX_LENGTH,
                            className.length()));
        }
        return Table.getTableNameForClass(className);
    }
}
