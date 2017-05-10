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
import java.util.Set;

import io.realm.RealmFieldType;
import io.realm.internal.Table;


/**
 * A field descriptor that uses dynamic table lookup.
 * Use when cache cannot be trusted...
 */
class DynamicFieldDescriptor extends FieldDescriptor {
    private final Table table;

    /**
     * Build a dynamic field descriptor for the passed field description string.
     *
     * @param table the start table.
     * @param fieldDescription the field description.
     * @param validInternalColumnTypes valid types for the last field in the field description.
     * @param validFinalColumnTypes valid types for the last field in the field description.
     */
    DynamicFieldDescriptor(Table table, String fieldDescription, Set<RealmFieldType> validInternalColumnTypes, Set<RealmFieldType> validFinalColumnTypes) {
        super(fieldDescription, validInternalColumnTypes, validFinalColumnTypes);
        this.table = table;
    }

    @Override
    protected void compileFieldDescription(List<String> fields) {
        final int nFields = fields.size();
        long[] columnIndices = new long[nFields];
        Table currentTable = table;

        long columnIndex;
        String tableName = null;
        String columnName = null;
        RealmFieldType columnType = null;
        for (int i = 0; i < nFields; i++) {
            columnName = fields.get(i);
            if ((columnName == null) || (columnName.length() <= 0)) {
                throw new IllegalArgumentException(
                        "Invalid query: Field descriptor contains an empty field.  A field description may not begin with or contain adjacent periods ('.').");
            }

            tableName = currentTable.getClassName();

            columnIndex = currentTable.getColumnIndex(columnName);
            if (columnIndex < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid query: field '%s' not found in table '%s'.", columnName, tableName));
            }

            columnType = currentTable.getColumnType(columnIndex);
            if (i < nFields - 1) {
                verifyInternalColumnType(tableName, columnName, columnType);
                currentTable = currentTable.getLinkTarget(columnIndex);
            }

            columnIndices[i] = columnIndex;
        }

        setCompilationResults(tableName, columnName, columnType, columnIndices, new long[nFields]);
    }
}
