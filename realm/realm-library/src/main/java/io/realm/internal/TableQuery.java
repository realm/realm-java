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

import android.util.Log;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.Case;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.internal.objectstore.OsKeyPathMapping;
import io.realm.log.RealmLog;


public class TableQuery implements NativeObject {
    private final long nativeArgumentList;

    private static final boolean DEBUG = false;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    // See documentation in that NativeContext for an explanation of how this is used
    @SuppressWarnings("unused")
    private final NativeContext context;

    private final Table table;
    private long nativePtr;

    private QueryBuilder queryBuilder = new QueryBuilder();

    private @Nullable
    OsKeyPathMapping mapping;

    // TODO: Can we protect this?
    public TableQuery(NativeContext context,
            Table table,
            long nativeQueryPtr,
            @Nullable OsKeyPathMapping mapping) {
        if (DEBUG) {
            RealmLog.debug("New TableQuery: ptr=%x", nativeQueryPtr);
        }
        this.context = context;
        this.table = table;
        this.mapping = mapping;
        this.nativePtr = nativeQueryPtr;
        this.nativeArgumentList = nativeCreateArgumentList();

        context.addReference(this);
    }

    public TableQuery(NativeContext context,
            Table table,
            long nativeQueryPtr) {
        this(context, table, nativeQueryPtr, null);
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
    void validateQuery() {
        if (!queryBuilder.isValidated()) {
            String predicate = queryBuilder.build();
            boolean isOrConnected = queryBuilder.isOrConnected();
            queryBuilder = new QueryBuilder();

            // Realm.log
            Log.d("PREDICATE", predicate);

            nativeRawPredicate(nativePtr,
                    isOrConnected,
                    predicate,
                    nativeArgumentList,
                    (mapping != null) ? mapping.getNativePtr() : 0);
        }
    }

    // Grouping

    public void group() {
        queryBuilder.beingGroup();
    }

    public void endGroup() {
        queryBuilder.endGroup();
    }

    public void or() {
        queryBuilder.or();
    }

    public void not() {
        queryBuilder.not();
    }

    public void sort(String[] fieldNames, Sort[] sortOrders) {
        queryBuilder.sort(fieldNames, sortOrders);
    }

    public void distinct(String[] fieldNames) {
        queryBuilder.distinct(fieldNames);
    }

    public void limit(long limit) {
        queryBuilder.limit(limit);
    }

    // Queries for integer values.

    public void predicate(String fieldName, long value) {
        long position = nativeAddIntegerArgument(nativeArgumentList, value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, long value) {
        long position = nativeAddIntegerArgument(nativeArgumentList, value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, long value) {
        long position = nativeAddIntegerArgument(nativeArgumentList, value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, long value) {
        long position = nativeAddIntegerArgument(nativeArgumentList, value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, long value) {
        long position = nativeAddIntegerArgument(nativeArgumentList, value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, long value) {
        long position = nativeAddIntegerArgument(nativeArgumentList, value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, long value1, long value2) {
        long position1 = nativeAddIntegerArgument(nativeArgumentList, value1);
        long position2 = nativeAddIntegerArgument(nativeArgumentList, value2);

        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Queries for float values.

    public void equalTo(String fieldName, float value) {
        long position = nativeAddFloatArgument(nativeArgumentList, value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, float value) {
        long position = nativeAddFloatArgument(nativeArgumentList, value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, float value) {
        long position = nativeAddFloatArgument(nativeArgumentList, value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, float value) {
        long position = nativeAddFloatArgument(nativeArgumentList, value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, float value) {
        long position = nativeAddFloatArgument(nativeArgumentList, value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, float value) {
        long position = nativeAddFloatArgument(nativeArgumentList, value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, float value1, float value2) {
        long position1 = nativeAddFloatArgument(nativeArgumentList, value1);
        long position2 = nativeAddFloatArgument(nativeArgumentList, value2);
        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Queries for double values.

    public void equalTo(String fieldName, double value) {
        long position = nativeAddDoubleArgument(nativeArgumentList, value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, double value) {
        long position = nativeAddDoubleArgument(nativeArgumentList, value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, double value) {
        long position = nativeAddDoubleArgument(nativeArgumentList, value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, double value) {
        long position = nativeAddDoubleArgument(nativeArgumentList, value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, double value) {
        long position = nativeAddDoubleArgument(nativeArgumentList, value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, double value) {
        long position = nativeAddDoubleArgument(nativeArgumentList, value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, double value1, double value2) {
        long position1 = nativeAddDoubleArgument(nativeArgumentList, value1);
        long position2 = nativeAddDoubleArgument(nativeArgumentList, value2);

        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Query for boolean values.

    public void equalTo(String fieldName, boolean value) {
        long position = nativeAddBooleanArgument(nativeArgumentList, value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    // Queries for Date values.

    private static final String DATE_NULL_ERROR_MESSAGE = "Date value in query criteria must not be null.";

    public void equalTo(String fieldName, @Nullable Date value) {
        long position = nativeAddDateArgument(nativeArgumentList, value.getTime());
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, Date value) {
        long position = nativeAddDateArgument(nativeArgumentList, value.getTime());
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, Date value) {
        long position = nativeAddDateArgument(nativeArgumentList, value.getTime());
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, Date value) {
        long position = nativeAddDateArgument(nativeArgumentList, value.getTime());
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, Date value) {
        long position = nativeAddDateArgument(nativeArgumentList, value.getTime());
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, Date value) {
        long position = nativeAddDateArgument(nativeArgumentList, value.getTime());
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, Date value1, Date value2) {
        long position1 = nativeAddDateArgument(nativeArgumentList, value1.getTime());
        long position2 = nativeAddDateArgument(nativeArgumentList, value2.getTime());
        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    public void between(String fieldName, Decimal128 value1, Decimal128 value2) {
        long position1 = nativeAddDecimal128Argument(nativeArgumentList, value1.getLow(), value1.getHigh());
        long position2 = nativeAddDecimal128Argument(nativeArgumentList, value2.getLow(), value2.getHigh());
        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Queries for Binary values.

    public void equalTo(String fieldName, byte[] value) {
        long position = nativeAddByteArrayArgument(nativeArgumentList, value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, byte[] value) {
        long position = nativeAddByteArrayArgument(nativeArgumentList, value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    // Equals
    public void equalTo(String fieldName, @Nullable String value, Case caseSensitive) {
        long position = nativeAddStringArgument(nativeArgumentList, value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendEqualTo(fieldName, position);
        } else {
            queryBuilder.appendEqualToNotSensitive(fieldName, position);
        }
    }

    public void equalTo(String fieldName, String value) {
        equalTo(fieldName, value, Case.SENSITIVE);
    }

    // Not Equals
    public void notEqualTo(String fieldName, @Nullable String value, Case caseSensitive) {
        long position = nativeAddStringArgument(nativeArgumentList, value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendNotEqualTo(fieldName, position);
        } else {
            queryBuilder.appendNotEqualToNotSensitive(fieldName, position);
        }
    }

    public void beginsWith(String fieldName, String value, Case caseSensitive) {
        long position = nativeAddStringArgument(nativeArgumentList, value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendBeginsWith(fieldName, position);
        } else {
            queryBuilder.appendBeginsWithNotSensitive(fieldName, position);
        }
    }

    public void endsWith(String fieldName, String value, Case caseSensitive) {
        long position = nativeAddStringArgument(nativeArgumentList, value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendEndsWith(fieldName, position);
        } else {
            queryBuilder.appendEndsWithNotSensitive(fieldName, position);
        }
    }

    public void endsWith(String fieldName, String value) {
        endsWith(fieldName, value, Case.SENSITIVE);
    }

    public void like(String fieldName, String value, Case caseSensitive) {
        long position = nativeAddStringArgument(nativeArgumentList, value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendLike(fieldName, position);
        } else {
            queryBuilder.appendLikeNotSensitive(fieldName, position);
        }
    }

    public void contains(String fieldName, String value, Case caseSensitive) {
        long position = nativeAddStringArgument(nativeArgumentList, value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendContains(fieldName, position);
        } else {
            queryBuilder.appendContainsNotSensitive(fieldName, position);
        }
    }

    public void isEmpty(String fieldName) {
//        nativeIsEmpty(nativePtr, columnKeys, tablePtrs);
        // TODO: RAW QUERY
    }

    public void isNotEmpty(String fieldName) {
        // TODO: RAW QUERY
    }

    // Queries for Decimal128

    public void equalTo(String fieldName, Decimal128 value) {
        long position = nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, Decimal128 value) {
        long position = nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void lessThan(String fieldName, Decimal128 value) {
        long position = nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, Decimal128 value) {
        long position = nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void greaterThan(String fieldName, Decimal128 value) {
        long position = nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, Decimal128 value) {
        long position = nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }


    // Queries for ObjectId

    public void equalTo(String fieldName, ObjectId value) {
        long position = nativeAddObjectIdArgument(nativeArgumentList, value.toString());
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, ObjectId value) {
        long position = nativeAddObjectIdArgument(nativeArgumentList, value.toString());
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void lessThan(String fieldName, ObjectId value) {
        long position = nativeAddObjectIdArgument(nativeArgumentList, value.toString());
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, ObjectId value) {
        long position = nativeAddObjectIdArgument(nativeArgumentList, value.toString());
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void greaterThan(String fieldName, ObjectId value) {
        long position = nativeAddObjectIdArgument(nativeArgumentList, value.toString());
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, ObjectId value) {
        long position = nativeAddObjectIdArgument(nativeArgumentList, value.toString());
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    // Queries for UUID

    public void equalTo(String fieldName, UUID value) {
        long position = nativeAddUUIDArgument(nativeArgumentList, value.toString());
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, UUID value) {
        long position = nativeAddUUIDArgument(nativeArgumentList, value.toString());
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void lessThan(String fieldName, UUID value) {
        long position = nativeAddUUIDArgument(nativeArgumentList, value.toString());
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, UUID value) {
        long position = nativeAddUUIDArgument(nativeArgumentList, value.toString());
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void greaterThan(String fieldName, UUID value) {
        long position = nativeAddUUIDArgument(nativeArgumentList, value.toString());
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, UUID value) {
        long position = nativeAddUUIDArgument(nativeArgumentList, value.toString());
        queryBuilder.appendGreaterThanEquals(fieldName, position);
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

    // isNull and isNotNull
    public void isNull(String fieldName) {
        queryBuilder.isNull(fieldName);
    }

    public void isNotNull(String fieldName) {
        queryBuilder.isNotNull(fieldName);
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

    public void rawPredicate(String filter, @Nullable OsKeyPathMapping mapping, Object[] args) {
        long listPtr = nativeCreateArgumentList();

        for (int i = 0; i < args.length; i++) {
            Object argument = args[i];
            if (argument == null) {
                nativeAddNullArgument(listPtr);
            } else if (argument instanceof Boolean) {
                nativeAddBooleanArgument(listPtr, (Boolean) argument);
            } else if (argument instanceof Float) {
                nativeAddFloatArgument(listPtr, (Float) argument);
            } else if (argument instanceof Double) {
                nativeAddDoubleArgument(listPtr, (Double) argument);
            } else if (argument instanceof Number) {
                Number value = (Number) argument;
                nativeAddIntegerArgument(listPtr, value.longValue());
            } else if (argument instanceof String) {
                nativeAddStringArgument(listPtr, (String) argument);
            } else if (argument instanceof byte[]) {
                nativeAddByteArrayArgument(listPtr, (byte[]) argument);
            } else if (argument instanceof Date) {
                nativeAddDateArgument(listPtr, ((Date) argument).getTime());
            } else if (argument instanceof Decimal128) {
                Decimal128 value = (Decimal128) argument;
                nativeAddDecimal128Argument(listPtr, value.getLow(), value.getHigh());
            } else if (argument instanceof ObjectId) {
                ObjectId value = (ObjectId) argument;
                nativeAddObjectIdArgument(listPtr, value.toString());
            } else if (argument instanceof UUID) {
                UUID value = (UUID) argument;
                nativeAddUUIDArgument(listPtr, value.toString());
            } else if (argument instanceof RealmModel) {
                RealmModel value = (RealmModel) argument;

                if (!RealmObject.isValid(value) || !RealmObject.isManaged(value)) {
                    throw new IllegalArgumentException("Argument[" + i + "] is not a valid managed object.");
                }

                RealmObjectProxy proxy = (RealmObjectProxy) value;
                UncheckedRow row = (UncheckedRow) proxy.realmGet$proxyState().getRow$realm();
                nativeAddObjectArgument(listPtr, row.getNativePtr());
            } else {
                throw new IllegalArgumentException("Unsupported query argument type: " + argument.getClass().getSimpleName());
            }
        }

        nativeRawPredicate(nativePtr,
                false,
                filter,
                listPtr,
                (mapping != null) ? mapping.getNativePtr() : 0
        );

        nativeDestroyArgumentList(listPtr);
    }

    public long remove() {
        validateQuery();
        if (table.isImmutable()) { throwImmutable(); }
        return nativeRemove(nativePtr);
    }

    private void throwImmutable() {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }

    public void alwaysTrue() {
        queryBuilder.alwaysTrue();
    }

    public void alwaysFalse() {
        queryBuilder.alwaysFalse();
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

    private static native long nativeCreateArgumentList();

    private static native void nativeDestroyArgumentList(long listPtr);

    private static native long nativeAddNullArgument(long listPtr);

    private static native long nativeAddIntegerArgument(long listPtr, long val);

    private static native long nativeAddStringArgument(long listPtr, String val);

    private static native long nativeAddFloatArgument(long listPtr, float val);

    private static native long nativeAddDoubleArgument(long listPtr, double val);

    private static native long nativeAddBooleanArgument(long listPtr, boolean val);

    private static native long nativeAddByteArrayArgument(long listPtr, byte[] val);

    private static native long nativeAddDateArgument(long listPtr, long val);

    private static native long nativeAddDecimal128Argument(long listPtr, long low, long high);

    private static native long nativeAddObjectIdArgument(long listPtr, String data);

    private static native long nativeAddUUIDArgument(long listPtr, String data);

    private static native long nativeAddObjectArgument(long listPtr, long rowPtr);

    private static native void nativeRawPredicate(long nativeQueryPtr, boolean isOrConnected, String filter, long argsPtr, long mappingPtr);

    private static native long nativeGetFinalizerPtr();
}
