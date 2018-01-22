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
package io.realm.internal.fields;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.realm.RealmFieldType;
import io.realm.internal.ColumnInfo;
import io.realm.internal.NativeObject;

/**
 * Parses the passed field description (@see parseFieldDescription(String) and returns the information
 * necessary for RealmQuery predicates to select the specified records.
 * Because the values returned by this method will, immediately, be handed to native code, they are
 * in coordinated arrays, not a List&lt;ColumnDetails&gt;
 * There are two kinds of records.  If return[1][i] is NativeObject.NULLPTR, return[0][i] contains
 * the column index for the i-th element in the dotted field description path.
 * If return[1][i] is *not* NativeObject.NULLPTR, it is a pointer to the source table for a backlink
 * and return[0][i] is the column index of the source column in that table.
 *
 * This class only understands how to parse field descriptions consisting of Java field names as
 * given in the model classes. If a field is specified using internal column names, like e.g.
 * queries done on a {@link io.realm.DynamicRealm} use {@link DynamicFieldDescriptor} instead.
 */
class CachedFieldDescriptor extends FieldDescriptor {
    private final SchemaProxy schema;
    private final String className;

    /**
     * @param schema the associated Realm Schema
     * @param className the starting Table: where(Table.class)
     * @param fieldDescription fieldName or link path to a field name using field names from Java model classes
     */
    CachedFieldDescriptor(SchemaProxy schema, String className, String fieldDescription, Set<RealmFieldType> validInternalColumnTypes, Set<RealmFieldType> validFinalColumnTypes) {
        super(fieldDescription, validInternalColumnTypes, validFinalColumnTypes);
        this.className = className;
        this.schema = schema;
    }

    @Override
    protected void compileFieldDescription(List<String> fields) {
        final int nFields = fields.size();
        long[] columnIndices = new long[nFields];
        long[] tableNativePointers = new long[nFields];

        String currentClassName = className;
        String currentColumnName = null;
        RealmFieldType currentColumnType = null;
        for (int i = 0; i < nFields; i++) {
            currentColumnName = fields.get(i);
            if ((currentColumnName == null) || (currentColumnName.length() <= 0)) {
                throw new IllegalArgumentException(
                        "Invalid query: Field descriptor contains an empty field.  A field description may not begin with or contain adjacent periods ('.').");
            }

            final ColumnInfo columnInfo = schema.getColumnInfo(currentClassName);
            if (columnInfo == null) {
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid query: class '%s' not found in this schema.", currentClassName));
            }

            final ColumnInfo.ColumnDetails details = columnInfo.getColumnDetails(currentColumnName);
            if (details == null) {
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid query: field '%s' not found in class '%s'.", currentColumnName, currentClassName));
            }

            currentColumnType = details.columnType;
            // we don't check the type of the last field in the chain since it is done in the C++ code
            if (i < nFields - 1) {
                verifyInternalColumnType(currentClassName, currentColumnName, currentColumnType);
                currentClassName = details.linkedClassName;
            }
            columnIndices[i] = details.columnIndex;
            tableNativePointers[i] = (currentColumnType != RealmFieldType.LINKING_OBJECTS)
                    ? NativeObject.NULLPTR
                    : schema.getNativeTablePtr(details.linkedClassName);
        }

        setCompilationResults(currentClassName, currentColumnName, currentColumnType, columnIndices, tableNativePointers);
    }
}
