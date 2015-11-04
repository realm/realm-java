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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
public final class RealmObjectSchema{

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
    private final Map<String, Long> columnIndices;

    /**
     * Creates a schema object for a given Realm class.
     *
     * @param realm       Realm holding the objects.
     * @param transaction transaction object for the current Realm.
     * @param table       table representation of the Realm class
     */
    RealmObjectSchema(BaseRealm realm, ImplicitTransaction transaction, Table table, Map<String, Long> columnIndices) {
        this.realm = realm;
        this.transaction = transaction;
        this.table = table;
        this.columnIndices = (columnIndices != null) ? columnIndices : new DynamicColumnMap(table);
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
     * instead.
     *
     * @param fieldName name of the field to add.
     * @param fieldType type of field to add. See {@link RealmObject} for the full list.
     * @param modifiers set of modifiers for this field.
     * @return the updated schema.
     * @throws IllegalArgumentException if the type isn't supported, field name is illegal or a field with that name
     * already exists.
     */
    public RealmObjectSchema addField(String fieldName, Class<?> fieldType, RealmModifier... modifiers) {
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
        boolean nullable = metadata.defaultNullable && !containsModifier(modifiers, RealmModifier.REQUIRED);
        long columnIndex = table.addColumn(metadata.realmType, fieldName, nullable);
        addModifiers(columnIndex, modifiers);
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
     *                                  index defined.
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
     * Checks if a given field has a {@link io.realm.annotations.Index} defined.
     *
     * @param fieldName existing field name to check.
     * @return {@code true} if field is indexed, {@code false} otherwise.
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
     *                                  has a primary key defined.
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
     * @param required  {@code true} if field should be required, {@code false} otherwise.
     * @return the updated schema.
     * @throws IllegalArgumentException if field name doesn't exists or the field already has the given required flag.
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
     * Sets a field to be nullable, i.e. it should be to hold {@code null values}.
     *
     * @param fieldName name of field in the schema.
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
    public Set<String> getFieldNames() {
        int columnCount = (int) table.getColumnCount();
        Set<String> columnNames = new LinkedHashSet<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(table.getColumnName(i));
        }
        return columnNames;
    }

    /**
     * Transform each RealmObject in the Realm that have this schema.
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
    // TODO: Refactor to avoid 4xsearches.
    private void addModifiers(long columnIndex, RealmModifier[] modifiers) {
        if (modifiers != null && modifiers.length > 0) {
            if (containsModifier(modifiers, RealmModifier.INDEXED)) {
                table.addSearchIndex(columnIndex);
            }

            if (containsModifier(modifiers, RealmModifier.PRIMARY_KEY)) {
                table.setPrimaryKey(columnIndex);
                table.addSearchIndex(columnIndex);
            }
        }
    }

    private boolean containsModifier(RealmModifier[] modifiers, RealmModifier modifier) {
        if (modifiers == null || modifiers.length == 0) {
            return false;
        }
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] == modifier) {
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
     * Returns the column indices for the given field name. If a linked field is defined, the column index for
     * each
     *
     * @param fieldDescription fieldName or link path to a field name.
     * @param validColumnTypes Legal field type for the last field a
     * @return
     */
    // TODO: consider another caching strategy so linked classes are included in the cache.
    long[] getColumnIndices(String fieldDescription, RealmFieldType... validColumnTypes) {
        if (fieldDescription == null || fieldDescription.equals("")) {
            throw new IllegalArgumentException("Non-empty fieldname must be provided");
        }
        Table table = this.table;
        boolean checkColumnType = validColumnTypes != null && validColumnTypes.length > 0;
        if (containsDot(fieldDescription)) {

            // Resolve field description down to last field name
            String[] names = splitString(fieldDescription); //fieldName.split("\\.");
            long[] columnIndices = new long[names.length];
            for (int i = 0; i < names.length - 1; i++) {
                long index = table.getColumnIndex(names[i]);
                if (index < 0) {
                    throw new IllegalArgumentException("Invalid query: " + names[i] + " does not refer to a class.");
                }
                RealmFieldType type = table.getColumnType(index);
                if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
                    table = table.getLinkTarget(index);
                    columnIndices[i] = index;
                } else {
                    throw new IllegalArgumentException("Invalid query: " + names[i] + " does not refer to a class.");
                }
            }

            // Check if last field name is a valid field
            String columnName = names[names.length - 1];
            long columnIndex = table.getColumnIndex(columnName);
            columnIndices[names.length - 1] = columnIndex;
            if (columnIndex < 0) {
                throw new IllegalArgumentException(columnName + " is not a field name in class " + table.getName());
            }
            if (checkColumnType && !isValidType(table.getColumnType(columnIndex), validColumnTypes)) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch.", names[names.length - 1]));
            }
            return columnIndices;
        } else {
            if (getFieldIndex(fieldDescription) == null) {
                throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldDescription));
            }
            RealmFieldType tableColumnType = table.getColumnType(getFieldIndex(fieldDescription));
            if (checkColumnType && !isValidType(tableColumnType, validColumnTypes)) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch. Was %s, expected %s.",
                        fieldDescription, tableColumnType, Arrays.toString(validColumnTypes)));
            }
            return new long[] {getFieldIndex(fieldDescription)};
        }
    }

    private boolean containsDot(String s) {
        return s.indexOf('.') != -1;
    }

    private String[] splitString(String s) {
        int i, j, n;

        // count the number of .
        n = 0;
        for (i = 0; i < s.length(); i++)
            if (s.charAt(i) == '.')
                n++;

        // split at .
        String[] arr = new String[n+1];
        i = 0;
        n = 0;
        j = s.indexOf('.');
        while (j != -1) {
            arr[n] = s.substring(i, j);
            i = j+1;
            j = s.indexOf('.', i);
            n++;
        }
        arr[n] = s.substring(s.lastIndexOf('.')+1);

        return arr;
    }

    private boolean isValidType(RealmFieldType columnType, RealmFieldType[] validColumnTypes) {
        for (int i = 0; i < validColumnTypes.length; i++) {
            if (validColumnTypes[i] == columnType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the underlying table backing this schema.
     * @return The underlying {@link Table}
     */
    Table getTable() {
        return table;
    }

    /**
     * Returns the column index in the underlying table for the given field name.
     * INVARIANT: fieldName should be present
     *
     * @param fieldName
     * @return
     */
    Long getFieldIndex(String fieldName) {
        return columnIndices.get(fieldName);
    }

    /**
     * Transformer interface for traversing all objects with the current schema and apply a transformation on each.
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


    // FIXME Replace with Schema when it is available
    private static class DynamicColumnMap implements Map<String, Long> {
        private final Table table;

        public DynamicColumnMap(Table table) {
            this.table = table;
        }

        @Override
        public Long get(Object key) {
            return table.getColumnIndex((String) key);
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
