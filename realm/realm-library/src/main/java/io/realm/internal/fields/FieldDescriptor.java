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

import java.util.Arrays;
import java.util.List;

import io.realm.RealmFieldType;
import io.realm.internal.ColumnInfo;
import io.realm.internal.Table;


/**
 * Class describing a single field possible several links away.
 */
public abstract class FieldDescriptor {
    public interface SchemaProxy {
        boolean hasCache();

        ColumnInfo getColumnInfo(String tableName);

        long getNativeTablePtr(String targetTable);
    }

    @Deprecated
    public static FieldDescriptor createLegacyDescriptor(Table table, String fieldDescription, boolean allowLink, boolean allowList) {
        return new LegacyFieldDescriptor(table, fieldDescription, allowLink, allowList);
    }

    /**
     * TODO:
     * I suspect that choosing the parsing strategy based on whether there is a ref to a ColumnIndices
     * around or not, is bad architecture.  Almost certainly, there should be a schema that has
     * ColumnIndices and one that does not and the strategies below should belong to the first
     * and second, respectively.  --gbm
     */
    public static FieldDescriptor createFieldDescriptor(SchemaProxy schema, Table table, String fieldDescription, RealmFieldType[] validColumnTypes) {
        return (!schema.hasCache())
                ? new DynamicFieldDescriptor(table, fieldDescription, validColumnTypes)
                : new CachedFieldDescriptor(schema, table.getClassName(), fieldDescription, validColumnTypes);
    }


    public abstract int length();

    /**
     * After the field description (@see parseFieldDescription(String) is parsed, this method
     * returns a java array of column indices for the columns named in the description.
     * If the column is a LinkingObjects column, the index is the index in the <b>source</b>table.
     */
    public abstract long[] getColumnIndices();

    /**
     * After the field description (@see parseFieldDescription(String) is parsed, this method
     * returns a java array.  For most columns the table will be the 'current' table, so this
     * array will contain ativeObject.NULLPTR.  If a column is a LinkingObjects column, however,
     * the array contains the native pointer to the <b>source</b> table.
     */
    public abstract long[] getNativeTablePointers();

    public abstract RealmFieldType getFieldType();

    public abstract String getFieldName();

    public abstract boolean hasSearchIndex();

    /**
     * Parse the passed field description into its components.
     * This must be standard across implementations and is, therefore, implemented in the base class.
     *
     * @param fieldDescription a field description.
     * @return the parse tree: a list of column names
     */
    protected final List<String> parseFieldDescription(String fieldDescription) {
        if (fieldDescription == null || fieldDescription.equals("")) {
            throw new IllegalArgumentException("Invalid query: field name is empty");
        }
        if (fieldDescription.endsWith(".")) {
            throw new IllegalArgumentException("Invalid query: field name must not end with a period ('.')");
        }
        return Arrays.asList(fieldDescription.split("\\."));
    }

    /**
     * Verify that the named column, in the named table, of the specified type, is one of the legal column types.
     *
     * @param tableName Name of the table containing the column: used in error messages
     * @param columnName Name of the column whose type is being tested: used in error messages
     * @param columnType The type of the column: examined for validity.
     * @param validColumnTypes A list of valid column types
     */
    protected final void verifyColumnType(String tableName, String columnName, RealmFieldType columnType, RealmFieldType... validColumnTypes) {
        if ((validColumnTypes == null) || (validColumnTypes.length <= 0)) {
            return;
        }

        for (int i = 0; i < validColumnTypes.length; i++) {
            if (validColumnTypes[i] == columnType) {
                return;
            }
        }

        throw new IllegalArgumentException(String.format(
                "Invalid query: field '%s' in table '%s' is of invalid type '%s'.",
                columnName, tableName, columnType.toString()));
    }
}
