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
package io.realm.internal;

import java.util.Arrays;

import io.realm.RealmFieldType;


/**
 * Class describing a single field possible several links away.
 */
public class FieldDescriptor {

    private long[] columnIndices;
    private RealmFieldType fieldType;
    private String fieldName;
    private boolean searchIndex;

    public FieldDescriptor(Table table, String fieldDescription, boolean allowLink, boolean allowList) {
        if (fieldDescription == null || fieldDescription.isEmpty()) {
            throw new IllegalArgumentException("Non-empty field name must be provided");
        }
        if (fieldDescription.startsWith(".") || fieldDescription.endsWith(".")) {
            throw new IllegalArgumentException("Illegal field name. It cannot start or end with a '.': " + fieldDescription);
        }
        if (fieldDescription.contains(".")) {
            // Resolves field description down to last field name
            String[] names = fieldDescription.split("\\.");
            long[] columnIndices = new long[names.length];
            for (int i = 0; i < names.length - 1; i++) {
                long index = table.getColumnIndex(names[i]);
                if (index == Table.NO_MATCH) {
                    throw new IllegalArgumentException(
                            String.format("Invalid field name: '%s' does not refer to a class.", names[i]));
                }
                RealmFieldType type = table.getColumnType(index);
                if (!allowLink && type == RealmFieldType.OBJECT) {
                    throw new IllegalArgumentException(
                            String.format("'RealmObject' field '%s' is not a supported link field here.", names[i]));
                } else if (!allowList && type == RealmFieldType.LIST) {
                    throw new IllegalArgumentException(
                            String.format("'RealmList' field '%s' is not a supported link field here.", names[i]));
                } else if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
                    table = table.getLinkTarget(index);
                    columnIndices[i] = index;
                } else {
                    throw new IllegalArgumentException(
                            String.format("Invalid field name: '%s' does not refer to a class.", names[i]));
                }
            }

            // Check if last field name is a valid field
            String columnName = names[names.length - 1];
            long columnIndex = table.getColumnIndex(columnName);
            columnIndices[names.length - 1] = columnIndex;
            if (columnIndex == Table.NO_MATCH) {
                throw new IllegalArgumentException(
                        String.format("'%s' is not a field name in class '%s'.", columnName, table.getName()));
            }

            this.fieldType = table.getColumnType(columnIndex);
            this.fieldName = columnName;
            this.columnIndices = columnIndices;
            this.searchIndex = table.hasSearchIndex(columnIndex);
        } else {
            long fieldIndex = table.getColumnIndex(fieldDescription);
            if (fieldIndex == Table.NO_MATCH) {
                throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldDescription));
            }
            this.fieldType = table.getColumnType(fieldIndex);
            this.fieldName = fieldDescription;
            this.columnIndices = new long[] {fieldIndex};
            this.searchIndex = table.hasSearchIndex(fieldIndex);
        }
    }

    public long[] getColumnIndices() {
        return Arrays.copyOf(columnIndices, columnIndices.length);
    }

    public RealmFieldType getFieldType() {
        return fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean hasSearchIndex() {
        return searchIndex;
    }
}
