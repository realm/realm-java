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

import javax.annotation.Nonnull;

import io.realm.internal.OsObjectStore;
import io.realm.internal.Table;
import io.realm.internal.fields.FieldDescriptor;

/**
 * Mutable {@link RealmObjectSchema}.
 */
class MutableRealmObjectSchema extends RealmObjectSchema {

    /**
     * Creates a mutable schema object for a given Realm class.
     *
     * @param realm Realm holding the objects.
     * @param table table representation of the Realm class
     */
    MutableRealmObjectSchema(BaseRealm realm, RealmSchema schema, Table table) {
        super(realm, schema, table, new DynamicColumnIndices(table));
    }

    @Override
    public RealmObjectSchema setClassName(String className) {
        realm.checkNotInSync(); // renaming a table is not permitted
        checkEmpty(className);
        String internalTableName = Table.getTableNameForClass(className);
        if (className.length() > Table.CLASS_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "Class name is too long. Limit is %1$d characters: \'%2$s\' (%3$d)",
                    Table.CLASS_NAME_MAX_LENGTH, className, className.length()));
        }
        //noinspection ConstantConditions
        if (realm.sharedRealm.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        // in case this table has a primary key, we need to transfer it after renaming the table.
        //noinspection ConstantConditions
        @Nonnull String oldTableName = table.getName();
        @Nonnull String oldClassName = table.getClassName();
        String pkField = OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, oldClassName);
        if (pkField != null) {
            OsObjectStore.setPrimaryKeyForObject(realm.sharedRealm, oldClassName, null);
        }
        realm.sharedRealm.renameTable(oldTableName, internalTableName);
        if (pkField != null) {
            try {
                OsObjectStore.setPrimaryKeyForObject(realm.sharedRealm, className, pkField);
            } catch (Exception e) {
                // revert the table name back when something goes wrong
                //noinspection ConstantConditions
                realm.sharedRealm.renameTable(table.getName(), oldTableName);
                throw e;
            }
        }
        return this;
    }

    private void checkEmpty(String str) {
        //noinspection ConstantConditions
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Null or empty class names are not allowed");
        }
    }

    @Override
    public RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes) {
        FieldMetaData metadata = SUPPORTED_SIMPLE_FIELDS.get(fieldType);
        if (metadata == null) {
            if (SUPPORTED_LINKED_FIELDS.containsKey(fieldType)) {
                throw new IllegalArgumentException("Use addRealmObjectField() instead to add fields that link to other RealmObjects: " + fieldName);
            } else if (RealmModel.class.isAssignableFrom(fieldType)) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "Use 'addRealmObjectField()' instead to add fields that link to other RealmObjects: %s(%s)",
                        fieldName, fieldType));
            } else {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "Realm doesn't support this field type: %s(%s)",
                        fieldName, fieldType));
            }
        }

        if (containsAttribute(attributes, FieldAttribute.PRIMARY_KEY)) {
            checkAddPrimaryKeyForSync();
        }

        checkNewFieldName(fieldName);
        boolean nullable = metadata.defaultNullable;
        if (containsAttribute(attributes, FieldAttribute.REQUIRED)) {
            nullable = false;
        }

        long columnIndex = table.addColumn(metadata.fieldType, fieldName, nullable);
        try {
            addModifiers(fieldName, attributes);
        } catch (Exception e) {
            // Modifiers have been removed by the addModifiers method()
            table.removeColumn(columnIndex);
            throw e;
        }
        return this;
    }

    @Override
    public RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.OBJECT, fieldName, realm.sharedRealm.getTable(Table.getTableNameForClass(objectSchema.getClassName())));
        return this;
    }

    @Override
    public RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.LIST, fieldName, realm.sharedRealm.getTable(Table.getTableNameForClass(objectSchema.getClassName())));
        return this;
    }

    @Override
    public RealmObjectSchema addRealmListField(String fieldName, Class<?> primitiveType) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);

        FieldMetaData metadata = SUPPORTED_SIMPLE_FIELDS.get(primitiveType);
        if (metadata == null) {
            if (primitiveType.equals(RealmObjectSchema.class) || RealmModel.class.isAssignableFrom(primitiveType)) {
                throw new IllegalArgumentException("Use 'addRealmListField(String name, RealmObjectSchema schema)' instead to add lists that link to other RealmObjects: " + fieldName);
            } else {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "RealmList does not support lists with this type: %s(%s)",
                        fieldName, primitiveType));
            }
        }
        table.addColumn(metadata.listType, fieldName, metadata.defaultNullable);
        return this;
    }

    @Override
    public RealmObjectSchema removeField(String fieldName) {
        realm.checkNotInSync(); // destructive modification of a schema is not permitted
        checkLegalName(fieldName);
        if (!hasField(fieldName)) {
            throw new IllegalStateException(fieldName + " does not exist.");
        }
        long columnIndex = getColumnIndex(fieldName);
        String className = getClassName();
        if (fieldName.equals(OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, className))) {
            OsObjectStore.setPrimaryKeyForObject(realm.sharedRealm, className, fieldName);
        }
        table.removeColumn(columnIndex);
        return this;
    }

    @Override
    public RealmObjectSchema renameField(String currentFieldName, String newFieldName) {
        realm.checkNotInSync(); // destructive modification of a schema is not permitted
        checkLegalName(currentFieldName);
        checkFieldExists(currentFieldName);
        checkLegalName(newFieldName);
        checkFieldNameIsAvailable(newFieldName);
        long columnIndex = getColumnIndex(currentFieldName);
        table.renameColumn(columnIndex, newFieldName);

        // ATTENTION: We don't need to re-set the PK table here since the column index won't be changed when renaming.

        return this;
    }

    @Override
    public RealmObjectSchema addIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (table.hasSearchIndex(columnIndex)) {
            throw new IllegalStateException(fieldName + " already has an index.");
        }
        table.addSearchIndex(columnIndex);
        return this;
    }

    @Override
    public RealmObjectSchema removeIndex(String fieldName) {
        realm.checkNotInSync(); // Destructive modifications are not permitted.
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (!table.hasSearchIndex(columnIndex)) {
            throw new IllegalStateException("Field is not indexed: " + fieldName);
        }
        table.removeSearchIndex(columnIndex);
        return this;
    }

    @Override
    public RealmObjectSchema addPrimaryKey(String fieldName) {
        checkAddPrimaryKeyForSync();
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        String currentPKField = OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName());
        if (currentPKField != null) {
            throw new IllegalStateException(
                    String.format(Locale.ENGLISH, "Field '%s' has been already defined as primary key.",
                            currentPKField));
        }
        long columnIndex = getColumnIndex(fieldName);
        if (!table.hasSearchIndex(columnIndex)) {
            // No exception will be thrown since adding PrimaryKey implies the column has an index.
            table.addSearchIndex(columnIndex);
        }
        OsObjectStore.setPrimaryKeyForObject(realm.sharedRealm, getClassName(), fieldName);
        return this;
    }

    @Override
    public RealmObjectSchema removePrimaryKey() {
        realm.checkNotInSync(); // Destructive modifications are not permitted.
        String pkField = OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName());
        if (pkField == null) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        long columnIndex = table.getColumnIndex(pkField);
        if (table.hasSearchIndex(columnIndex)) {
            table.removeSearchIndex(columnIndex);
        }
        OsObjectStore.setPrimaryKeyForObject(realm.sharedRealm, getClassName(), null);
        return this;
    }

    @Override
    public RealmObjectSchema setRequired(String fieldName, boolean required) {
        long columnIndex = table.getColumnIndex(fieldName);
        boolean currentColumnRequired = isRequired(fieldName);
        RealmFieldType type = table.getColumnType(columnIndex);

        if (type == RealmFieldType.OBJECT) {
            throw new IllegalArgumentException("Cannot modify the required state for RealmObject references: " + fieldName);
        }
        if (type == RealmFieldType.LIST) {
            throw new IllegalArgumentException("Cannot modify the required state for RealmList references: " + fieldName);
        }
        if (required && currentColumnRequired) {
            throw new IllegalStateException("Field is already required: " + fieldName);
        }
        if (!required && !currentColumnRequired) {
            throw new IllegalStateException("Field is already nullable: " + fieldName);
        }

        if (required) {
            table.convertColumnToNotNullable(columnIndex);
        } else {
            table.convertColumnToNullable(columnIndex);
        }
        return this;
    }

    @Override
    public RealmObjectSchema setNullable(String fieldName, boolean nullable) {
        setRequired(fieldName, !nullable);
        return this;
    }

    @Override
    public RealmObjectSchema transform(Function function) {
        //noinspection ConstantConditions
        if (function != null) {
            long size = table.size();
            for (long i = 0; i < size; i++) {
                function.apply(new DynamicRealmObject(realm, table.getCheckedRow(i)));
            }
        }

        return this;
    }

    /**
     * Returns a field descriptor based on the internal field names found in the Realm file.
     *
     * @param internalColumnNameDescription internal column name or internal linked column name description.
     * @param validColumnTypes valid field type for the last field in a linked field
     * @return the corresponding FieldDescriptor.
     * @throws IllegalArgumentException if a proper FieldDescriptor could not be created.
     */
    @Override
    FieldDescriptor getColumnIndices(String internalColumnNameDescription, RealmFieldType... validColumnTypes) {
        return FieldDescriptor.createStandardFieldDescriptor(getSchemaConnector(), getTable(), internalColumnNameDescription, validColumnTypes);
    }

    // Invariant: Field was just added. This method is responsible for cleaning up attributes if it fails.
    private void addModifiers(String fieldName, FieldAttribute[] attributes) {
        boolean indexAdded = false;
        try {
            //noinspection ConstantConditions
            if (attributes != null && attributes.length > 0) {
                if (containsAttribute(attributes, FieldAttribute.INDEXED)) {
                    addIndex(fieldName);
                    indexAdded = true;
                }

                if (containsAttribute(attributes, FieldAttribute.PRIMARY_KEY)) {
                    // Note : adding primary key implies application of FieldAttribute.INDEXED attribute.
                    addPrimaryKey(fieldName);
                    indexAdded = true;
                }

                // REQUIRED is being handled when adding the column using addField through the nullable parameter.
            }
        } catch (Exception e) {
            // If something went wrong, revert all attributes.
            long columnIndex = getColumnIndex(fieldName);
            if (indexAdded) {
                table.removeSearchIndex(columnIndex);
            }
            throw (RuntimeException) e;
        }
    }

    static boolean containsAttribute(FieldAttribute[] attributeList, FieldAttribute attribute) {
        //noinspection ConstantConditions
        if (attributeList == null || attributeList.length == 0) {
            return false;
        }
        for (FieldAttribute anAttributeList : attributeList) {
            if (anAttributeList == attribute) {
                return true;
            }
        }
        return false;
    }

    private void checkNewFieldName(String fieldName) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
    }

    private void checkFieldNameIsAvailable(String fieldName) {
        if (table.getColumnIndex(fieldName) != Table.NO_MATCH) {
            throw new IllegalArgumentException("Field already exists in '" + getClassName() + "': " + fieldName);
        }
    }

    private void checkAddPrimaryKeyForSync() {
        if (realm.configuration.isSyncConfiguration()) {
            throw new UnsupportedOperationException("'addPrimaryKey' is not supported by synced Realms.");
        }
    }
}
