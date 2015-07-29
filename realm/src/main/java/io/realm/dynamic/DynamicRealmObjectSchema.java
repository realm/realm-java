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

import java.util.Collections;
import java.util.Set;

import io.realm.Realm;
import io.realm.exceptions.RealmException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;

/**
 * Class for interacting with a schema for a given RealmClass. This makes it possible to add, delete
 * or change the fields or objects for given class.
 *
 * This is used when migrating between different versions of a Realm.
 *
 * @see io.realm.RealmMigration
 */
public final class DynamicRealmObjectSchema {

    private final Realm realm;
    private final Table table;
    private final ImplicitTransaction transaction;

    /**
     * Creates a schema object for a given Realm class.
     * @param realm Realm holding the objects.
     * @param transaction Transaction object for the current Realm.
     * @param table Table representation of the Realm class
     */
    DynamicRealmObjectSchema(Realm realm, ImplicitTransaction transaction, Table table) {
        this.realm = realm;
        this.transaction = transaction;
        this.table = table;
    }

    /**
     * Adds a {@code String} field that is allowed to contain {@code null} values.
     *
     * @param fieldName Name of field to add
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public DynamicRealmObjectSchema addString(String fieldName) {
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
    public DynamicRealmObjectSchema addString(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.STRING, fieldName);
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
    public DynamicRealmObjectSchema addShort(String fieldName) {
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
    public DynamicRealmObjectSchema addShort(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
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
    public DynamicRealmObjectSchema addInt(String fieldName) {
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
    public DynamicRealmObjectSchema addInt(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
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
    public DynamicRealmObjectSchema addLong(String fieldName) {
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
    public DynamicRealmObjectSchema addLong(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
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
    public DynamicRealmObjectSchema addByte(String fieldName) {
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
    public DynamicRealmObjectSchema addByte(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
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
    public DynamicRealmObjectSchema addBoolean(String fieldName) {
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
    public DynamicRealmObjectSchema addBoolean(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.BOOLEAN, fieldName);
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
    public DynamicRealmObjectSchema addBlob(String fieldName) {
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
    public DynamicRealmObjectSchema addBlob(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.BINARY, fieldName);
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
    public DynamicRealmObjectSchema addFloat(String fieldName) {
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
    public DynamicRealmObjectSchema addFloat(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.FLOAT, fieldName);
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
    public DynamicRealmObjectSchema addDouble(String fieldName) {
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
    public DynamicRealmObjectSchema addDouble(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.DOUBLE, fieldName);
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
    public DynamicRealmObjectSchema addDate(String fieldName) {
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
    public DynamicRealmObjectSchema addDate(String fieldName, Set<RealmModifier> modifiers) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.DATE, fieldName);
        addModifiers(columnIndex, modifiers);
        return this;
    }

    /**
     * Adds a field that links to another Realm object.
     *
     * @param fieldName Name of field to add.
     * @param objectSchema {@link DynamicRealmObjectSchema} for the object to link to.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public DynamicRealmObjectSchema addObject(String fieldName, DynamicRealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(ColumnType.LINK, fieldName, transaction.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Adds a field that links to a {@link io.realm.RealmList} of other Realm objects.
     *
     * @param fieldName Name of field to add
     * @param objectSchema {@link DynamicRealmObjectSchema} for the object to link to.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public DynamicRealmObjectSchema addList(String fieldName, DynamicRealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(ColumnType.LINK_LIST, fieldName, transaction.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Removes a field from the class.
     *
     * @param fieldName Field name to remove
     * @return The updated schema
     * @throws IllegalArgumentException if field name doesn't exists.
     */
    public DynamicRealmObjectSchema removeField(String fieldName) {
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
    public DynamicRealmObjectSchema renameField(String currentFieldName, String newFieldName) {
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
    public DynamicRealmObjectSchema addIndex(String fieldName) {
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
     * Removes an index from a given field. This is the same as removing the {@code @Index} annotation on the field.
     *
     * @param fieldName Field to remove index from.
     * @return The updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or the field doesn't have an index.
     */
    public DynamicRealmObjectSchema removeIndex(String fieldName) {
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
    public DynamicRealmObjectSchema addPrimaryKey(String fieldName) {
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
    public DynamicRealmObjectSchema removePrimaryKey() {
        if (!table.hasPrimaryKey()) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        table.setPrimaryKey("");
        return this;
    }


    public DynamicRealmObjectSchema setNullable(String fieldName) {
        throw new RealmException("Waiting for Null support");
    }

    public DynamicRealmObjectSchema setNotNullable(String fieldName) {
        throw new RealmException("Waiting for Null support");
    }

    /**
     * Creates an object with default values. Classes with a primary key defined must use {@link #createObject(Object)}
     * instead.
     *
     * @return The new object. All fields will have default values for their type.
     * @throws IllegalStateException if the class have a primary key defined.
     */
    public DynamicRealmObject createObject() {
        if (table.hasPrimaryKey()) {
            throw new IllegalStateException("Class requires a primary key value. Use createObject(primaryKeyValue) instead.");
        }
        long rowIndex = table.addEmptyRow();
        return new DynamicRealmObject(realm, table.getCheckedRow(rowIndex));
    }

    /**
     * Creates an object with a given primary key. Classes without a primary key defined must use {@link #createObject()}
     * instead.
     *
     * @return The new object. All fields will have default values for their type, except for the primary key field which
     * will have the provided value.
     * @throws IllegalArgumentException if the primary key value is of the wrong type.
     * @throws IllegalStateException if the class doesn't have a primary key defined.
     */
    public DynamicRealmObject createObject(Object primaryKeyValue) {
        long index = table.addEmptyRowWithPrimaryKey(primaryKeyValue);
        return new DynamicRealmObject(realm, table.getCheckedRow(index));
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
     * Checks if a given field is indexed.
     *
     * @param fieldName Field name to check.
     * @return {@code true} if field is indexed, {@code false} otherwise.
     * @see io.realm.annotations.Index
     */
    public boolean hasIndex(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return table.hasSearchIndex(columnIndex);
    }

    /**
     * Iterate each object with the current schema. Order is undefined.
     *
     * @return The updated schema
     */
    public DynamicRealmObjectSchema forEach(Iterator iterator) {
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

    private String getClassName() {
        return table.getName().substring(Table.TABLE_PREFIX.length());
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


