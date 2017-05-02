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

import java.util.Arrays;
import java.util.List;

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
    private final List<String> fields;
    private final RealmFieldType[] validColumnTypes;

    private long[] columnIndices;
    private long[] tableNativePointers;

    /**
     * @param schema the associated Realm Schema
     * @param className the starting Table: where(Table.class)
     * @param fieldDescription fieldName or link path to a field name.
     * @param validColumnTypes valid field type for the last field in a linked field
     */
    CachedFieldDescriptor(SchemaProxy schema, String className, String fieldDescription, RealmFieldType... validColumnTypes) {
        this.fields = parseFieldDescription(fieldDescription);
        int nFields = fields.size();
        if (nFields <= 0) {
            throw new IllegalArgumentException("Invalid query: Empty field descriptor");
        }
        this.validColumnTypes = validColumnTypes;
        this.className = className;
        this.schema = schema;
    }


    @Override
    public int length() {
        return fields.size();
    }

    @Override
    public long[] getColumnIndices() {
        if (columnIndices == null) {
            compileFieldDescription();
        }
        return Arrays.copyOf(columnIndices, columnIndices.length);
    }

    @Override
    public long[] getNativeTablePointers() {
        if (tableNativePointers == null) {
            compileFieldDescription();
        }
        return Arrays.copyOf(tableNativePointers, tableNativePointers.length);
    }

    @Override
    public RealmFieldType getFieldType() {
        throw new UnsupportedOperationException("Cached FieldDescriptors do not support getFieldType");
    }

    @Override
    public String getFieldName() {
        throw new UnsupportedOperationException("Cached FieldDescriptors do not support getFieldName");
    }

    @Override
    public boolean hasSearchIndex() {
        throw new UnsupportedOperationException("Cached FieldDescriptors do not support hasSearchIndex");
    }

    private void compileFieldDescription() {
        final int nFields = fields.size();
        columnIndices = new long[nFields];
        tableNativePointers = new long[nFields];
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
                        String.format("Invalid query: table '%s' not found in this schema.", currentTable));
            }

            columnIndex = tableInfo.getColumnIndex(columnName);
            if (columnIndex < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid query: field '%s' not found in table '%s'.", columnName, currentTable));
            }

            columnType = tableInfo.getColumnType(columnName);
            // all but the last field must be a link type
            if (i < nFields - 1) {
                verifyColumnType(currentTable, columnName, columnType, RealmFieldType.OBJECT, RealmFieldType.LIST, RealmFieldType.LINKING_OBJECTS);
                currentTable = tableInfo.getLinkedTable(columnName);
            }
            columnIndices[i] = columnIndex;
            tableNativePointers[i] = (columnType != RealmFieldType.LINKING_OBJECTS)
                    ? NativeObject.NULLPTR
                    : schema.getNativeTablePtr(currentTable);
        }

        verifyColumnType(className, columnName, columnType, validColumnTypes);
    }
}
