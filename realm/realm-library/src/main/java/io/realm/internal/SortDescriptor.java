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

import java.io.*;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmFieldType;
import io.realm.Sort;

public class SortDescriptor implements Closeable {

    private final long[][] columnIndices;
    private final boolean[] ascendings;
    private long nativePtr = 0;
    final static List<RealmFieldType> validFieldTypesForSort = Arrays.asList(
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.FLOAT, RealmFieldType.DOUBLE,
            RealmFieldType.STRING, RealmFieldType.DATE);
    final static List<RealmFieldType> validFieldTypesForDistinct = Arrays.asList(
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.STRING, RealmFieldType.DATE);

    // Internal use only. For JNI testing.
    SortDescriptor(Table table, long[] columnIndices) {
        this(table, new long[][] {columnIndices}, null);
    }

    // Internal use only. For JNI testing.
    SortDescriptor(Table table, long[] columnIndices, Sort sortOrder) {
       this(table, new long[][] {columnIndices}, new Sort[] {sortOrder});
    }

    private SortDescriptor(Table table, long[][] columnIndices, Sort[] sortOrders) {
        if (sortOrders != null) {
            ascendings = new boolean[sortOrders.length];
            for (int i = 0; i < sortOrders.length; i++) {
                ascendings[i] = sortOrders[i].getValue();
            }
        } else {
            ascendings = null;
        }

        this.columnIndices = columnIndices;
        nativePtr = nativeCreate(table.getNativePtr(), columnIndices, ascendings);
    }

    public static SortDescriptor getInstanceForSort(Table table, String fieldDescription, Sort sortOrder) {
        return getInstanceForSort(table, new String[] {fieldDescription}, new Sort[] {sortOrder});
    }

    public static SortDescriptor getInstanceForSort(Table table, String[] fieldDescriptions, Sort[] sortOrders) {
        if (fieldDescriptions == null || fieldDescriptions.length == 0) {
            throw new IllegalArgumentException("You must provide at least one field name.");
        }
        if (sortOrders == null || sortOrders.length == 0) {
            throw new IllegalArgumentException("You must provide at least one sort order.");
        }
        if (fieldDescriptions.length != sortOrders.length) {
            throw new IllegalArgumentException("Number of fields and sort orders do not match.");
        }

        long[][] columnIndices = new long[fieldDescriptions.length][];
        for (int i = 0; i < fieldDescriptions.length; i++) {
            FieldDescriptor descriptor = new FieldDescriptor(table, fieldDescriptions[i], false);
            checkFieldTypeForSort(descriptor, fieldDescriptions[i]);
            columnIndices[i] = descriptor.getColumnIndices();
        }

        return new SortDescriptor(table, columnIndices, sortOrders);
    }

    public static SortDescriptor getInstanceForDistinct(Table table, String fieldDescription) {
        return getInstanceForDistinct(table, new String[] {fieldDescription});
    }

    public static SortDescriptor getInstanceForDistinct(Table table, String[] fieldDescriptions) {
        if (fieldDescriptions == null || fieldDescriptions.length == 0) {
            throw new IllegalArgumentException("You must provide at least one field name.");
        }

        long[][] columnIndices = new long[fieldDescriptions.length][];
        for (int i = 0; i < fieldDescriptions.length; i++) {
            FieldDescriptor descriptor = new FieldDescriptor(table, fieldDescriptions[i], false);
            checkFieldTypeForDistinct(descriptor, fieldDescriptions[i]);
            columnIndices[i] = descriptor.getColumnIndices();
        }

        return new SortDescriptor(table, columnIndices, null);
    }

    public long getNativePtr() {
        return nativePtr;
    }

    private static void checkFieldTypeForSort(FieldDescriptor descriptor, String fieldDescriptions) {
        for (RealmFieldType aValidFieldTypesForSort : validFieldTypesForSort) {
            if (aValidFieldTypesForSort == descriptor.getFieldType()) {
                return;
            }
        }
        throw new IllegalArgumentException(String.format(
                "Sort is not supported on '%s' field '%s' in '%s'.", descriptor.toString(), descriptor.getFieldName(),
                fieldDescriptions));
    }

    private static void checkFieldTypeForDistinct(FieldDescriptor descriptor, String fieldDescriptions) {
        if (!validFieldTypesForDistinct.contains(descriptor.getFieldType())) {
            throw new IllegalArgumentException(String.format(
                    "Distinct is not supported on '%s' field '%s' in '%s'.",
                    descriptor.getFieldType().toString(), descriptor.getFieldName(), fieldDescriptions));
        }
        if (!descriptor.hasSearchIndex()) {
            throw new IllegalArgumentException(String.format(
                    "Field '%s' in '%s' must be indexed in order to use it for distinct queries.",
                    descriptor.getFieldName(), fieldDescriptions));
        }
    }

    long[][] getColumnIndices() {
        return columnIndices;
    }

    boolean[] getAscendings() {
        return ascendings;
    }

    @Override
    public void close() {
        nativeClose(nativePtr);
        nativePtr = 0;
    }

    private static native long nativeCreate(long tablePtr, long[][] columnIndices, boolean[] ascending);
    private static native void nativeClose(long ptr);
}
