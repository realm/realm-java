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

import java.util.Collections;
import java.util.Set;

import io.realm.Realm;
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
public final class RealmObjectSchema {

    private static final String TABLE_PREFIX = "class_";
    private final Realm realm;
    private final Table table;
    private final ImplicitTransaction transaction;

    /**
     * Creates a schema object for a given Realm class.
     * @param realm Realm holding the objects.
     * @param transaction Transaction object for the current Realm.
     * @param table Table representation of the Realm class
     */
    RealmObjectSchema(Realm realm, ImplicitTransaction transaction, Table table) {
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
        checkEmpty(fieldName);
        checkFieldNameIsAvailable(fieldName);
        long columnIndex = table.addColumn(ColumnType.STRING, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.INTEGER, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.BOOLEAN, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.BINARY, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.FLOAT, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.DOUBLE, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        long columnIndex = table.addColumn(ColumnType.DATE, fieldName);
        setModifiers(columnIndex, modifiers);
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
        checkEmpty(fieldName);
        table.addColumnLink(ColumnType.LINK, fieldName, transaction.getTable(TABLE_PREFIX + objectSchema.getClassName()));
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
        checkEmpty(fieldName);
        table.addColumnLink(ColumnType.LINK_LIST, fieldName, transaction.getTable(TABLE_PREFIX + objectSchema.getClassName()));
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
        checkEmpty(fieldName);
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
        checkEmpty(currentFieldName);
        checkFieldExists(currentFieldName);
        checkEmpty(newFieldName);
        checkFieldNameIsAvailable(newFieldName);
        long columnIndex = getColumnIndex(currentFieldName);
        table.renameColumn(columnIndex, newFieldName);
        return this;
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
        checkEmpty(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
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
    public RealmObjectSchema removeIndex(String fieldName) {
        checkEmpty(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (!table.hasSearchIndex(columnIndex)) {
            throw new IllegalArgumentException("Field is not indexed: " + fieldName);
        }
        table.removeIndex(columnIndex);
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
        checkEmpty(fieldName);
        checkFieldExists(fieldName);
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

    private void setModifiers(long columnIndex, Set<RealmModifier> modifiers) {
        if (modifiers != null && modifiers.size() > 0) {
            if (modifiers.contains(RealmModifier.INDEXED)) {
                table.addSearchIndex(columnIndex);
            }

            if (modifiers.contains(RealmModifier.PRIMARY_KEY)) {
                table.setPrimaryKey(columnIndex);
            }
        }
    }

    private void checkEmpty(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Fieldname must not be null or empty");
        }
    }

    private void checkFieldNameIsAvailable(@NotNull String fieldName) {
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
        return table.getName().substring(TABLE_PREFIX.length());
    }
}
