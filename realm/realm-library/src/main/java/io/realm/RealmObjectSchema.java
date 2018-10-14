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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.annotations.Required;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectStore;
import io.realm.internal.Table;
import io.realm.internal.fields.FieldDescriptor;


/**
 * Class for interacting with the schema for a given RealmObject class. This makes it possible to inspect,
 * add, delete or change the fields for given class.
 * <p>
 * If this {@link RealmObjectSchema} is retrieved from an immutable {@link RealmSchema}, this {@link RealmObjectSchema}
 * will be immutable as well.
 *
 * @see io.realm.RealmMigration
 */
public abstract class RealmObjectSchema {

    static final Map<Class<?>, FieldMetaData> SUPPORTED_SIMPLE_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(String.class, new FieldMetaData(RealmFieldType.STRING, RealmFieldType.STRING_LIST, true));
        m.put(short.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Short.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(int.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Integer.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(long.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Long.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(float.class, new FieldMetaData(RealmFieldType.FLOAT, RealmFieldType.FLOAT_LIST, false));
        m.put(Float.class, new FieldMetaData(RealmFieldType.FLOAT, RealmFieldType.FLOAT_LIST, true));
        m.put(double.class, new FieldMetaData(RealmFieldType.DOUBLE, RealmFieldType.DOUBLE_LIST, false));
        m.put(Double.class, new FieldMetaData(RealmFieldType.DOUBLE, RealmFieldType.DOUBLE_LIST, true));
        m.put(boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, RealmFieldType.BOOLEAN_LIST, false));
        m.put(Boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, RealmFieldType.BOOLEAN_LIST, true));
        m.put(byte.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Byte.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(byte[].class, new FieldMetaData(RealmFieldType.BINARY, RealmFieldType.BINARY_LIST, true));
        m.put(Date.class, new FieldMetaData(RealmFieldType.DATE, RealmFieldType.DATE_LIST, true));
        SUPPORTED_SIMPLE_FIELDS = Collections.unmodifiableMap(m);
    }

    static final Map<Class<?>, FieldMetaData> SUPPORTED_LINKED_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(RealmObject.class, new FieldMetaData(RealmFieldType.OBJECT, null, false));
        m.put(RealmList.class, new FieldMetaData(RealmFieldType.LIST, null, false));
        SUPPORTED_LINKED_FIELDS = Collections.unmodifiableMap(m);
    }

    final RealmSchema schema;
    final BaseRealm realm;
    final Table table;
    private final ColumnInfo columnInfo;

    /**
     * Creates a schema object for a given Realm class.
     *
     * @param realm Realm holding the objects.
     * @param table table representation of the Realm class
     * @param columnInfo mapping between field names and column indexes for the given table
     */
    RealmObjectSchema(BaseRealm realm, RealmSchema schema, Table table, ColumnInfo columnInfo) {
        this.schema = schema;
        this.realm = realm;
        this.table = table;
        this.columnInfo = columnInfo;
    }

    /**
     * Returns the name of the RealmObject class being represented by this schema.
     * <p>
     * <ul>
     * <li>When using a normal {@link Realm} this name is the same as the {@link RealmObject} class.</li>
     * <li>When using a {@link DynamicRealm} this is the name used in all API methods requiring a class name.</li>
     * </ul>
     *
     * @return the name of the RealmObject class represented by this schema.
     */
    public String getClassName() {
        return table.getClassName();
    }

    /**
     * Sets a new name for this RealmObject class. This is equivalent to renaming it.
     *
     * @param className the new name for this class.
     * @throws IllegalArgumentException if className is {@code null} or an empty string, or its length exceeds 56
     * characters.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     * @see RealmSchema#rename(String, String)
     */
    public abstract RealmObjectSchema setClassName(String className);

