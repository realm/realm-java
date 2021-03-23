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
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import io.realm.Mixed;
import io.realm.MixedNativeFunctionsImpl;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.internal.objectstore.OsKeyPathMapping;
import io.realm.log.RealmLog;


public class TableQuery implements NativeObject {
    private static final boolean DEBUG = false;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final Table table;
    private final long nativePtr;

    private final MixedNativeFunctionsImpl mixedNativeFunctions = new MixedNativeFunctionsImpl();

    private boolean queryValidated = true;

    private static String escapeFieldName(String fieldName){
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

    public static String buildSortDescriptor(String[] fieldNames, Sort[] sortOrders){
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

    public TableQuery sort(String[] fieldNames, Sort[] sortOrders) {
        String descriptor = buildSortDescriptor(fieldNames, sortOrders);
        rawDescriptor(descriptor);
        return this;
    }

    public static String buildDistinctDescriptor(String[] fieldNames){
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

    public TableQuery distinct(String[] fieldNames) {
        String descriptor = buildDistinctDescriptor(fieldNames);
        rawDescriptor(descriptor);
        return this;
    }

    public TableQuery limit(long limit) {
        rawDescriptor("LIMIT(" + limit + ")");
        return this;
    }

    public TableQuery isEmpty(String fieldName) {
        rawPredicateWithPointers(escapeFieldName(fieldName) + ".@count = 0");
        queryValidated = false;
        return this;
    }

    public TableQuery isNotEmpty(String fieldName) {
        rawPredicateWithPointers(escapeFieldName(fieldName) + ".@count != 0");
        queryValidated = false;
        return this;
    }

    public TableQuery rawPredicate(String predicate, Object... args) {
        Mixed[] mixedArgs = new Mixed[args.length];

        for (int i = 0; i < args.length; i++) {
            Object argument = args[i];
            if (argument == null) {
                mixedArgs[i] = Mixed.nullValue();
            } else if (argument instanceof Boolean) {
                mixedArgs[i] = Mixed.valueOf((Boolean) argument);
            } else if (argument instanceof Byte) {
                mixedArgs[i] = Mixed.valueOf((Byte) argument);
            } else if (argument instanceof Short) {
                mixedArgs[i] = Mixed.valueOf((Short) argument);
            } else if (argument instanceof Integer) {
                mixedArgs[i] = Mixed.valueOf((Integer) argument);
            } else if (argument instanceof Long) {
                mixedArgs[i] = Mixed.valueOf((Long) argument);
            } else if (argument instanceof Float) {
                mixedArgs[i] = Mixed.valueOf((Float) argument);
            } else if (argument instanceof Double) {
                mixedArgs[i] = Mixed.valueOf((Double) argument);
            } else if (argument instanceof Decimal128) {
                mixedArgs[i] = Mixed.valueOf((Decimal128) argument);
            } else if (argument instanceof String) {
                mixedArgs[i] = Mixed.valueOf((String) argument);
            } else if (argument instanceof byte[]) {
                mixedArgs[i] = Mixed.valueOf((byte[]) argument);
            } else if (argument instanceof Date) {
                mixedArgs[i] = Mixed.valueOf((Date) argument);
            } else if (argument instanceof ObjectId) {
                mixedArgs[i] = Mixed.valueOf((ObjectId) argument);
            } else if (argument instanceof UUID) {
                mixedArgs[i] = Mixed.valueOf((UUID) argument);
            } else if (RealmModel.class.isAssignableFrom(argument.getClass())) {
                RealmModel value = (RealmModel) argument;

                if (!RealmObject.isValid(value) || !RealmObject.isManaged(value)) {
                    throw new IllegalArgumentException("Argument[" + i + "] is not a valid managed object.");
                }

                mixedArgs[i] = Mixed.valueOf((RealmModel) argument);
            } else {
                throw new IllegalArgumentException("Unsupported query argument type: " + argument.getClass().getSimpleName());
            }
        }

        mixedNativeFunctions.callRawPredicate(this, predicate, mixedArgs);

        return this;
    }

    public void rawPredicateWithPointers(String predicate, long... values) {
        OsKeyPathMapping mapping = table.getOsKeyPathMapping();

        nativeRawPredicate(nativePtr,
                predicate,
                values,
                (mapping != null) ? mapping.getNativePtr() : 0);
    }

    private void rawDescriptor(String descriptor) {
        OsKeyPathMapping mapping = table.getOsKeyPathMapping();

        nativeRawDescriptor(nativePtr,
                descriptor,
                (mapping != null) ? mapping.getNativePtr() : 0);
    }

    public TableQuery equalTo(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " = $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " != $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery equalToInsensitive(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " =[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualToInsensitive(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " !=[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " > $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " >= $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " < $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " <= $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(String fieldName, Mixed value1, Mixed value2) {
        mixedNativeFunctions.callRawPredicate(this, "(" + escapeFieldName(fieldName) + " >= $0 AND " + escapeFieldName(fieldName) + " <= $1)", value1, value2);
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWith(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " BEGINSWITH $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWithInsensitive(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " BEGINSWITH[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " ENDSWITH $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery endsWithInsensitive(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " ENDSWITH[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery like(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " LIKE $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery likeInsensitive(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " LIKE[c] $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery contains(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " CONTAINS $0", value);
        queryValidated = false;
        return this;
    }

    public TableQuery containsInsensitive(String fieldName, Mixed value) {
        mixedNativeFunctions.callRawPredicate(this, escapeFieldName(fieldName) + " CONTAINS[c] $0", value);
        queryValidated = false;
        return this;
    }

    // isNull and isNotNull
    public TableQuery isNull(String fieldName) {
        rawPredicateWithPointers(escapeFieldName(fieldName) + " = NULL");
        queryValidated = false;
        return this;
    }

    public TableQuery isNotNull(String fieldName) {
        rawPredicateWithPointers(escapeFieldName(fieldName) + " != NULL");
        queryValidated = false;
        return this;
    }

    public TableQuery alwaysTrue() {
        rawPredicateWithPointers("TRUEPREDICATE");
        queryValidated = false;
        return this;
    }

    public TableQuery alwaysFalse() {
        rawPredicateWithPointers("FALSEPREDICATE");
        queryValidated = false;
        return this;
    }

    public TableQuery in(String fieldName, Mixed[] values) {
        fieldName = escapeFieldName(fieldName);

        beginGroup().equalTo(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            if(values[i] == null){
                or().isNull(fieldName);
            } else {
                or().equalTo(fieldName, values[i]);
            }
        }
        endGroup();

        queryValidated = false;
        return this;
    }

    public TableQuery inInsensitive(String fieldName, Mixed[] values) {
        fieldName = escapeFieldName(fieldName);

        beginGroup().equalToInsensitive(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            if(values[i] == null){
                or().isNull(fieldName);
            } else {
                or().equalToInsensitive(fieldName, values[i]);
            }
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

    private native double nativeSumDouble(long nativeQueryPtr, long columnKey);

    private native long[] nativeSumDecimal128(long nativeQueryPtr, long columnKey);

    private native Double nativeMaximumDouble(long nativeQueryPtr, long columnKey);

    private native long[] nativeMaximumDecimal128(long nativeQueryPtr, long columnKey);

    private native Double nativeMinimumDouble(long nativeQueryPtr, long columnKey);

    private native long[] nativeMinimumDecimal128(long nativeQueryPtr, long columnKey);

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
