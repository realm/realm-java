/*
 * Copyright 2014 Realm Inc.
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

import org.bson.types.Decimal128;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.RealmAny;
import io.realm.RealmAnyNativeFunctionsImpl;
import io.realm.Sort;
import io.realm.internal.core.NativeRealmAny;
import io.realm.internal.objectstore.OsKeyPathMapping;
import io.realm.log.RealmLog;


public class TableQuery implements NativeObject {
    private static final boolean DEBUG = false;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final Table table;
    private final long nativePtr;

    private final RealmAnyNativeFunctionsImpl realmAnyNativeFunctions = new RealmAnyNativeFunctionsImpl();

    private boolean queryValidated = true;

    private static String escapeFieldName(@Nullable String fieldName) {
        if (fieldName == null) { return null; }
        return fieldName.replace(" ", "\\ ");
    }

    public TableQuery(NativeContext context,
            Table table,
            long nativeQueryPtr) {
        if (DEBUG) {
            RealmLog.debug("New TableQuery: ptr=%x", nativeQueryPtr);
        }
        this.table = table;
        this.nativePtr = nativeQueryPtr;

        context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public Table getTable() {
        return table;
    }

    /**
     * Checks in core if query syntax is valid. Throws exception, if not.
     */
    public void validateQuery() {
        if (!queryValidated) {
            String invalidMessage = nativeValidateQuery(nativePtr);
            if ("".equals(invalidMessage)) {
                queryValidated = true; // If empty string error message, query is valid
            } else { throw new UnsupportedOperationException(invalidMessage); }
        }
    }

    // Grouping

    public TableQuery beginGroup() {
        nativeBeginGroup(nativePtr);
        queryValidated = false;
        return this;
    }

    public TableQuery endGroup() {
        nativeEndGroup(nativePtr);
        queryValidated = false;
        return this;
    }

    public TableQuery or() {
        nativeOr(nativePtr);
        queryValidated = false;
        return this;
    }

    public TableQuery not() {
        nativeNot(nativePtr);
        queryValidated = false;
        return this;
    }

    public static String buildSortDescriptor(String[] fieldNames, Sort[] sortOrders) {
        StringBuilder descriptorBuilder = new StringBuilder("SORT(");

        String sortSeparator = "";
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];

            descriptorBuilder.append(sortSeparator)
                    .append(escapeFieldName(fieldName))
                    .append(" ")
                    .append((sortOrders[i] == Sort.ASCENDING) ? "ASC" : "DESC");

            sortSeparator = ", ";
        }

        descriptorBuilder.append(")");

        return descriptorBuilder.toString();
    }

    public TableQuery sort(@Nullable OsKeyPathMapping mapping, String[] fieldNames, Sort[] sortOrders) {
        String descriptor = buildSortDescriptor(fieldNames, sortOrders);
        rawDescriptor(mapping, descriptor);
        return this;
    }

    public static String buildDistinctDescriptor(String[] fieldNames) {
        StringBuilder descriptorBuilder = new StringBuilder("DISTINCT(");

        String distinctSeparator = "";
        for (String fieldName : fieldNames) {
            descriptorBuilder.append(distinctSeparator)
                    .append(escapeFieldName(fieldName));

            distinctSeparator = ", ";
        }

        descriptorBuilder.append(")");

        return descriptorBuilder.toString();
    }

    public TableQuery distinct(@Nullable OsKeyPathMapping mapping, String[] fieldNames) {
        String descriptor = buildDistinctDescriptor(fieldNames);
        rawDescriptor(mapping, descriptor);
        return this;
    }

    public TableQuery limit(long limit) {
        rawDescriptor(null, "LIMIT(" + limit + ")");
        return this;
    }

    public TableQuery isEmpty(@Nullable OsKeyPathMapping mapping, String fieldName) {
        rawPredicateWithPointers(mapping, escapeFieldName(fieldName) + ".@count = 0");
        queryValidated = false;
        return this;
    }

    public TableQuery isNotEmpty(@Nullable OsKeyPathMapping mapping, String fieldName) {
        rawPredicateWithPointers(mapping, escapeFieldName(fieldName) + ".@count != 0");
        queryValidated = false;
        return this;
    }

    public TableQuery rawPredicate(@Nullable OsKeyPathMapping mapping, String predicate, RealmAny... args) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, predicate, args);
        return this;
    }

    public void rawPredicateWithPointers(@Nullable OsKeyPathMapping mapping, String predicate, long... values) {
        nativeRawPredicate(nativePtr,
                predicate,
                values,
                (mapping != null) ? mapping.getNativePtr() : 0);
    }

    private void rawDescriptor(@Nullable OsKeyPathMapping mapping, String descriptor) {
        nativeRawDescriptor(nativePtr,
                descriptor,
                (mapping != null) ? mapping.getNativePtr() : 0);
    }

    public TableQuery equalTo(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " = $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " != $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery equalToInsensitive(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " =[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualToInsensitive(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " !=[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " > $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " >= $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " < $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " <= $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value1, RealmAny value2) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, "(" + escapeFieldName(fieldName) + " >= $0 AND " + escapeFieldName(fieldName) + " <= $1)", value1, value2);
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWith(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " BEGINSWITH $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWithInsensitive(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " BEGINSWITH[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " ENDSWITH $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery endsWithInsensitive(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " ENDSWITH[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery like(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " LIKE $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery likeInsensitive(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " LIKE[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery contains(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " CONTAINS $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery containsInsensitive(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + " CONTAINS[c] $0", value);
        queryValidated = false;
        return this;
    }

    // Dictionary queries

    public TableQuery containsKey(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny key) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, "ANY " + escapeFieldName(fieldName) + ".@keys == $0", key);
        queryValidated = false;
        return this;
    }

    public TableQuery containsValue(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, "ANY " + escapeFieldName(fieldName) + ".@values == $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery containsEntry(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny key, RealmAny value) {
        realmAnyNativeFunctions.callRawPredicate(this, mapping, escapeFieldName(fieldName) + "[$0] == $1", key, value);
        queryValidated = false;
        return this;
    }

    // isNull and isNotNull
    public TableQuery isNull(@Nullable OsKeyPathMapping mapping, String fieldName) {
        rawPredicateWithPointers(mapping, escapeFieldName(fieldName) + " = NULL");
        queryValidated = false;
        return this;
    }

    public TableQuery isNotNull(@Nullable OsKeyPathMapping mapping, String fieldName) {
        rawPredicateWithPointers(mapping, escapeFieldName(fieldName) + " != NULL");
        queryValidated = false;
        return this;
    }

    public TableQuery alwaysTrue() {
        rawPredicateWithPointers(null, "TRUEPREDICATE");
        queryValidated = false;
        return this;
    }

    public TableQuery alwaysFalse() {
        rawPredicateWithPointers(null, "FALSEPREDICATE");
        queryValidated = false;
        return this;
    }

    public TableQuery in(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny[] values) {
        fieldName = escapeFieldName(fieldName);

        beginGroup();

        boolean first = true;
        for (RealmAny value : values) {
            if (!first) { or(); }
            if (value == null) {
                isNull(mapping, fieldName);
            } else {
                equalTo(mapping, fieldName, value);
            }
            first = false;
        }
        endGroup();

        queryValidated = false;
        return this;
    }

    public TableQuery inInsensitive(@Nullable OsKeyPathMapping mapping, String fieldName, RealmAny[] values) {
        fieldName = escapeFieldName(fieldName);

        beginGroup();

        boolean first = true;
        for (RealmAny value : values) {
            if (!first) { or(); }
            if (value == null) {
                isNull(mapping, fieldName);
            } else {
                equalToInsensitive(mapping, fieldName, value);
            }
            first = false;
        }
        endGroup();

        queryValidated = false;
        return this;
    }

    // Searching methods.

    /**
     * Returns the table row index for the first element matching the query.
     */
    public long find() {
        validateQuery();
        return nativeFind(nativePtr);
    }

    //
    // Aggregation methods
    //

    // Integer aggregation

    public long sumInt(long columnKey) {
        validateQuery();
        return nativeSumInt(nativePtr, columnKey);
    }

    public Long maximumInt(long columnKey) {
        validateQuery();
        return nativeMaximumInt(nativePtr, columnKey);
    }

    public Long minimumInt(long columnKey) {
        validateQuery();
        return nativeMinimumInt(nativePtr, columnKey);
    }

    public double averageInt(long columnKey) {
        validateQuery();
        return nativeAverageInt(nativePtr, columnKey);
    }

    // Float aggregation

    public double sumFloat(long columnKey) {
        validateQuery();
        return nativeSumFloat(nativePtr, columnKey);
    }

    public Float maximumFloat(long columnKey) {
        validateQuery();
        return nativeMaximumFloat(nativePtr, columnKey);
    }

    public Float minimumFloat(long columnKey) {
        validateQuery();
        return nativeMinimumFloat(nativePtr, columnKey);
    }

    public double averageFloat(long columnKey) {
        validateQuery();
        return nativeAverageFloat(nativePtr, columnKey);
    }

    // Double aggregation

    public double sumDouble(long columnKey) {
        validateQuery();
        return nativeSumDouble(nativePtr, columnKey);
    }

    public Decimal128 sumDecimal128(long columnKey) {
        validateQuery();
        long[] data = nativeSumDecimal128(nativePtr, columnKey);
        if (data != null) {
            return Decimal128.fromIEEE754BIDEncoding(data[1]/*high*/, data[0]/*low*/);
        } else {
            return null;
        }
    }

    public Double maximumDouble(long columnKey) {
        validateQuery();
        return nativeMaximumDouble(nativePtr, columnKey);
    }

    public Double minimumDouble(long columnKey) {
        validateQuery();
        return nativeMinimumDouble(nativePtr, columnKey);
    }

    public double averageDouble(long columnKey) {
        validateQuery();
        return nativeAverageDouble(nativePtr, columnKey);
    }

    // RealmAny aggregation
    public Decimal128 sumRealmAny(long columnKey) {
        validateQuery();
        long[] data =  nativeSumRealmAny(nativePtr, columnKey);
        return Decimal128.fromIEEE754BIDEncoding(data[1]/*high*/, data[0]/*low*/);
    }

    public NativeRealmAny maximumRealmAny(long columnKey) {
        validateQuery();
        return nativeMaximumRealmAny(nativePtr, columnKey);
    }

    public NativeRealmAny minimumRealmAny(long columnKey) {
        validateQuery();
        return nativeMinimumRealmAny(nativePtr, columnKey);
    }

    public Decimal128 averageRealmAny(long columnKey) {
        validateQuery();
        long[] data =  nativeAverageRealmAny(nativePtr, columnKey);
        return Decimal128.fromIEEE754BIDEncoding(data[1]/*high*/, data[0]/*low*/);
    }

    public Decimal128 averageDecimal128(long columnKey) {
        validateQuery();
        long[] result = nativeAverageDecimal128(nativePtr, columnKey);
        if (result != null) {
            return Decimal128.fromIEEE754BIDEncoding(result[1]/*high*/, result[0]/*low*/);
        }
        return null;
    }

    public Decimal128 maximumDecimal128(long columnKey) {
        validateQuery();
        long[] result = nativeMaximumDecimal128(nativePtr, columnKey);
        if (result != null) {
            return Decimal128.fromIEEE754BIDEncoding(result[1]/*high*/, result[0]/*low*/);
        }
        return null;
    }

    // Date aggregation

    public Date maximumDate(long columnKey) {
        validateQuery();
        Long result = nativeMaximumTimestamp(nativePtr, columnKey);
        if (result != null) {
            return new Date(result);
        }
        return null;
    }

    public Date minimumDate(long columnKey) {
        validateQuery();
        Long result = nativeMinimumTimestamp(nativePtr, columnKey);
        if (result != null) {
            return new Date(result);
        }
        return null;
    }

    public Decimal128 minimumDecimal128(long columnKey) {
        validateQuery();
        long[] result = nativeMinimumDecimal128(nativePtr, columnKey);
        if (result != null) {
            return Decimal128.fromIEEE754BIDEncoding(result[1]/*high*/, result[0]/*low*/);
        }
        return null;
    }

    // Count

    /**
     * Returns only the number of matching objects.
     * This method is very fast compared to evaluating a query completely, but it does not
     * goes around any logic implemented in Object Store and other parts of the API that works
     * on query results. So the primary use case for this method is testing.
     */
    @Deprecated
    public long count() {
        validateQuery();
        return nativeCount(nativePtr);
    }

    public long remove() {
        validateQuery();
        if (table.isImmutable()) { throwImmutable(); }
        return nativeRemove(nativePtr);
    }

    private void throwImmutable() {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }

    private native long nativeFind(long nativeQueryPtr);

    private native long nativeSumInt(long nativeQueryPtr, long columnKey);

    private native Long nativeMaximumInt(long nativeQueryPtr, long columnKey);

    private native Long nativeMinimumInt(long nativeQueryPtr, long columnKey);

    private native double nativeAverageInt(long nativeQueryPtr, long columnKey);

    private native double nativeSumFloat(long nativeQueryPtr, long columnKey);

    private native Float nativeMaximumFloat(long nativeQueryPtr, long columnKey);

    private native Float nativeMinimumFloat(long nativeQueryPtr, long columnKey);

    private native double nativeAverageFloat(long nativeQueryPtr, long columnKey);

    private native long[] nativeSumRealmAny(long nativeQueryPtr, long columnKey);

    private native double nativeSumDouble(long nativeQueryPtr, long columnKey);

    private native long[] nativeSumDecimal128(long nativeQueryPtr, long columnKey);

    private native Double nativeMaximumDouble(long nativeQueryPtr, long columnKey);

    private native NativeRealmAny nativeMaximumRealmAny(long nativeQueryPtr, long columnKey);

    private native long[] nativeMaximumDecimal128(long nativeQueryPtr, long columnKey);

    private native NativeRealmAny nativeMinimumRealmAny(long nativeQueryPtr, long columnKey);

    private native Double nativeMinimumDouble(long nativeQueryPtr, long columnKey);

    private native long[] nativeMinimumDecimal128(long nativeQueryPtr, long columnKey);

    private native long[] nativeAverageRealmAny(long nativeQueryPtr, long columnKey);

    private native double nativeAverageDouble(long nativeQueryPtr, long columnKey);

    private native long[] nativeAverageDecimal128(long nativeQueryPtr, long columnKey);

    private native Long nativeMaximumTimestamp(long nativeQueryPtr, long columnKey);

    private native Long nativeMinimumTimestamp(long nativeQueryPtr, long columnKey);

    private native long nativeCount(long nativeQueryPtr);

    private native long nativeRemove(long nativeQueryPtr);

    private native void nativeRawPredicate(long nativeQueryPtr, String filter, long[] argsPtr, long mappingPtr);

    private native void nativeRawDescriptor(long nativeQueryPtr, String descriptor, long mappingPtr);

    private native void nativeBeginGroup(long nativeQueryPtr);

    private native void nativeEndGroup(long nativeQueryPtr);

    private native void nativeOr(long nativeQueryPtr);

    private native void nativeNot(long nativeQueryPtr);

    private native String nativeValidateQuery(long nativeQueryPtr);

    private static native long nativeGetFinalizerPtr();
}