    /**
     * Adds a new simple field to the RealmObject class. The type must be one supported by Realm. See
     * {@link RealmObject} for the list of supported types. If the field should allow {@code null} values use the boxed
     * type instead e.g., {@code Integer.class} instead of {@code int.class}.
     * <p>
     * To add fields that reference other RealmObjects or RealmLists use
     * {@link #addRealmObjectField(String, RealmObjectSchema)} or {@link #addRealmListField(String, RealmObjectSchema)}
     * instead.
     *
     * @param fieldName name of the field to add.
     * @param fieldType type of field to add. See {@link RealmObject} for the full list.
     * @param attributes set of attributes for this field.
     * @return the updated schema.
     * @throws IllegalArgumentException if the type isn't supported, field name is illegal or a field with that name
     * already exists.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes);

    /**
     * Adds a new field that references another {@link RealmObject}.
     *
     * @param fieldName name of the field to add.
     * @param objectSchema schema for the Realm type being referenced.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema);

    /**
     * Adds a new field that contains a {@link RealmList} with references to other Realm model classes.
     * <p>
     * If the list contains primitive types, use {@link #addRealmListField(String, Class)} instead.
     *
     * @param fieldName name of the field to add.
     * @param objectSchema schema for the Realm type being referenced.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name is illegal or a field with that name already exists.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema);

    /**
     * Adds a new field that references a {@link RealmList} with primitive values. See {@link RealmObject} for the
     * list of supported types.
     * <p>
     * Nullability of elements are defined by using the correct class e.g., {@code Integer.class} instead of
     * {@code int.class}. Alternatively {@link #setRequired(String, boolean)} can be used.
     * <p>
     * Example:
     * <pre>
     * {@code
     * // Defines the list of Strings as being non null.
     * RealmObjectSchema schema = schema.create("Person")
     *     .addRealmListField("children", String.class)
     *     .setRequired("children", true)
     * }
     * </pre>
     * If the list contains references to other Realm classes, use
     * {@link #addRealmListField(String, RealmObjectSchema)} instead.
     *
     * @param fieldName name of the field to add.
     * @param primitiveType simple type of elements in the array.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name is illegal, a field with that name already exists or
     * the element type isn't supported.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema addRealmListField(String fieldName, Class<?> primitiveType);

    /**
     * Removes a field from the class.
     *
     * @param fieldName field name to remove.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exist.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema removeField(String fieldName);

    /**
     * Renames a field from one name to another.
     *
     * @param currentFieldName field name to rename.
     * @param newFieldName the new field name.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exist or if the new field name already exists.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema renameField(String currentFieldName, String newFieldName);

    /**
     * Tests if the class has field defined with the given name.
     *
     * @param fieldName field name to test.
     * @return {@code true} if the field exists, {@code false} otherwise.
     */
    public boolean hasField(String fieldName) {
        return table.getColumnIndex(fieldName) != Table.NO_MATCH;
    }

    /**
     * Adds an index to a given field. This is the equivalent of adding the {@link io.realm.annotations.Index}
     * annotation on the field.
     *
     * @param fieldName field to add index to.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exist, the field cannot be indexed or it already has a
     * index defined.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema addIndex(String fieldName);

    /**
     * Checks if a given field has an index defined.
     *
     * @param fieldName existing field name to check.
     * @return {@code true} if field is indexed, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exist.
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
     * @throws IllegalArgumentException if field name doesn't exist or the field doesn't have an index.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema removeIndex(String fieldName);

    /**
     * Adds a primary key to a given field. This is the same as adding the {@link io.realm.annotations.PrimaryKey}
     * annotation on the field. Further, this implicitly adds {@link io.realm.annotations.Index} annotation to the field
     * as well.
     *
     * @param fieldName field to set as primary key.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exist, the field cannot be a primary key or it already
     * has a primary key defined.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable or this method is called on a synced Realm.
     */
    public abstract RealmObjectSchema addPrimaryKey(String fieldName);

