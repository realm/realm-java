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
import java.util.List;

import io.realm.RealmFieldType;
import io.realm.Sort;


/**
 * Java class to present the same name core class in Java. This can be converted to a cpp realm::SortDescriptor object
 * through realm::_impl::JavaSortDescriptor.
 * <p>
 * NOTE: Since the column indices are determined when constructing the object with the given table's status, the indices
 * could be wrong when schema changes. Always create and consume the instance when needed, DON'T store a SortDescriptor
 * and use it whenever the ShareGroup can be in different versions.
 */
@KeepMember
public class SortDescriptor {

    private final long[][] columnIndices;
    private final boolean[] ascendings;
    private final Table table;

    final static List<RealmFieldType> validFieldTypesForSort = Arrays.asList(
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.FLOAT, RealmFieldType.DOUBLE,
            RealmFieldType.STRING, RealmFieldType.DATE);
    final static List<RealmFieldType> validFieldTypesForDistinct = Arrays.asList(
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.STRING, RealmFieldType.DATE);

    // Internal use only. For JNI testing.
    SortDescriptor(Table table, long[] columnIndices) {
        this(table, new long[][] {columnIndices}, null);
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
        this.table = table;
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
            FieldDescriptor descriptor = new FieldDescriptor(table, fieldDescriptions[i], true, false);
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
            FieldDescriptor descriptor = new FieldDescriptor(table, fieldDescriptions[i], false, false);
            checkFieldTypeForDistinct(descriptor, fieldDescriptions[i]);
            columnIndices[i] = descriptor.getColumnIndices();
        }

        return new SortDescriptor(table, columnIndices, null);
    }

    private static void checkFieldTypeForSort(FieldDescriptor descriptor, String fieldDescriptions) {
        if (!validFieldTypesForSort.contains(descriptor.getFieldType())) {
            throw new IllegalArgumentException(String.format(
                    "Sort is not supported on '%s' field '%s' in '%s'.", descriptor.toString(), descriptor.getFieldName(),
                    fieldDescriptions));
        }
    }

    private static void checkFieldTypeForDistinct(FieldDescriptor descriptor, String fieldDescriptions) {
        if (!validFieldTypesForDistinct.contains(descriptor.getFieldType())) {
            throw new IllegalArgumentException(String.format(
                    "Distinct is not supported on '%s' field '%s' in '%s'.",
                    descriptor.getFieldType().toString(), descriptor.getFieldName(), fieldDescriptions));
        }
    }

    // Called by JNI.
    @KeepMember
    long[][] getColumnIndices() {
        return columnIndices;
    }

    // Called by JNI.
    @KeepMember
    boolean[] getAscendings() {
        return ascendings;
    }

    // Called by JNI.
    @KeepMember
    @SuppressWarnings("unused")
    private long getTablePtr() {
        return table.getNativePtr();
    }
}
