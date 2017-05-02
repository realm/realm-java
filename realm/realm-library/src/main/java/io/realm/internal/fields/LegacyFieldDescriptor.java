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

import io.realm.RealmFieldType;
import io.realm.internal.Table;


/**
 * This class was copy/pasta from the code from which DynamicFieldDescriptor was derived.
 * As a duplicate, it did not get the improvements made in DynamicFieldDescriptor.
 * TODO: It should just go away.
 */
@Deprecated
class LegacyFieldDescriptor extends FieldDescriptor {
    private final Table table;
    private final String fieldDescription;
    private final boolean allowLink;
    private final boolean allowList;

    private long[] columnIndices;
    private RealmFieldType fieldType;
    private String fieldName;
    private boolean searchIndex;

    LegacyFieldDescriptor(Table table, String fieldDescription, boolean allowLink, boolean allowList) {
        this.table = table;
        this.fieldDescription = fieldDescription;
        this.allowLink = allowLink;
        this.allowList = allowList;
    }

    @Override
    public int length() {
        if (null == columnIndices) {
            compileFieldDescription();
        }
        return columnIndices.length;
    }

    @Override
    public long[] getColumnIndices() {
        if (null == columnIndices) {
            compileFieldDescription();
        }
        return Arrays.copyOf(columnIndices, columnIndices.length);
    }

    @Override
    public long[] getNativeTablePointers() {
        if (null == columnIndices) {
            compileFieldDescription();
        }
        return new long[columnIndices.length];
    }

    @Override
    public RealmFieldType getFieldType() {
        if (null == columnIndices) {
            compileFieldDescription();
        }
        return fieldType;
    }

    @Override
    public String getFieldName() {
        if (null == columnIndices) {
            compileFieldDescription();
        }
        return fieldName;
    }

    @Override
    public boolean hasSearchIndex() {
        if (null == columnIndices) {
            compileFieldDescription();
        }
        return searchIndex;
    }

    private void compileFieldDescription() {
        Table currentTable = table;

        if (fieldDescription == null || fieldDescription.isEmpty()) {
            throw new IllegalArgumentException("Non-empty field name must be provided");
        }
        if (fieldDescription.startsWith(".") || fieldDescription.endsWith(".")) {
            throw new IllegalArgumentException("Illegal field name. It cannot start or end with a '.': " + fieldDescription);
        }

        if (!fieldDescription.contains(".")) {
            long fieldIndex = currentTable.getColumnIndex(fieldDescription);
            if (fieldIndex == Table.NO_MATCH) {
                throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldDescription));
            }
            this.columnIndices = new long[] {fieldIndex};
            this.fieldType = currentTable.getColumnType(fieldIndex);
            this.fieldName = fieldDescription;
            this.searchIndex = currentTable.hasSearchIndex(fieldIndex);
        } else {
            // Resolves field description down to last field name
            String[] names = fieldDescription.split("\\.");
            long[] columnIndices = new long[names.length];
            for (int i = 0; i < names.length - 1; i++) {
                long index = currentTable.getColumnIndex(names[i]);
                if (index == Table.NO_MATCH) {
                    throw new IllegalArgumentException(
                            String.format("Invalid field name: '%s' does not refer to a class.", names[i]));
                }
                RealmFieldType type = currentTable.getColumnType(index);
                if (!allowLink && type == RealmFieldType.OBJECT) {
                    throw new IllegalArgumentException(
                            String.format("'RealmObject' field '%s' is not a supported link field here.", names[i]));
                } else if (!allowList && type == RealmFieldType.LIST) {
                    throw new IllegalArgumentException(
                            String.format("'RealmList' field '%s' is not a supported link field here.", names[i]));
                } else if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
                    currentTable = currentTable.getLinkTarget(index);
                    columnIndices[i] = index;
                } else {
                    throw new IllegalArgumentException(
                            String.format("Invalid field name: '%s' does not refer to a class.", names[i]));
                }
            }

            // Check if last field name is a valid field
            String columnName = names[names.length - 1];
            long columnIndex = currentTable.getColumnIndex(columnName);
            columnIndices[names.length - 1] = columnIndex;
            if (columnIndex == Table.NO_MATCH) {
                throw new IllegalArgumentException(
                        String.format("'%s' is not a field name in class '%s'.", columnName, table.getName()));
            }

            this.columnIndices = columnIndices;
            this.fieldType = currentTable.getColumnType(columnIndex);
            this.fieldName = columnName;
            this.searchIndex = currentTable.hasSearchIndex(columnIndex);
        }
    }
}
