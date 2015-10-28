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

import java.util.Collections;
import java.util.Set;

import io.realm.annotations.Required;
import io.realm.exceptions.RealmException;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;

/**
 * Class for interacting with the schema for a given Realm model class. This makes it possible to
 * add, delete or change the fields for given class.
 *
 * @see io.realm.RealmMigration
 */
public final class RealmObjectSchema {

    private final BaseRealm realm;
    private final Table table;
    private final ImplicitTransaction transaction;

    /**
     * Creates a schema object for a given Realm class.
     *
     * @param realm Realm holding the objects.
     * @param transaction transaction object for the current Realm.
     * @param table table representation of the Realm class
     */
    RealmObjectSchema(BaseRealm realm, ImplicitTransaction transaction, Table table) {
        this.realm = realm;
        this.transaction = transaction;
        this.table = table;
    }

    /**
     * Returns the name of the Realm model class being represented by this schema.
     *
     * When using a normal {@link Realm} this name is the same as the {@link RealmObject} model class.
     * When using a {@link DynamicRealm} this is the name used in all API methods requiring a class name.
     *
     * @return the name of the Realm model class represented by this schema.
     */
    public String getClassName() {
        return table.getName().substring(Table.TABLE_PREFIX.length());
    }

    /**
     * Set a new name of the this class. This is equivalent to renaming it.
     *
     * @param newClassName the new name for this class.
     * @see RealmSchema#renameClass(String, String)
     */
    public void setClassName(String newClassName) {
        checkEmpty(newClassName);
        String internalTableName = Table.TABLE_PREFIX + newClassName;
        if (transaction.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + newClassName);
        }
        transaction.renameTable(table.getName(), internalTableName);
    }

    /**
     * Adds a {@code String} field that is allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addStringField(String fieldName) {
        return addStringField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code String} field.
     *
     * @param fieldName name of field to add.
     * @param modifiers set of modifiers for this field. The field is nullable by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addStringField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = !modifiers.contains(RealmModifier.REQUIRED);
        long columnIndex = table.addColumn(RealmFieldType.STRING, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code short} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addShortField(String fieldName) {
        return addShortField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code short} field.
     *
     * @param fieldName name of field to add.
     * @param modifiers set of modifiers for this field. The field is {@link Required} by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addShortField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = modifiers.contains(RealmModifier.NULLABLE);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code int} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addIntField(String fieldName) {
        return addIntField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code int} field.
     *
     * @param fieldName name of field to add.
     * @param modifiers set of modifiers for this field. The field is {@link Required} by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addIntField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = modifiers.contains(RealmModifier.NULLABLE);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code long} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addLongField(String fieldName) {
        return addLongField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code long} field.
     *
     * @param fieldName name of field to add
     * @param modifiers set of modifiers for this field. The field is {@link Required} by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addLongField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = modifiers.contains(RealmModifier.NULLABLE);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code byte} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addByteField(String fieldName) {
        return addByteField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code byte} field.
     *
     * @param fieldName name of field to add
     * @param modifiers set of modifiers for this field. The field is {@link Required} by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addByteField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = modifiers.contains(RealmModifier.NULLABLE);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }


    /**
     * Adds a {@code boolean} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBooleanField(String fieldName) {
        return addBooleanField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code boolean} field.
     *
     * @param fieldName name of field to add
     * @param modifiers set of modifiers for this field. The field is {@link Required} by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBooleanField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = modifiers.contains(RealmModifier.NULLABLE);
        long columnIndex = table.addColumn(RealmFieldType.BOOLEAN, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code byte[]} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBlobField(String fieldName) {
        return addBlobField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code byte[]} field.
     *
     * @param fieldName name of field to add.
     * @param modifiers set of modifiers for this field. The field is nullable by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBlobField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = !modifiers.contains(RealmModifier.REQUIRED);
        long columnIndex = table.addColumn(RealmFieldType.BINARY, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code float} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addFloatField(String fieldName) {
        return addFloatField(fieldName, Collections.EMPTY_SET);
    }


    /**
     * Adds a {@code float} field.
     *
     * @param fieldName name of field to add.
     * @param modifiers set of modifiers for this field. The field is {@link Required} by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addFloatField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = modifiers.contains(RealmModifier.NULLABLE);
        long columnIndex = table.addColumn(RealmFieldType.FLOAT, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code double} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDoubleField(String fieldName) {
        return addDoubleField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code double} field.
     *
     * @param fieldName name of field to add.
     * @param modifiers set of modifiers for this field. The field is {@link Required} by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDoubleField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = modifiers.contains(RealmModifier.NULLABLE);
        long columnIndex = table.addColumn(RealmFieldType.DOUBLE, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code Date} field that is nullable by default.
     *
     * @param fieldName name of field to add.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDateField(String fieldName) {
        return addDateField(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code Date} field.
     *
     * @param fieldName name of field to add.
     * @param modifiers set of modifiers for this field. The field is nullable by default.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDateField(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        boolean nullable = !modifiers.contains(RealmModifier.REQUIRED);
        long columnIndex = table.addColumn(RealmFieldType.DATE, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a field that links to another Realm object.
     *
     * @param fieldName name of field to add.
     * @param objectSchema {@link RealmObjectSchema} for the object to link to.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addObjectField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.OBJECT, fieldName, transaction.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Adds a field that links to a {@link io.realm.RealmList} of other Realm objects.
     *
     * @param fieldName name of field to add.
     * @param objectSchema {@link RealmObjectSchema} for the object to link to.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addListField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.LIST, fieldName, transaction.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Removes a field from the class.
     *
     * @param fieldName field name to remove.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists.
     */
    public RealmObjectSchema removeField(String fieldName) {
        checkLegalName(fieldName);
        if (!hasField(fieldName)) {
            throw new IllegalStateException(fieldName + " does not exist.");
        }
        long columnIndex = getColumnIndex(fieldName);
        table.removeColumn(columnIndex);
        return this;
    }

    /**
     * Renames a field from one name to another.
     *
     * @param currentFieldName field name to rename.
     * @param newFieldName the new field name.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or if the new field name already
     * exists.
     */
    public RealmObjectSchema renameField(String currentFieldName, String newFieldName) {
        checkLegalName(currentFieldName);
        checkFieldExists(currentFieldName);
        checkLegalName(newFieldName);
        checkFieldNameIsAvailable(newFieldName);
        long columnIndex = getColumnIndex(currentFieldName);
        table.renameColumn(columnIndex, newFieldName);
        return this;
    }

    /**
     * Tests if the schema has field defined with the given name.
     *
     * @param fieldName field name to test.
     * @return {@code true} if the field exists, {@code false} otherwise.
     */
    public boolean hasField(String fieldName) {
        return table.getColumnIndex(fieldName) != TableOrView.NO_MATCH;
    }

    /**
     * Adds a index to a given field. This is the same as adding the {@code @Index} annotation on the field.
     *
     * @param fieldName field to add name to rename.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists, the field cannot be indexed or it already has a
     * index defined.
     */
    public RealmObjectSchema addIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (table.hasSearchIndex(columnIndex)) {
            throw new IllegalStateException(fieldName  + " already has an index.");
        }
        table.addSearchIndex(columnIndex);
        return this;
    }

    /**
     * Checks if a given field has a {@link io.realm.annotations.Index} defined.
     *
     * @param fieldName existing field name to check.
     * @return {@code true} if field is indexed. {@false otherwise}.
     */
    public boolean hasIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        return table.hasSearchIndex(table.getColumnIndex(fieldName));
    }


    /**
     * Removes an index from a given field. This is the same as removing the {@code @Index} annotation on the field.
     *
     * @param fieldName field to remove index from.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or the field doesn't have an index.
     */
    public RealmObjectSchema removeIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (!table.hasSearchIndex(columnIndex)) {
            throw new IllegalStateException("Field is not indexed: " + fieldName);
        }
        table.removeSearchIndex(columnIndex);
        return this;
    }

    /**
     * Adds a primary key to a given field. This is the same as adding the {@code @PrimaryKey} annotation on the field.
     *
     * @param fieldName field to add name to rename.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists, the field cannot be a primary key or it already
     * has a primary key defined.
     */
    public RealmObjectSchema addPrimaryKey(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        if (table.hasPrimaryKey()) {
            throw new IllegalStateException("A primary key is already defined");
        }
        table.setPrimaryKey(fieldName);
        return this;
    }

    /**
     * Removes the primary key from this class. This is the same as removing the {@code @PrimaryKey} annotation from the
     * class.
     *
     * @return the updated schema.
     * @throws IllegalArgumentException if the class doesn't have a primary key defined.
     */
    public RealmObjectSchema removePrimaryKey() {
        if (!table.hasPrimaryKey()) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        table.setPrimaryKey("");
        return this;
    }

    /**
     * Sets a field to be required, i.e. not allowed to hold {@code null values}.
     *
     * @param fieldName name of field in the schema.
     * @param required {@code true} if field should be required, {@false otherwise}.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or the field already has the given required flag.
     */
    public RealmObjectSchema setRequired(String fieldName, boolean required) {
        long columnIndex = table.getColumnIndex(fieldName);
        boolean currentColumnRequired = isRequired(fieldName);
        if (required && currentColumnRequired) {
            throw new IllegalStateException("Current field is already marked as Required: " + fieldName);
        }
        if (!required && !currentColumnRequired) {
            throw new IllegalStateException("Current field is already marked as not Required: " + fieldName);
        }

        if (required) {
            table.convertColumnToNotNullable(columnIndex);
        } else {
            table.convertColumnToNullable(columnIndex);
        }
        return this;
    }

    /**
     * Checks if a given field is required, i.e. is not allowed to contain {@code null} values.
     *
     * @param fieldName field to check.
     * @return {@code true} if it is requied, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exists.
     * @see Required
     */
    public boolean isRequired(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        return !table.isColumnNullable(columnIndex);
    }

    /**
     * Checks if a given field is nullable, i.e. is allowed to contain {@code null} values.
     *
     * @param fieldName field to check.
     * @return {@code true} if it is requied, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exists.
     * @see Required
     */
    public boolean isNullable(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        return table.isColumnNullable(columnIndex);
    }

    /**
     * Checks if the object has a primary key defined.
     *
     * @return {@code true} if a primary key is defined, {@code false} otherwise.
     * @see io.realm.annotations.PrimaryKey
     */
    public boolean hasPrimaryKey() {
        return table.hasPrimaryKey();
    }

    /**
     * Return all fields in this schema.
     *
     * @return A list of all the fields in this schema.
     */
    public String[] getFieldNames() {
        int columns = (int) table.getColumnCount();
        String[] columnNames = new String[columns];
        for (int i = 0; i < columns; i++) {
            columnNames[i] = table.getColumnName(i);
        }
        return columnNames;
    }

    /**
     * Iterate each object in the Realm that have the type defined by this schema. The order is
     * undefined.
     *
     * @return This schema.
     */
    public RealmObjectSchema forEach(Iterator iterator) {
        if (iterator != null) {
            long size = table.size();
            for (long i = 0; i < size; i++) {
                iterator.next(new DynamicRealmObject(realm, table.getCheckedRow(i)));
            }
        }

        return this;
    }

    // Invariant: Field was just added
    // TODO: Refactor to avoid 4xsearches.
    private void addModifiers(long columnIndex, Set<RealmModifier> modifiers) {
        if (modifiers != null && modifiers.size() > 0) {
            if (modifiers.contains(RealmModifier.INDEXED)) {
                table.addSearchIndex(columnIndex);
            }

            if (modifiers.contains(RealmModifier.PRIMARY_KEY)) {
                table.setPrimaryKey(columnIndex);
                table.addSearchIndex(columnIndex);
            }
        }
    }

    private void checkLegalName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name must not be null or empty");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Field name must not contain '.'");
        }
    }

    private void checkFieldNameIsAvailable(String fieldName) {
        if (table.getColumnIndex(fieldName) != TableOrView.NO_MATCH) {
            throw new IllegalArgumentException("Field already exist in '" + getClassName() + "': " + fieldName);
        }
    }

    private void checkFieldExists(String fieldName) {
        if (table.getColumnIndex(fieldName) == TableOrView.NO_MATCH) {
            throw new IllegalArgumentException("Field name doesn't exist on object '" + getClassName() + "': " + fieldName);
        }
    }

    private long getColumnIndex(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(
                    String.format("Fieldname '%s' does not exist on schema for '%s",
                            fieldName, getClassName()
                    ));
        }
        return columnIndex;
    }

    private void checkEmpty(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Null or empty class names are not allowed");
        }
    }

    private void checkLegalModifiers(long columnIndex, Set<RealmModifier> modifiers) {
        if (modifiers.contains(RealmModifier.REQUIRED) && modifiers.contains(RealmModifier.NULLABLE)) {
            throw new RealmException(table.getColumnName(columnIndex) + " cannot be both @Required and nullable at " +
                    "the same time.");
        }
    }

    /**
     * Iterator interface for traversing all objects with the current schema.
     *
     * @see #forEach(Iterator)
     */
    public interface Iterator {
        void next(DynamicRealmObject obj);
    }
}


