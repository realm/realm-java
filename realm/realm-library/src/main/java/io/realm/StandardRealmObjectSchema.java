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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import io.realm.annotations.Required;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;


class StandardRealmObjectSchema extends RealmObjectSchema {

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_SIMPLE_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(String.class, new FieldMetaData(RealmFieldType.STRING, true));
        m.put(short.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Short.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(int.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Integer.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(long.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Long.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(float.class, new FieldMetaData(RealmFieldType.FLOAT, false));
        m.put(Float.class, new FieldMetaData(RealmFieldType.FLOAT, true));
        m.put(double.class, new FieldMetaData(RealmFieldType.DOUBLE, false));
        m.put(Double.class, new FieldMetaData(RealmFieldType.DOUBLE, true));
        m.put(boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, false));
        m.put(Boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, true));
        m.put(byte.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Byte.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(byte[].class, new FieldMetaData(RealmFieldType.BINARY, true));
        m.put(Date.class, new FieldMetaData(RealmFieldType.DATE, true));
        SUPPORTED_SIMPLE_FIELDS = Collections.unmodifiableMap(m);
    }

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_LINKED_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(RealmObject.class, new FieldMetaData(RealmFieldType.OBJECT, false));
        m.put(RealmList.class, new FieldMetaData(RealmFieldType.LIST, false));
        SUPPORTED_LINKED_FIELDS = Collections.unmodifiableMap(m);
    }

    private final BaseRealm realm;
    private final Map<String, Long> columnIndices;
    private final Table table;

    /**
     * Creates a schema object for a given Realm class.
     *
     * @param realm Realm holding the objects.
     * @param table table representation of the Realm class
     * @param columnIndices mapping between field names and column indexes for the given table
     */
    StandardRealmObjectSchema(BaseRealm realm, Table table, Map<String, Long> columnIndices) {
        this.realm = realm;
        this.table = table;
        this.columnIndices = columnIndices;
    }

    @Override
    Table getTable() {
        return table;
    }

    /**
     * There are no resources here that need closing.
     */
    @Override
    public void close() { }

    /**
     * Returns the name of the RealmObject class being represented by this schema.
     * <p>
     * <ul>
     * <li>When using a typed {@link Realm} this name is the same as the {@link RealmObject} class.</li>
     * <li>When using a {@link DynamicRealm} this is the name used in all API methods requiring a class name.</li>
     * </ul>
     *
     * @return the name of the RealmObject class represented by this schema.
     */
    @Override
    public String getClassName() {
        return table.getName().substring(Table.TABLE_PREFIX.length());
    }

