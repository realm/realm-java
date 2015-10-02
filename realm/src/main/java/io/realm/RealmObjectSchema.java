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
     * @param realm Realm holding the objects.
     * @param transaction Transaction object for the current Realm.
     * @param table Table representation of the Realm class
     */
    RealmObjectSchema(BaseRealm realm, ImplicitTransaction transaction, Table table) {
        this.realm = realm;
        this.transaction = transaction;
        this.table = table;
    }

    /**
     * Returns the name of the Realm model class being represented by this schema.
     *
     * When using a normal {@link Realm} this name is normally the name of the {@link RealmObject} model class.
     * When using the {@link DynamicRealm} this is the name used in all API methods requiring a class name.
     *
     * @return The name of the Realm model class represented by this schema.
     */
    public String getClassName() {
        return table.getName().substring(Table.TABLE_PREFIX.length());
    }

    /**
     * Set a new name of the this class (= Renaming it).
     * @param newClasName Set the new name for this Realm class.
     *
     */
    public void setClassName(String newClasName) {
        checkEmpty(newClasName);
        String internalTableName = Table.TABLE_PREFIX + newClasName;
        if (transaction.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + newClasName);
        }
        transaction.renameTable(table.getName(), internalTableName);
    }

    /**
     * Adds a {@code String} field that is allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addString(String fieldName) {
        return addString(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code String} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addString(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.STRING, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code short} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addShort(String fieldName) {
        return addShort(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code short} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addShort(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code int} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addInt(String fieldName) {
        return addInt(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code int} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addInt(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code long} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addLong(String fieldName) {
        return addLong(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code long} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addLong(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code byte} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addByte(String fieldName) {
        return addByte(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code byte} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addByte(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.INTEGER, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }


    /**
     * Adds a {@code boolean} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBoolean(String fieldName) {
        return addBoolean(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code boolean} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBoolean(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.BOOLEAN, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code byte[]} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBlob(String fieldName) {
        return addBlob(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code byte[]} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addBlob(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.BINARY, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code float} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addFloat(String fieldName) {
        return addFloat(fieldName, Collections.EMPTY_SET);
    }


    /**
     * Adds a {@code float} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addFloat(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.FLOAT, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code double} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDouble(String fieldName) {
        return addDouble(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code double} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDouble(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.DOUBLE, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a {@code Date} field that is not allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDate(String fieldName) {
        return addDate(fieldName, Collections.EMPTY_SET);
    }

    /**
     * Adds a {@code Date} field.
     *
     * @param fieldName Name of field to add
     * @param modifiers Set of modifiers for this field.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addDate(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(RealmFieldType.DATE, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a field that links to another Realm object.
     *
     * @param fieldName Name of field to add.
     * @param objectSchema {@link RealmObjectSchema} for the object to link to.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addObject(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.OBJECT, fieldName, transaction.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Adds a field that links to a {@link io.realm.RealmList} of other Realm objects.
     *
     * @param fieldName Name of field to add
     * @param objectSchema {@link RealmObjectSchema} for the object to link to.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addList(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.LIST, fieldName, transaction.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Removes a field from the class.
     *
     * @param fieldName Field name to remove
     * @return The updated schema
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
     * @param currentFieldName Field name to rename.
     * @param newFieldName The new field name.
     * @return The updated schema.
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
     * @param fieldName Field name to test.
     * @return {@code true} if the field exists, {@code false} otherwise.
     */
    public boolean hasField(String fieldName) {
        return table.getColumnIndex(fieldName) != TableOrView.NO_MATCH;
    }

    /**
     * Adds a index to a given field. This is the same as adding the {@code @Index} annotation on the field.
     *
     * @param fieldName Field to add name to rename.
     * @return The updated schema.
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
     * @param fieldName Existing field name to check.
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
     * @param fieldName Field to remove index from.
     * @return The updated schema.
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
     * @param fieldName Field to add name to rename.
     * @return The updated schema.
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
     * @return The updated schema.
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
     * Sets a field to be nullable.
     *
     * @param fieldName Name of field in the schema
     * @param nullable {@code true} if field should be nullable. {@false otherwise}.
     * @return The updated schema.
     * @throws IllegalArgumentException
     */
    public RealmObjectSchema setNullable(String fieldName, boolean nullable) {
        throw new RealmException("Waiting for Null support");
    }

    /**
     * Checks if a given field is allowed to contain {@code null} values.
     * @param fieldName Field to check.
     * @return {@code true} if it can have {@code null} values. {@code false} otherwise.
     */
    public boolean isNullable(String fieldName) {
        throw new RealmException("Waiting for Null support");
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
     * Iterate each object in the Realm that have the typed defined by this schema. The order is
     * undefined.
     *
     * @return This schema.
     */
    public RealmObjectSchema forEach(Iterator iterator) {
        if (iterator != null) {
            long size = table.size();
            for (long i = 0; i < size; i++) {
                // TODO Consider reusing the object. Benchmark difference
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
            }

            if (modifiers.contains(RealmModifier.NON_NULLABLE) || modifiers.contains(RealmModifier.NULLABLE)) {
                throw new RealmException("Null not support yet, so neither is changing state");
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

    /**
     * Iterator interface for traversing all objects with the current schema.
     *
     * @see #forEach(Iterator)
     */
    public interface Iterator {
        void next(DynamicRealmObject obj);
    }
}


