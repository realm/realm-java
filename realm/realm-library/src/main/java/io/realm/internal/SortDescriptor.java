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
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.RealmFieldType;
import io.realm.Sort;
import io.realm.internal.fields.FieldDescriptor;


/**
 * Java class to present the same name core class in Java. This can be converted to a cpp realm::SortDescriptor object
 * through realm::_impl::JavaSortDescriptor.
 * <p>
 * NOTE: Since the column indices are determined when constructing the object with the given table's status, the indices
 * could be wrong when schema changes. Always create and consume the instance when needed, DON'T store a SortDescriptor
 * and use it whenever the ShareGroup can be in different versions.
 * <p>
 * Sort descriptors do not support Linking Objects, either internally or as terminal types.
 */
@Keep
public class SortDescriptor {
    //@VisibleForTesting
    final static Set<RealmFieldType> SORT_VALID_FIELD_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.FLOAT, RealmFieldType.DOUBLE,
            RealmFieldType.STRING, RealmFieldType.DATE)));

    //@VisibleForTesting
    final static Set<RealmFieldType> DISTINCT_VALID_FIELD_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            RealmFieldType.BOOLEAN, RealmFieldType.INTEGER, RealmFieldType.STRING, RealmFieldType.DATE)));

    public static SortDescriptor getInstanceForSort(FieldDescriptor.SchemaProxy proxy, Table table, String fieldDescription, Sort sortOrder) {
        return getInstanceForSort(proxy, table, new String[] {fieldDescription}, new Sort[] {sortOrder});
    }

    public static SortDescriptor getInstanceForSort(FieldDescriptor.SchemaProxy proxy, Table table, String[] fieldDescriptions, Sort[] sortOrders) {
        //noinspection ConstantConditions
        if (sortOrders == null || sortOrders.length == 0) {
            throw new IllegalArgumentException("You must provide at least one sort order.");
        }
        if (fieldDescriptions.length != sortOrders.length) {
            throw new IllegalArgumentException("Number of fields and sort orders do not match.");
        }
        return getInstance(proxy, table, fieldDescriptions, sortOrders, FieldDescriptor.OBJECT_LINK_FIELD_TYPE, SORT_VALID_FIELD_TYPES, "Sort is not supported");
    }

    public static SortDescriptor getInstanceForDistinct(FieldDescriptor.SchemaProxy proxy, Table table, String fieldDescription) {
        return getInstanceForDistinct(proxy, table, new String[] {fieldDescription});
    }

    public static SortDescriptor getInstanceForDistinct(FieldDescriptor.SchemaProxy proxy, Table table, String[] fieldDescriptions) {
        return getInstance(proxy, table, fieldDescriptions, null, FieldDescriptor.NO_LINK_FIELD_TYPE, DISTINCT_VALID_FIELD_TYPES, "Distinct is not supported");
    }

    private static SortDescriptor getInstance(
            FieldDescriptor.SchemaProxy proxy,
            Table table,
            String[] fieldDescriptions,
            @Nullable Sort[] sortOrders,
            Set<RealmFieldType> legalInternalTypes,
            Set<RealmFieldType> legalTerminalTypes,
            String message) {

        //noinspection ConstantConditions
        if (fieldDescriptions == null || fieldDescriptions.length == 0) {
            throw new IllegalArgumentException("You must provide at least one field name.");
        }

        long[][] columnIndices = new long[fieldDescriptions.length][];

        // Force aggressive parsing of the FieldDescriptors, so that only valid SortDescriptor objects are created.
        for (int i = 0; i < fieldDescriptions.length; i++) {
            FieldDescriptor descriptor = FieldDescriptor.createFieldDescriptor(proxy, table, fieldDescriptions[i], legalInternalTypes, null);
            checkFieldType(descriptor, legalTerminalTypes, message, fieldDescriptions[i]);
            columnIndices[i] = descriptor.getColumnIndices();
        }

        return new SortDescriptor(table, columnIndices, sortOrders);
    }

    // Internal use only. For JNI testing.
    //@VisibleForTesting
    static SortDescriptor getTestInstance(Table table, long[] columnIndices) {
        return new SortDescriptor(table, new long[][] {columnIndices}, null);
    }

    // could do this in the field descriptor, but this provides a better error message
    private static void checkFieldType(FieldDescriptor descriptor, Set<RealmFieldType> legalTerminalTypes, String message, String fieldDescriptions) {
        if (!legalTerminalTypes.contains(descriptor.getFinalColumnType())) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "%s on '%s' field '%s' in '%s'.", message, descriptor.getFinalColumnType(), descriptor.getFinalColumnName(), fieldDescriptions));
        }
    }


    private final Table table;
    private final long[][] columnIndices;
    private final boolean[] ascendings;

    private SortDescriptor(Table table, long[][] columnIndices, @Nullable Sort[] sortOrders) {
        this.table = table;
        this.columnIndices = columnIndices;
        if (sortOrders != null) {
            ascendings = new boolean[sortOrders.length];
            for (int i = 0; i < sortOrders.length; i++) {
                ascendings[i] = sortOrders[i].getValue();
            }
        } else {
            ascendings = null;
        }
    }

    // Called by JNI.
    @SuppressWarnings("unused")
    long[][] getColumnIndices() {
        return columnIndices;
    }

    // Called by JNI.
    @SuppressWarnings("unused")
    boolean[] getAscendings() {
        return ascendings;
    }

    // Called by JNI.
    @SuppressWarnings("unused")
    private long getTablePtr() {
        return table.getNativePtr();
    }
}
