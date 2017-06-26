package io.realm.internal.fields;
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
 * in coordinated arrays, not a List&lt;ColumnDeatils&gt;
 * There are two kinds of records.  If return[1][i] is NativeObject.NULLPTR, return[0][i] contains
 * the column index for the i-th element in the dotted field description path.
 * If return[1][i] is *not* NativeObject.NULLPTR, it is a pointer to the source table for a backlink
 * and return[0][i] is the column index of the source column in that table.
 */
class CachedFieldDescriptor extends FieldDescriptor {
    private final SchemaProxy schema;
    private final String className;

    /**
     * @param schema the associated Realm Schema
     * @param className the starting Table: where(Table.class)
     * @param fieldDescription fieldName or link path to a field name.
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
        String currentTable = className;

        ColumnInfo tableInfo;
        String columnName = null;
        RealmFieldType columnType = null;
        long columnIndex;
        for (int i = 0; i < nFields; i++) {
            columnName = fields.get(i);
            if ((columnName == null) || (columnName.length() <= 0)) {
                throw new IllegalArgumentException(
                        "Invalid query: Field descriptor contains an empty field.  A field description may not begin with or contain adjacent periods ('.').");
            }

            tableInfo = schema.getColumnInfo(currentTable);
            if (tableInfo == null) {
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid query: table '%s' not found in this schema.", currentTable));
            }

            columnIndex = tableInfo.getColumnIndex(columnName);
            if (columnIndex < 0) {
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid query: field '%s' not found in table '%s'.", columnName, currentTable));
            }

            columnType = tableInfo.getColumnType(columnName);
            if (i < nFields - 1) {
                verifyInternalColumnType(currentTable, columnName, columnType);
                currentTable = tableInfo.getLinkedTable(columnName);
            }
            columnIndices[i] = columnIndex;
            tableNativePointers[i] = (columnType != RealmFieldType.LINKING_OBJECTS)
                    ? NativeObject.NULLPTR
                    : schema.getNativeTablePtr(currentTable);
        }

        setCompilationResults(className, columnName, columnType, columnIndices, tableNativePointers);
    }
}
