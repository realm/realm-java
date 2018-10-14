/*
 * Copyright 2016 Realm Inc.
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
package io.realm.internal.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import io.realm.RealmFieldType;
import io.realm.internal.ColumnInfo;
import io.realm.internal.Table;


/**
 * Class describing a single field, possibly several links away, e.g.:
 * <ul>
 * </li> "someField"
 * </li> "someRealmObjectField.someField"
 * </li> "someRealmListField.someField"
 * </li> "someLinkingObjectField.someField"
 * </li> "someRealmObjectField.someRealmListField.someLinkingObjectField.someField"
 * </ul>
 */
public abstract class FieldDescriptor {

    private static final Pattern FIELD_SEPARATOR = Pattern.compile("\\.");

    public interface SchemaProxy {
        boolean hasCache();

        ColumnInfo getColumnInfo(String tableName);

        long getNativeTablePtr(String targetTable);
    }

    public static final Set<RealmFieldType> ALL_LINK_FIELD_TYPES;

    static {
        Set<RealmFieldType> s = new HashSet<>(3);
        s.add(RealmFieldType.OBJECT);
        s.add(RealmFieldType.LIST);
        s.add(RealmFieldType.LINKING_OBJECTS);
        ALL_LINK_FIELD_TYPES = Collections.unmodifiableSet(s);
    }

    public static final Set<RealmFieldType> SIMPLE_LINK_FIELD_TYPES;

    static {
        Set<RealmFieldType> s = new HashSet<>(2);
        s.add(RealmFieldType.OBJECT);
        s.add(RealmFieldType.LIST);
        SIMPLE_LINK_FIELD_TYPES = Collections.unmodifiableSet(s);
    }

    public static final Set<RealmFieldType> LIST_LINK_FIELD_TYPE;

    static {
        Set<RealmFieldType> s = new HashSet<>(1);
        s.add(RealmFieldType.LIST);
        LIST_LINK_FIELD_TYPE = Collections.unmodifiableSet(s);
    }

    public static final Set<RealmFieldType> OBJECT_LINK_FIELD_TYPE;

    static {
        Set<RealmFieldType> s = new HashSet<>(1);
        s.add(RealmFieldType.OBJECT);
        OBJECT_LINK_FIELD_TYPE = Collections.unmodifiableSet(s);
    }

    public static final Set<RealmFieldType> NO_LINK_FIELD_TYPE = Collections.emptySet();

    /**
     * Convenience method to allow var-arg specification of valid final column types
     *
     * @param schema Proxy to schema info
     * @param table the start table
     * @param fieldDescription dot-separated column names
     * @param validFinalColumnTypes legal types for the last column
     * @return the Field descriptor
     */
    public static FieldDescriptor createStandardFieldDescriptor(
            SchemaProxy schema,
            Table table,
            String fieldDescription,
            RealmFieldType... validFinalColumnTypes) {

        return createFieldDescriptor(
                schema,
                table,
                fieldDescription,
                null,
                new HashSet<>(Arrays.asList(validFinalColumnTypes))
        );
    }

    /**
     * Factory method for field descriptors.
     *
     * @param schema Proxy to schema info
     * @param table the start table
     * @param fieldDescription dot-separated column names
     * @param validFinalColumnTypes legal types for the last column
     * @return the Field descriptor
     * <p>
     * TODO:
     * I suspect that choosing the parsing strategy based on whether there is a ref to a ColumnIndices
     * around or not, is bad architecture.  Almost certainly, there should be a schema that has
     * ColumnIndices and one that does not and the strategies below should belong to the first
     * and second, respectively.  --gbm
     */
    public static FieldDescriptor createFieldDescriptor(
            SchemaProxy schema,
            Table table,
            String fieldDescription,
            Set<RealmFieldType> validInternalColumnTypes,
            Set<RealmFieldType> validFinalColumnTypes) {
        return ((schema == null) || !schema.hasCache())
                ? new DynamicFieldDescriptor(table, fieldDescription, (null != validInternalColumnTypes) ? validInternalColumnTypes : SIMPLE_LINK_FIELD_TYPES, validFinalColumnTypes)
                : new CachedFieldDescriptor(schema, table.getClassName(), fieldDescription, (null != validInternalColumnTypes) ? validInternalColumnTypes : ALL_LINK_FIELD_TYPES, validFinalColumnTypes);
    }


    private final List<String> fields;
    private final Set<RealmFieldType> validInternalColumnTypes;
    private final Set<RealmFieldType> validFinalColumnTypes;

    private String finalColumnName;
    private RealmFieldType finalColumnType;
    private long[] columnIndices;
    private long[] nativeTablePointers;

