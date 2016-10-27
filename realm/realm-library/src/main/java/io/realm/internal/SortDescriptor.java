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

import io.realm.RealmFieldType;
import io.realm.Sort;

public class SortDescriptor {

    private final long[][] columnIndices;
    private long nativePtr = 0;
    private final static RealmFieldType[] validFieldTypesForSort = new RealmFieldType[] {
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.FLOAT, RealmFieldType.DOUBLE,
            RealmFieldType.STRING, RealmFieldType.DATE
    };
    private final static RealmFieldType[] validFieldTypesForDistinct = new RealmFieldType[] {
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.STRING, RealmFieldType.DATE
    };

    // Internal use only. For JNI testing.
    SortDescriptor(Table table, long[] columnIndices) {
        this(table, new long[][] {columnIndices}, null);
    }

    // Internal use only. For JNI testing.
    SortDescriptor(Table table, long[] columnIndices, Sort sortOrder) {
       this(table, new long[][] {columnIndices}, new Sort[] {sortOrder});
    }

    private SortDescriptor(Table table, long[][] columnIndices, Sort[] sortOrders) {
        boolean[] ascending = null;
        if (sortOrders != null) {
            ascending = new boolean[sortOrders.length];
            for (int i = 0; i < sortOrders.length; i++) {
                ascending[i] = sortOrders[i].getValue();
            }
        }

        this.columnIndices = columnIndices;
        nativePtr = nativeCreate(table.nativePtr, columnIndices, ascending);
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
            FieldDescriptor descriptor = new FieldDescriptor(table, fieldDescriptions[i], true);
            checkFieldTypeForSort(descriptor.getLastFieldType(), descriptor.getLastFieldName(), fieldDescriptions[i]);
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
            checkFieldTypeForDistinct(
                    descriptor.getLastFieldType(), descriptor.getLastFieldName(), fieldDescriptions[i]);
            columnIndices[i] = descriptor.getColumnIndices();
        }

        return new SortDescriptor(table, columnIndices, null);
    }

    public long getNativePtr() {
        return nativePtr;
    }

    long[][] getColumnIndices() {
        return columnIndices;
    }

    private static void checkFieldTypeForSort(RealmFieldType type, String fieldName, String fieldDescriptions) {
        for (RealmFieldType aValidFieldTypesForSort : validFieldTypesForSort) {
            if (aValidFieldTypesForSort == type) {
                return;
            }
        }
        throw new IllegalArgumentException(String.format(
                "Sort is not supported on '%s' field '%s' in '%s'.", type.toString(), fieldName, fieldDescriptions));
    }

    private static void checkFieldTypeForDistinct(RealmFieldType type, String fieldName, String fieldDescriptions) {
        for (RealmFieldType aValidFieldTypesForSort : validFieldTypesForDistinct) {
            if (aValidFieldTypesForSort == type) {
                return;
            }
        }
        throw new IllegalArgumentException(String.format(
                "Distinct is not supported on '%s' field '%s' in '%s'.",
                type.toString(), fieldName, fieldDescriptions));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (nativePtr != 0) {
            nativeClose(nativePtr);
            nativePtr = 0;
        }
    }

    private static native long nativeCreate(long tablePtr, long[][] columnIndices, boolean[] ascending);
    private static native void nativeClose(long ptr);
}