    /**
     * Sets a new name for this RealmObject class. This is equivalent to renaming it. When
     * {@link StandardRealmObjectSchema#table} has a primary key, this will transfer the primary key for the new class name.
     *
     * @param className the new name for this class.
     * @throws IllegalArgumentException if className is {@code null} or an empty string, or its length exceeds 56
     * characters.
     * @see StandardRealmSchema#rename(String, String)
     */
    @Override
    public StandardRealmObjectSchema setClassName(String className) {
        realm.checkNotInSync(); // renaming a table is not permitted
        checkEmpty(className);
        String internalTableName = Table.TABLE_PREFIX + className;
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is too long. Limit is 56 characters: \'" + className + "\' (" + Integer.toString(className.length()) + ")");
        }
        if (realm.sharedRealm.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        // in case this table has a primary key, we need to transfer it after renaming the table.
        String oldTableName = null;
        String pkField = null;
        if (table.hasPrimaryKey()) {
            oldTableName = table.getName();
            pkField = getPrimaryKey();
            table.setPrimaryKey(null);
        }
        realm.sharedRealm.renameTable(table.getName(), internalTableName);
        if (pkField != null && !pkField.isEmpty()) {
            try {
                table.setPrimaryKey(pkField);
            } catch (Exception e) {
                // revert the table name back when something goes wrong
                realm.sharedRealm.renameTable(table.getName(), oldTableName);
                throw e;
            }
        }
        return this;
    }

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
     */
    @Override
    public StandardRealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes) {
        FieldMetaData metadata = SUPPORTED_SIMPLE_FIELDS.get(fieldType);
        if (metadata == null) {
            if (SUPPORTED_LINKED_FIELDS.containsKey(fieldType)) {
                throw new IllegalArgumentException("Use addRealmObjectField() instead to add fields that link to other RealmObjects: " + fieldName);
            } else {
                throw new IllegalArgumentException(String.format("Realm doesn't support this field type: %s(%s)",
                        fieldName, fieldType));
            }
        }

        checkNewFieldName(fieldName);
        boolean nullable = metadata.defaultNullable;
        if (containsAttribute(attributes, FieldAttribute.REQUIRED)) {
            nullable = false;
        }

        long columnIndex = table.addColumn(metadata.realmType, fieldName, nullable);
        try {
            addModifiers(fieldName, attributes);
        } catch (Exception e) {
            // Modifiers have been removed by the addModifiers method()
            table.removeColumn(columnIndex);
            throw e;
        }
        return this;
    }

    /**
     * Adds a new field that references another {@link RealmObject}.
     *
     * @param fieldName name of the field to add.
     * @param objectSchema schema for the Realm type being referenced.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name is illegal or a field with that name already exists.
     */
    @Override
    public StandardRealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.OBJECT, fieldName, realm.sharedRealm.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Adds a new field that references a {@link RealmList}.
     *
     * @param fieldName name of the field to add.
     * @param objectSchema schema for the Realm type being referenced.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name is illegal or a field with that name already exists.
     */
    @Override
    public StandardRealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.LIST, fieldName, realm.sharedRealm.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    /**
     * Removes a field from the class.
     *
     * @param fieldName field name to remove.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exist.
     */
    @Override
    public StandardRealmObjectSchema removeField(String fieldName) {
        realm.checkNotInSync(); // destructive modification of a schema is not permitted
        checkLegalName(fieldName);
        if (!hasField(fieldName)) {
            throw new IllegalStateException(fieldName + " does not exist.");
        }
        long columnIndex = getColumnIndex(fieldName);
        if (table.getPrimaryKey() == columnIndex) {
            table.setPrimaryKey(null);
        }
        table.removeColumn(columnIndex);
        return this;
    }

    /**
     * Renames a field from one name to another.
     *
     * @param currentFieldName field name to rename.
     * @param newFieldName the new field name.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exist or if the new field name already exists.
     */
    @Override
    public StandardRealmObjectSchema renameField(String currentFieldName, String newFieldName) {
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

    /**
     * Tests if the class has field defined with the given name.
     *
     * @param fieldName field name to test.
     * @return {@code true} if the field exists, {@code false} otherwise.
     */
    @Override
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
     */
    @Override
    public StandardRealmObjectSchema addIndex(String fieldName) {
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
     * @throws IllegalArgumentException if field name doesn't exist.
     * @see io.realm.annotations.Index
     */
    @Override
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
     */
    @Override
    public StandardRealmObjectSchema removeIndex(String fieldName) {
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

    /**
     * Adds a primary key to a given field. This is the same as adding the {@link io.realm.annotations.PrimaryKey}
     * annotation on the field. Further, this implicitly adds {@link io.realm.annotations.Index} annotation to the field
     * as well.
     *
     * @param fieldName field to set as primary key.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exist, the field cannot be a primary key or it already
     * has a primary key defined.
     */
    @Override
    public StandardRealmObjectSchema addPrimaryKey(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        if (table.hasPrimaryKey()) {
            throw new IllegalStateException("A primary key is already defined");
        }
        table.setPrimaryKey(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (!table.hasSearchIndex(columnIndex)) {
            // No exception will be thrown since adding PrimaryKey implies the column has an index.
            table.addSearchIndex(columnIndex);
        }
        return this;
    }

    /**
     * Removes the primary key from this class. This is the same as removing the {@link io.realm.annotations.PrimaryKey}
     * annotation from the class. Further, this implicitly removes {@link io.realm.annotations.Index} annotation from
     * the field as well.
     *
     * @return the updated schema.
     * @throws IllegalArgumentException if the class doesn't have a primary key defined.
     */
    @Override
    public StandardRealmObjectSchema removePrimaryKey() {
        realm.checkNotInSync(); // Destructive modifications are not permitted.
        if (!table.hasPrimaryKey()) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        long columnIndex = table.getPrimaryKey();
        if (table.hasSearchIndex(columnIndex)) {
            table.removeSearchIndex(columnIndex);
        }
        table.setPrimaryKey("");
        return this;
    }

    /**
     * Sets a field to be required i.e., it is not allowed to hold {@code null} values. This is equivalent to switching
     * between boxed types and their primitive variant e.g., {@code Integer} to {@code int}.
     *
     * @param fieldName name of field in the class.
     * @param required {@code true} if field should be required, {@code false} otherwise.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name doesn't exist, cannot have the {@link Required} annotation or
     * the field already have been set as required.
     * @see Required
     */
    @Override
    public StandardRealmObjectSchema setRequired(String fieldName, boolean required) {
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
     * Sets a field to be nullable i.e., it should be able to hold {@code null} values. This is equivalent to switching
     * between primitive types and their boxed variant e.g., {@code int} to {@code Integer}.
     *
     * @param fieldName name of field in the class.
     * @param nullable {@code true} if field should be nullable, {@code false} otherwise.
     * @return the updated schema.
     * @throws IllegalArgumentException if the field name doesn't exist, or cannot be set as nullable.
     */
    @Override
    public StandardRealmObjectSchema setNullable(String fieldName, boolean nullable) {
        setRequired(fieldName, !nullable);
        return this;
    }

    /**
     * Checks if a given field is required i.e., it is not allowed to contain {@code null} values.
     *
     * @param fieldName field to check.
     * @return {@code true} if it is required, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exist.
     * @see #setRequired(String, boolean)
     */
    @Override
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
    @Override
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
    @Override
    public boolean isPrimaryKey(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return columnIndex == table.getPrimaryKey();
    }

    /**
     * Checks if the class has a primary key defined.
     *
     * @return {@code true} if a primary key is defined, {@code false} otherwise.
     * @see io.realm.annotations.PrimaryKey
     */
    @Override
    public boolean hasPrimaryKey() {
        return table.hasPrimaryKey();
    }

    /**
     * Returns the name of the primary key field.
     *
     * @return the name of the primary key field.
     * @throws IllegalStateException if the class doesn't have a primary key defined.
     */
    @Override
    public String getPrimaryKey() {
        if (!table.hasPrimaryKey()) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        return table.getColumnName(table.getPrimaryKey());
    }

    /**
     * Returns all fields in this class.
     *
     * @return a list of all the fields in this class.
     */
    @Override
    public Set<String> getFieldNames() {
        int columnCount = (int) table.getColumnCount();
        Set<String> columnNames = new LinkedHashSet<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(table.getColumnName(i));
        }
        return columnNames;
    }

    /**
     * Runs a transformation function on each RealmObject instance of the current class. The object will be represented
     * as a {@link DynamicRealmObject}.
     *
     * @return this schema.
     */
    @Override
    public StandardRealmObjectSchema transform(Function function) {
        if (function != null) {
            long size = table.size();
            for (long i = 0; i < size; i++) {
                function.apply(new DynamicRealmObject(realm, table.getCheckedRow(i)));
            }
        }

        return this;
    }

    /**
     * Returns the type used by the underlying storage engine to represent this field.
     *
     * @return the underlying type used by Realm to represent this field.
     */
    @Override
    public RealmFieldType getFieldType(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return table.getColumnType(columnIndex);
    }

    @Override
    StandardRealmObjectSchema add(String name, RealmFieldType type, boolean primary, boolean indexed, boolean required) {
        long columnIndex = table.addColumn(type, name, (required) ? Table.NOT_NULLABLE : Table.NULLABLE);

        if (indexed) { table.addSearchIndex(columnIndex); }

        if (primary) { table.setPrimaryKey(name); }

        return this;
    }

    @Override
    StandardRealmObjectSchema add(String name, RealmFieldType type, RealmObjectSchema linkedTo) {
        table.addColumnLink(
                type,
                name,
                realm.getSharedRealm().getTable(StandardRealmSchema.TABLE_PREFIX + linkedTo.getClassName()));
        return this;
    }

    /**
     * Returns the column indices for the given field name. If a linked field is defined, the column index for
     * each field is returned.
     *
     * @param fieldDescription fieldName or link path to a field name.
     * @param validColumnTypes valid field type for the last field in a linked field
     * @return list of column indices.
     */
    // TODO: consider another caching strategy so linked classes are included in the cache.
    @Override
    long[] getColumnIndices(String fieldDescription, RealmFieldType... validColumnTypes) {
        if (fieldDescription == null || fieldDescription.equals("")) {
            throw new IllegalArgumentException("Invalid query: field name is empty");
        }
        if (fieldDescription.endsWith(".")) {
            throw new IllegalArgumentException("Invalid query: field name must not end with a period ('.')");
        }
        String[] names = fieldDescription.split("\\.");

        //final RealmProxyMediator mediator = realm.getConfiguration().getSchemaMediator();

        long[] columnIndices = new long[names.length];
        Table currentTable = table;
        RealmFieldType columnType;
        String columnName;
        String tableName;
        for (int i = 0; /* loop exits in the middle */ ; i++) {
            columnName = names[i];
            if (columnName.length() <= 0) {
                throw new IllegalArgumentException(String.format(
                        "Invalid query: empty column name in field '%s'.  " +
                                "A field name must not begin with, end with, or contain adjacent periods ('.').",
                                fieldDescription));
            }

            tableName = getTableName(currentTable);
            long index = currentTable.getColumnIndex(columnName);
            if (index < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid query: field '%s' does not exist in table '%s'.",
                                columnName, tableName));
            }
            columnIndices[i] = index;

            columnType = currentTable.getColumnType(index);

            if (i >= names.length - 1) { break; }

            if ((columnType != RealmFieldType.OBJECT) && (columnType != RealmFieldType.LIST)) {
                throw new IllegalArgumentException(
                        String.format("Invalid query: field '%s' in table '%s' is of type '%s'.  It must be a LIST or OBJECT type.",
                                columnName, tableName, columnType.toString()));
            }

            currentTable = currentTable.getLinkTarget(index);
        }

        if ((validColumnTypes != null) && (validColumnTypes.length > 0) && !isValidType(columnType, validColumnTypes)) {
            throw new IllegalArgumentException(
                    String.format("Invalid query: field '%s' in table '%s' is of invalid type '%s'.",
                            columnName, tableName, columnType.toString()));
        }

        return columnIndices;
    }

    /**
     * Returns the column index in the underlying table for the given field name.
     *
     * @param fieldName field name to find index for.
     * @return column index or null if it doesn't exists.
     */
    Long getFieldIndex(String fieldName) {
        return columnIndices.get(fieldName);
    }

    /**
     * Returns the column index in the underlying table for the given field name.
     *
     * @param fieldName field name to find index for.
     * @return column index.
     * @throws IllegalArgumentException if the field does not exists.
     */
    @Override
    long getAndCheckFieldIndex(String fieldName) {
        Long index = columnIndices.get(fieldName);
        if (index == null) {
            throw new IllegalArgumentException("Field does not exist: " + fieldName);
        }
        return index;
    }

    private String getTableName(Table table) {
        return table.getName().substring(StandardRealmSchema.TABLE_PREFIX.length());
    }

    // Invariant: Field was just added. This method is responsible for cleaning up attributes if it fails.
    private void addModifiers(String fieldName, FieldAttribute[] attributes) {
        boolean indexAdded = false;
        try {
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
            throw new IllegalArgumentException("Field name can not be null or empty");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Field name can not contain '.'");
        }
    }

    private void checkFieldNameIsAvailable(String fieldName) {
        if (table.getColumnIndex(fieldName) != Table.NO_MATCH) {
            throw new IllegalArgumentException("Field already exists in '" + getClassName() + "': " + fieldName);
        }
    }

    private void checkFieldExists(String fieldName) {
        if (table.getColumnIndex(fieldName) == Table.NO_MATCH) {
            throw new IllegalArgumentException("Field name doesn't exist on object '" + getClassName() + "': " + fieldName);
        }
    }

    private long getColumnIndex(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(
                    String.format("Field name '%s' does not exist on schema for '%s",
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

    private boolean isValidType(RealmFieldType columnType, RealmFieldType[] validColumnTypes) {
        for (int i = 0; i < validColumnTypes.length; i++) {
            if (validColumnTypes[i] == columnType) {
                return true;
            }
        }
        return false;
    }

    public static final class DynamicColumnMap implements Map<String, Long> {
        private final Table table;

        DynamicColumnMap(Table table) {
            this.table = table;
        }

        @Override
        public Long get(Object key) {
            long ret = table.getColumnIndex((String) key);
            return ret < 0 ? null : ret;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, Long>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long put(String key, Long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Long> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Long> values() {
            throw new UnsupportedOperationException();
        }
    }
}
