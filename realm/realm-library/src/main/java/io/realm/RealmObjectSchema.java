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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.realm.annotations.Required;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;

/**
 * Class for interacting with the schema for a given RealmObject class. This makes it possible to
 * add, delete or change the fields for given class.
 *
 * @see io.realm.RealmMigration
 */
public final class RealmObjectSchema {

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_SIMPLE_FIELDS;
    static {
        SUPPORTED_SIMPLE_FIELDS = new HashMap<Class<?>, FieldMetaData>();
        SUPPORTED_SIMPLE_FIELDS.put(String.class, new FieldMetaData(RealmFieldType.STRING, true));
        SUPPORTED_SIMPLE_FIELDS.put(short.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Short.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(int.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Integer.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(long.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Long.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(float.class, new FieldMetaData(RealmFieldType.FLOAT, false));
        SUPPORTED_SIMPLE_FIELDS.put(Float.class, new FieldMetaData(RealmFieldType.FLOAT, true));
        SUPPORTED_SIMPLE_FIELDS.put(double.class, new FieldMetaData(RealmFieldType.DOUBLE, false));
        SUPPORTED_SIMPLE_FIELDS.put(Double.class, new FieldMetaData(RealmFieldType.DOUBLE, true));
        SUPPORTED_SIMPLE_FIELDS.put(boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, false));
        SUPPORTED_SIMPLE_FIELDS.put(Boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, true));
        SUPPORTED_SIMPLE_FIELDS.put(byte.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Byte.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(byte[].class, new FieldMetaData(RealmFieldType.BINARY, true));
        SUPPORTED_SIMPLE_FIELDS.put(Date.class, new FieldMetaData(RealmFieldType.DATE, true));
    }

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_LINKED_FIELDS;
    static {
        SUPPORTED_LINKED_FIELDS = new HashMap<>();
        SUPPORTED_LINKED_FIELDS.put(RealmObject.class, new FieldMetaData(RealmFieldType.OBJECT, false));
        SUPPORTED_LINKED_FIELDS.put(RealmList.class, new FieldMetaData(RealmFieldType.LIST, false));
    }

    private final BaseRealm realm;
    private final Table table;
    private final ImplicitTransaction transaction;

    /**
     * Creates a schema object for a given Realm class.
     *
     * @param realm       Realm holding the objects.
     * @param transaction transaction object for the current Realm.
     * @param table       table representation of the Realm class
     */
    RealmObjectSchema(BaseRealm realm, ImplicitTransaction transaction, Table table) {
        this.realm = realm;
        this.transaction = transaction;
        this.table = table;
    }

    /**
     * Returns the name of the RealmObject class being represented by this schema.
     * <p>
     * When using a normal {@link Realm} this name is the same as the {@link RealmObject} class.
     * When using a {@link DynamicRealm} this is the name used in all API methods requiring a class name.
     *
     * @return the name of the RealmObject class represented by this schema.
     */
    public String getClassName() {
        return table.getName().substring(Table.TABLE_PREFIX.length());
    }

    /**
     * Sets a new name for this RealmObject class. This is equivalent to renaming it.
     *
     * @param className the new name for this class.
     * @see RealmSchema#rename(String, String)
     */
    public RealmObjectSchema setClassName(String className) {
        checkEmpty(className);
        String internalTableName = Table.TABLE_PREFIX + className;
        if (transaction.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        transaction.renameTable(table.getName(), internalTableName);
        return this;
    }

    /**
     * Adds a new simple field to the RealmObject class. The type must be one supported by Realm. See {@link RealmObject}
     * for the list of supported types. If the field should allow {@code null} values use the boxed type instead e.g.
     * {@code Integer.class} instead of {@code int.class}.
     * <p>
     * To add fields that reference other RealmObjects or RealmLists use {@link #addRealmObjectField(String, RealmObjectSchema)}
     * or {@link #addRealmListField(String, RealmObjectSchema)} instead.
     *
     * @param fieldName name of the field to add.
     * @param fieldType type of field to add. See {@link RealmObject} for the full list.
     * @param attributes set of attributes for this field.
     * @return the updated schema.
     * @throws IllegalArgumentException if the type isn't supported, field name is illegal or a field with that name
     * already exists.
     */
    public RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes) {
        FieldMetaData metadata = SUPPORTED_SIMPLE_FIELDS.get(fieldType);
        if (metadata == null) {
            if (SUPPORTED_LINKED_FIELDS.containsKey(fieldType)) {
                throw new IllegalArgumentException("Use addLinkField() instead to add fields that link to other RealmObjects: " + fieldName);
            } else {
                throw new IllegalArgumentException(String.format("Realm doesn't support this field type: %s(%s)",
                        fieldName, fieldType));
            }
        }

        checkNewFieldName(fieldName);
        boolean nullable = metadata.defaultNullable && !containsAttribute(attributes, FieldAttribute.REQUIRED);
        long columnIndex = table.addColumn(metadata.realmType, fieldName, nullable);
        addModifiers(columnIndex, attributes);
        return this;
    }

    /**
     * Adds a new field that references another {@link RealmObject}.
     *
     * @param fieldName  name of the field to add.
     * @param objectSchema schema for the Realm type being referenced.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.OBJECT, fieldName, transaction.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Adds a new field that references a {@link RealmList}.
     *
     * @param fieldName  name of the field to add.
     * @param objectSchema schema for the Realm type being referenced.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name is illegal or a field with that name already exists.
     */
    public RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema) {
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
     * @param newFieldName     the new field name.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or if the new field name already exists.
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
     * Tests if the class has field defined with the given name.
     *
     * @param fieldName field name to test.
     * @return {@code true} if the field exists, {@code false} otherwise.
     */
    public boolean hasField(String fieldName) {
        return table.getColumnIndex(fieldName) != TableOrView.NO_MATCH;
    }

    /**
     * Adds a index to a given field. This is the equivalent of adding the {@link io.realm.annotations.Index} annotation
     * on the field.
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
            throw new IllegalStateException(fieldName + " already has an index.");
        }
        table.addSearchIndex(columnIndex);
        return this;
    }

    /**
     * Checks if a given field has an index defined.
     *
     * @param fieldName existing field name to check.
     * @return {@code true} if field is indexed, {@code false} otherwise.
     * @see io.realm.annotations.Index
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
     * Adds a primary key to a given field. This is the same as adding the {@link io.realm.annotations.PrimaryKey}
     * annotation on the field.
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
     * Removes the primary key from this class. This is the same as removing the {@link io.realm.annotations.PrimaryKey}
     * annotation from the class.
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
     * Sets a field to be required, i.e. not allowed to hold {@code null values}. This is equivalent to switching
     * between boxed types and their primitive variant e.g. {@code Integer} to {@code int}.
     *
     * @param fieldName name of field in the class.
     * @param required  {@code true} if field should be required, {@code false} otherwise.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or the field already has the given required flag.
     * @see Required
     */
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

    /**
     * Sets a field to be nullable, i.e. it should be able to hold {@code null values}. This is equivalent to switching
     * between primitive types and their boxed variant e.g. {@code int} to {@code Integer}.
     *
     * @param fieldName name of field in the class.
     * @param nullable  {@code true} if field should be nullable, {@code false} otherwise.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or the field already has the given nullable flag.
     */
    public RealmObjectSchema setNullable(String fieldName, boolean nullable) {
        setRequired(fieldName, !nullable);
        return this;
    }

    /**
     * Checks if a given field is required, i.e. is not allowed to contain {@code null} values.
     *
     * @param fieldName field to check.
     * @return {@code true} if it is requied, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exists.
     * @see #setRequired(String, boolean)
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
     * @see #setNullable(String, boolean)
     */
    public boolean isNullable(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        return table.isColumnNullable(columnIndex);
    }

    /**
     * Checks if the class has a primary key defined.
     *
     * @return {@code true} if a primary key is defined, {@code false} otherwise.
     * @see io.realm.annotations.PrimaryKey
     */
    public boolean hasPrimaryKey() {
        return table.hasPrimaryKey();
    }

    /**
     * Return all fields in this class.
     *
     * @return A list of all the fields in this class.
     */
    public Set<String> getFieldNames() {
        int columnCount = (int) table.getColumnCount();
        Set<String> columnNames = new TreeSet<String>();
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(table.getColumnName(i));
        }
        return columnNames;
    }

    /**
     * Runs a transformation on each RealmObject instance of the current class. The object will be represented as a
     * {@link DynamicRealmObject}.
     *
     * @return This schema.
     */
    public RealmObjectSchema forEach(Transformer transformer) {
        if (transformer != null) {
            long size = table.size();
            for (long i = 0; i < size; i++) {
                transformer.apply(new DynamicRealmObject(realm, table.getCheckedRow(i)));
            }
        }

        return this;
    }

    // Invariant: Field was just added
    private void addModifiers(long columnIndex, FieldAttribute[] attributes) {
        if (attributes != null && attributes.length > 0) {
            if (containsAttribute(attributes, FieldAttribute.INDEXED)) {
                table.addSearchIndex(columnIndex);
            }

            if (containsAttribute(attributes, FieldAttribute.PRIMARY_KEY)) {
                table.setPrimaryKey(columnIndex);
                table.addSearchIndex(columnIndex);
            }
        }
    }

    private boolean containsAttribute(FieldAttribute[] attributeList, FieldAttribute attribute) {
        if (attributeList == null || attributeList.length == 0) {
            return false;
        }
        for (int i = 0; i < attributeList.length; i++) {
            if (attributeList[i] == attribute) {
                return true;
            }
        }
        return false;
    }

    private void checkNewFieldName(String fieldName) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
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
     * Transformer interface for traversing all objects of the current class and apply a transformation on each.
     *
     * @see #forEach(Transformer)
     */
    public interface Transformer {
        void apply(DynamicRealmObject obj);
    }

    // Tuple containing data about each supported Java type
    private static class FieldMetaData {
        public final RealmFieldType realmType;
        public final boolean defaultNullable;

        public FieldMetaData(RealmFieldType realmType, boolean defaultNullable) {
            this.realmType = realmType;
            this.defaultNullable = defaultNullable;
        }
    }
}