    /**
     * Removes the primary key from this class. This is the same as removing the {@link io.realm.annotations.PrimaryKey}
     * annotation from the class. Further, this implicitly removes {@link io.realm.annotations.Index} annotation from
     * the field as well.
     *
     * @return the updated schema.
     * @throws IllegalArgumentException if the class doesn't have a primary key defined.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema removePrimaryKey();

    /**
     * Sets a field to be required i.e., it is not allowed to hold {@code null} values. This is equivalent to switching
     * between boxed types and their primitive variant e.g., {@code Integer} to {@code int}.
     * <p>
     * If the type of designated field is a list of values (not {@link RealmObject}s , specified nullability
     * only affects its elements, not the field itself. Value list itself is always non-nullable.
     *
     * @param fieldName name of field in the class.
     * @param required {@code true} if field should be required, {@code false} otherwise.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name doesn't exist, cannot have the {@link Required} annotation or
     * the field already have been set as required.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     * @see Required
     */
    public abstract RealmObjectSchema setRequired(String fieldName, boolean required);

    /**
     * Sets a field to be nullable i.e., it should be able to hold {@code null} values. This is equivalent to switching
     * between primitive types and their boxed variant e.g., {@code int} to {@code Integer}.
     * <p>
     * If the type of designated field is a list of values (not {@link RealmObject}s , specified nullability
     * only affects its elements, not the field itself. Value list itself is always non-nullable.
     *
     * @param fieldName name of field in the class.
     * @param nullable {@code true} if field should be nullable, {@code false} otherwise.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name doesn't exist, or cannot be set as nullable.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema setNullable(String fieldName, boolean nullable);

    /**
     * Checks if a given field is required i.e., it is not allowed to contain {@code null} values.
     *
     * @param fieldName field to check.
     * @return {@code true} if it is required, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exist.
     * @see #setRequired(String, boolean)
     */
    public boolean isRequired(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return !table.isColumnNullable(columnIndex);
    }

    /**
     * Checks if a given field is nullable i.e., it is allowed to contain {@code null} values.
     *
     * @param fieldName field to check.
     * @return {@code true} if it is required, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exist.
     * @see #setNullable(String, boolean)
     */
    public boolean isNullable(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return table.isColumnNullable(columnIndex);
    }