    /**
     * @param fieldDescription fieldName or link path to a field name.
     * @param validInternalColumnTypes valid internal link types.
     * @param validFinalColumnTypes valid field types for the last field in a linked field
     */
    protected FieldDescriptor(
            String fieldDescription, Set<RealmFieldType>
            validInternalColumnTypes,
            Set<RealmFieldType> validFinalColumnTypes) {
        this.fields = parseFieldDescription(fieldDescription);
        int nFields = fields.size();
        if (nFields <= 0) {
            throw new IllegalArgumentException("Invalid query: Empty field descriptor");
        }
        this.validInternalColumnTypes = validInternalColumnTypes;
        this.validFinalColumnTypes = validFinalColumnTypes;
    }

    /**
     * The number of columnNames in the field description.
     * The returned number is the size of the array returned by
     * {@code getColumnIndices} and {@code getNativeTablePointers}
     *
     * @return the number of fields.
     */
    public final int length() {
        return fields.size();
    }

    /**
     * Return a java array of column indices for the columns named in the description.
     * If the column at ret[i] is a LinkingObjects column, ret[i] (the column index)
     * is the index for the <b>source</b> column in the <b>source</b> table.
     *
     * The return is an array because it will be, immediately, passed to native code
     *
     * @return an array of column indices.
     */
    public final long[] getColumnIndices() {
        compileIfNecessary();
        return Arrays.copyOf(columnIndices, columnIndices.length);
    }

    /**
     * Return a java array of native table pointers.  For most columns the table will be identified by
     * the type of the column: no further information is needed.  In that case, this array will contain
     * NativeObject.NULLPTR.  If, however, a column is a LinkingObjects column the <b>source</b> table
     * cannot be inferred, so the returned array contains the native pointer to it.
     *
     * The return is an array because it will be, immediately, passed to native code
     *
     * @return an array of native table pointers.
     */
    public final long[] getNativeTablePointers() {
        compileIfNecessary();
        return Arrays.copyOf(nativeTablePointers, nativeTablePointers.length);
    }

    /**
     * Getter for the name of the final column in the descriptor.
     *
     * @return the name of the final column
     */
    public final String getFinalColumnName() {
        compileIfNecessary();
        return finalColumnName;
    }

    /**
     * Getter for the type of the final column in the descriptor.
     *
     * @return the type of the final column
     */
    public final RealmFieldType getFinalColumnType() {
        compileIfNecessary();
        return finalColumnType;
    }

    /**
     * Subclasses implement this method with a compilation strategy.
     */
    protected abstract void compileFieldDescription(List<String> fields);

    /**
     * Verify that the named link column, in the named table, of the specified type, is one of the legal internal column types.
     *
     * @param tableName Name of the table containing the column: used in error messages
     * @param columnName Name of the column whose type is being tested: used in error messages
     * @param columnType The type of the column: examined for validity.
     */
    protected final void verifyInternalColumnType(String tableName, String columnName, RealmFieldType columnType) {
        verifyColumnType(tableName, columnName, columnType, validInternalColumnTypes);
    }

    /**
     * Store the results of compiling the field description.
     * Subclasses call this as the last action in
     *
     * @param finalClassName the name of the final table in the field description.
     * @param finalColumnName the name of the final column in the field description.
     * @param finalColumnType the type of the final column in the field description: MAY NOT BE {@code null}!
     * @param columnIndices the array of columnIndices.
     * @param nativeTablePointers the array of table pointers
     */
    protected final void setCompilationResults(
            String finalClassName,
            String finalColumnName,
            RealmFieldType finalColumnType,
            long[] columnIndices,
            long[] nativeTablePointers) {
        if ((validFinalColumnTypes != null) && (validFinalColumnTypes.size() > 0)) {
            verifyColumnType(finalClassName, finalColumnName, finalColumnType, validFinalColumnTypes);
        }
        this.finalColumnName = finalColumnName;
        this.finalColumnType = finalColumnType;
        this.columnIndices = columnIndices;
        this.nativeTablePointers = nativeTablePointers;
    }

    /**
     * Parse the passed field description into its components.
     * This must be standard across implementations and is, therefore, implemented in the base class.
     *
     * @param fieldDescription a field description.
     * @return the parse tree: a list of column names
     */
    private List<String> parseFieldDescription(String fieldDescription) {
        if (fieldDescription == null || fieldDescription.equals("")) {
            throw new IllegalArgumentException("Invalid query: field name is empty");
        }

        int lastDotIndex = fieldDescription.lastIndexOf(".");
        if (lastDotIndex == fieldDescription.length() - 1) {
            throw new IllegalArgumentException("Invalid query: field name must not end with a period ('.')");
        }

        if (lastDotIndex > -1) {
            return Arrays.asList(FIELD_SEPARATOR.split(fieldDescription));
        } else {
            return Collections.singletonList(fieldDescription);
        }
    }

    private void verifyColumnType(String className, String columnName, RealmFieldType columnType, Set<RealmFieldType> validTypes) {
        if (!validTypes.contains(columnType)) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "Invalid query: field '%s' in class '%s' is of invalid type '%s'.",
                    columnName, className, columnType.toString()));
        }
    }

    private void compileIfNecessary() {
        if (finalColumnType == null) {
            compileFieldDescription(fields);
        }
    }
}