    /**
     * Checks if a given field is the primary key field.
     *
     * @param fieldName field to check.
     * @return {@code true} if it is the primary key field, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exist.
     * @see #addPrimaryKey(String)
     */
    public boolean isPrimaryKey(String fieldName) {
        checkFieldExists(fieldName);
        return fieldName.equals(OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName()));
    }

    /**
     * Checks if the class has a primary key defined.
     *
     * @return {@code true} if a primary key is defined, {@code false} otherwise.
     * @see io.realm.annotations.PrimaryKey
     */
    public boolean hasPrimaryKey() {
        return OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName()) != null;
    }

    /**
     * Returns the name of the primary key field.
     *
     * @return the name of the primary key field.
     * @throws IllegalStateException if the class doesn't have a primary key defined.
     */
    public String getPrimaryKey() {
        String pkField = OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName());
        if (pkField == null) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        return pkField;
    }

    /**
     * Returns all fields in this class.
     *
     * @return a list of all the fields in this class.
     */
    public Set<String> getFieldNames() {
        int columnCount = (int) table.getColumnCount();
        Set<String> columnNames = new LinkedHashSet<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            String name = table.getColumnName(i);
            if (!OsObject.isObjectIdColumn(name)) {
                columnNames.add(name);
            }
        }
        return columnNames;
    }

    /**
     * Runs a transformation function on each RealmObject instance of the current class. The object will be represented
     * as a {@link DynamicRealmObject}.
     *
     * @param function transformation function.
     * @return this schema.
     * @throws UnsupportedOperationException if this {@link RealmObjectSchema} is immutable.
     */
    public abstract RealmObjectSchema transform(Function function);

    /**
     * Returns the type used by the underlying storage engine to represent this field.
     *
     * @param fieldName name of the target field.
     * @return the underlying type used by Realm to represent this field.
     */
    public RealmFieldType getFieldType(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return table.getColumnType(columnIndex);
    }

    /**
     * Get a parser for a field descriptor.
     *
     * @param fieldDescription fieldName or link path to a field name.
     * @param validColumnTypes valid field type for the last field in a linked field
     * @return a FieldDescriptor
     */
    abstract FieldDescriptor getColumnIndices(String fieldDescription, RealmFieldType... validColumnTypes);

    RealmObjectSchema add(String name, RealmFieldType type, boolean primary, boolean indexed, boolean required) {
        long columnIndex = table.addColumn(type, name, (required) ? Table.NOT_NULLABLE : Table.NULLABLE);

        if (indexed) { table.addSearchIndex(columnIndex); }

        if (primary) {
            OsObjectStore.setPrimaryKeyForObject(realm.sharedRealm, getClassName(), name);
        }

        return this;
    }

    RealmObjectSchema add(String name, RealmFieldType type, RealmObjectSchema linkedTo) {
        table.addColumnLink(
                type,
                name,
                realm.getSharedRealm().getTable(Table.getTableNameForClass(linkedTo.getClassName())));
        return this;
    }

    long getAndCheckFieldIndex(String fieldName) {
        long index = columnInfo.getColumnIndex(fieldName);
        if (index < 0) {
            throw new IllegalArgumentException("Field does not exist: " + fieldName);
        }
        return index;
    }

    Table getTable() {
        return table;
    }

    static final Map<Class<?>, FieldMetaData> getSupportedSimpleFields() {
        return SUPPORTED_SIMPLE_FIELDS;
    }

    protected final SchemaConnector getSchemaConnector() {
        return new SchemaConnector(schema);
    }

    /**
     * Function interface, used when traversing all objects of the current class and apply a function on each.
     *
     * @see #transform(Function)
     */
    public interface Function {
        void apply(DynamicRealmObject obj);
    }

    /**
     * Returns the column index in the underlying table for the given field name.
     * <b>FOR TESTING USE ONLY!</b>
     *
     * @param fieldName field name to find index for.
     * @return column index or -1 if it doesn't exists.
     */
    //@VisibleForTesting(otherwise = VisibleForTesting.NONE)
    long getFieldIndex(String fieldName) {
        return columnInfo.getColumnIndex(fieldName);
    }

    static void checkLegalName(String fieldName) {
        //noinspection ConstantConditions
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name can not be null or empty");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Field name can not contain '.'");
        }
        if (fieldName.length() > 63) {
            throw new IllegalArgumentException("Field name is currently limited to max 63 characters.");
        }
    }

    void checkFieldExists(String fieldName) {
        if (table.getColumnIndex(fieldName) == Table.NO_MATCH) {
            throw new IllegalArgumentException("Field name doesn't exist on object '" + getClassName() + "': " + fieldName);
        }
    }

    long getColumnIndex(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(
                    String.format(Locale.US,
                            "Field name '%s' does not exist on schema for '%s'",
                            fieldName, getClassName()
                    ));
        }
        return columnIndex;
    }

    static final class DynamicColumnIndices extends ColumnInfo {
        private final Table table;

        DynamicColumnIndices(Table table) {
            super(null, false);
            this.table = table;
        }

        @Override
        public long getColumnIndex(String columnName) {
            return table.getColumnIndex(columnName);
        }

        @Override
        public ColumnDetails getColumnDetails(String columnName) {
            throw new UnsupportedOperationException("DynamicColumnIndices do not support 'getColumnDetails'");
        }

        @Override
        public void copyFrom(ColumnInfo src) {
            throw new UnsupportedOperationException("DynamicColumnIndices cannot be copied");
        }

        @Override
        protected ColumnInfo copy(boolean immutable) {
            throw new UnsupportedOperationException("DynamicColumnIndices cannot be copied");
        }


        @Override
        protected void copy(ColumnInfo src, ColumnInfo dst) {
            throw new UnsupportedOperationException("DynamicColumnIndices cannot copy");
        }
    }

    // Tuple containing data about each supported Java type.
    static final class FieldMetaData {
        final RealmFieldType fieldType; // Underlying Realm type for fields with this type
        final RealmFieldType listType; // Underlying Realm type for RealmLists containing this type
        final boolean defaultNullable;

        FieldMetaData(RealmFieldType fieldType, @Nullable RealmFieldType listType, boolean defaultNullable) {
            this.fieldType = fieldType;
            this.listType = listType;
            this.defaultNullable = defaultNullable;
        }
    }
}
