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

    // TODO: Can we protect this?
    public TableQuery(NativeContext context,
            Table table,
            long nativeQueryPtr) {
        if (DEBUG) {
            RealmLog.debug("New TableQuery: ptr=%x", nativeQueryPtr);
        }
        this.context = context;
        this.table = table;
        this.nativePtr = nativeQueryPtr;
        this.nativeArgumentList = nativeCreateArgumentList();

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
        if (!queryBuilder.isValidated()) {
            boolean isOrConnected = queryBuilder.isOrConnected();
            String predicate = queryBuilder.build();

            // Realm.log
            Log.d("PREDICATE", predicate);

            OsKeyPathMapping mapping = table.getOsKeyPathMapping();

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

    private long addLongArgument(@Nullable Number value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddIntegerArgument(nativeArgumentList, value.longValue());
    }

    public void equalTo(String fieldName, @Nullable Number value) {
        long position = addLongArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable Number value) {
        long position = addLongArgument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, @Nullable Number value) {
        long position = addLongArgument(value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, @Nullable Number value) {
        long position = addLongArgument(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, @Nullable Number value) {
        long position = addLongArgument(value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, @Nullable Number value) {
        long position = addLongArgument(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, @Nullable Number value1, @Nullable Number value2) {
        long position1 = addLongArgument(value1);
        long position2 = addLongArgument(value2);

        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Queries for float values.

    private long addFloatArgument(@Nullable Float value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddFloatArgument(nativeArgumentList, value);
    }

    public void equalTo(String fieldName, @Nullable Float value) {
        long position = addFloatArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable Float value) {
        long position = addFloatArgument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, @Nullable Float value) {
        long position = addFloatArgument(value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, @Nullable Float value) {
        long position = addFloatArgument(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, @Nullable Float value) {
        long position = addFloatArgument(value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, @Nullable Float value) {
        long position = addFloatArgument(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, @Nullable Float value1, @Nullable Float value2) {
        long position1 = addFloatArgument(value1);
        long position2 = addFloatArgument(value2);
        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Queries for double values.

    private long addDoubleArgument(@Nullable Double value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddDoubleArgument(nativeArgumentList, value);
    }

    public void equalTo(String fieldName, @Nullable Double value) {
        long position = addDoubleArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable Double value) {
        long position = addDoubleArgument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, @Nullable Double value) {
        long position = addDoubleArgument(value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, @Nullable Double value) {
        long position = addDoubleArgument(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, @Nullable Double value) {
        long position = addDoubleArgument(value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, @Nullable Double value) {
        long position = addDoubleArgument(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, @Nullable Double value1, @Nullable Double value2) {
        long position1 = addDoubleArgument(value1);
        long position2 = addDoubleArgument(value2);

        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Query for boolean values.

    private long addBooleanArgument(@Nullable Boolean value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddBooleanArgument(nativeArgumentList, value);
    }

    public void equalTo(String fieldName, @Nullable Boolean value) {
        long position = addBooleanArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable Boolean value) {
        long position = addBooleanArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    // Queries for Date values.

    private long addDateArgument(@Nullable Date value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddDateArgument(nativeArgumentList, value.getTime());
    }

    public void equalTo(String fieldName, @Nullable Date value) {
        long position = addDateArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, Date value) {
        long position = addDateArgument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void greaterThan(String fieldName, Date value) {
        long position = addDateArgument(value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, Date value) {
        long position = addDateArgument(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    public void lessThan(String fieldName, Date value) {
        long position = addDateArgument(value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, Date value) {
        long position = addDateArgument(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void between(String fieldName, Date value1, Date value2) {
        long position1 = addDateArgument(value1);
        long position2 = addDateArgument(value2);
        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    // Queries for Binary values.

    private long addByteArrayArgument(@Nullable byte[] value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddByteArrayArgument(nativeArgumentList, value);
    }

    public void equalTo(String fieldName, @Nullable byte[] value) {
        long position = addByteArrayArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable byte[] value) {
        long position = addByteArrayArgument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    private long addStringArgument(@Nullable String value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddStringArgument(nativeArgumentList, value);
    }

    public void equalTo(String fieldName, @Nullable String value, Case caseSensitive) {
        long position = addStringArgument(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendEqualTo(fieldName, position);
        } else {
            queryBuilder.appendEqualToNotSensitive(fieldName, position);
        }
    }

    public void equalTo(String fieldName, String value) {
        equalTo(fieldName, value, Case.SENSITIVE);
    }

    public void notEqualTo(String fieldName, String value) {
        notEqualTo(fieldName, value, Case.SENSITIVE);
    }

    // Not Equals
    public void notEqualTo(String fieldName, @Nullable String value, Case caseSensitive) {
        long position = addStringArgument(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendNotEqualTo(fieldName, position);
        } else {
            queryBuilder.appendNotEqualToNotSensitive(fieldName, position);
        }
    }

    public void beginsWith(String fieldName, String value, Case caseSensitive) {
        long position = addStringArgument(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendBeginsWith(fieldName, position);
        } else {
            queryBuilder.appendBeginsWithNotSensitive(fieldName, position);
        }
    }

    public void endsWith(String fieldName, String value, Case caseSensitive) {
        long position = addStringArgument(value);

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
        long position = addStringArgument(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendLike(fieldName, position);
        } else {
            queryBuilder.appendLikeNotSensitive(fieldName, position);
        }
    }

    public void contains(String fieldName, String value, Case caseSensitive) {
        long position = addStringArgument(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendContains(fieldName, position);
        } else {
            queryBuilder.appendContainsNotSensitive(fieldName, position);
        }
    }

    public void isEmpty(String fieldName) {
        equalTo(fieldName + ".@count", 0);
    }

    public void isNotEmpty(String fieldName) {
        notEqualTo(fieldName + ".@count", 0);
    }

    // Queries for Decimal128

    private long addDecimal128Argument(@Nullable Decimal128 value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
    }

    public void between(String fieldName, @Nullable Decimal128 value1, @Nullable Decimal128 value2) {
        long position1 = addDecimal128Argument(value1);
        long position2 = addDecimal128Argument(value2);
        queryBuilder.appendBetween(fieldName, position1, position2);
    }

    public void equalTo(String fieldName, @Nullable Decimal128 value) {
        long position = addDecimal128Argument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable Decimal128 value) {
        long position = addDecimal128Argument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void lessThan(String fieldName, @Nullable Decimal128 value) {
        long position = addDecimal128Argument(value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, @Nullable Decimal128 value) {
        long position = addDecimal128Argument(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void greaterThan(String fieldName, @Nullable Decimal128 value) {
        long position = addDecimal128Argument(value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, @Nullable Decimal128 value) {
        long position = addDecimal128Argument(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }


    // Queries for ObjectId

    private long addObjectIdArgument(@Nullable ObjectId value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddObjectIdArgument(nativeArgumentList, value.toString());
    }

    public void equalTo(String fieldName, @Nullable ObjectId value) {
        long position = addObjectIdArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable ObjectId value) {
        long position = addObjectIdArgument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void lessThan(String fieldName, @Nullable ObjectId value) {
        long position = addObjectIdArgument(value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, @Nullable ObjectId value) {
        long position = addObjectIdArgument(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void greaterThan(String fieldName, @Nullable ObjectId value) {
        long position = addObjectIdArgument(value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, @Nullable ObjectId value) {
        long position = addObjectIdArgument(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
    }

    // Queries for UUID

    private long addUUIDArgument(@Nullable UUID value) {
        return (value == null) ? nativeAddNullArgument(nativeArgumentList) : nativeAddUUIDArgument(nativeArgumentList, value.toString());
    }

    public void equalTo(String fieldName, @Nullable UUID value) {
        long position = addUUIDArgument(value);
        queryBuilder.appendEqualTo(fieldName, position);
    }

    public void notEqualTo(String fieldName, @Nullable UUID value) {
        long position = addUUIDArgument(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
    }

    public void lessThan(String fieldName, @Nullable UUID value) {
        long position = addUUIDArgument(value);
        queryBuilder.appendLessThan(fieldName, position);
    }

    public void lessThanOrEqual(String fieldName, @Nullable UUID value) {
        long position = addUUIDArgument(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
    }

    public void greaterThan(String fieldName, @Nullable UUID value) {
        long position = addUUIDArgument(value);
        queryBuilder.appendGreaterThan(fieldName, position);
    }

    public void greaterThanOrEqual(String fieldName, @Nullable UUID value) {
        long position = addUUIDArgument(value);
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

    public void rawPredicate(String filter, Object[] args) {
        validateQuery();

        for (int i = 0; i < args.length; i++) {
            Object argument = args[i];
            if (argument == null) {
                nativeAddNullArgument(nativeArgumentList);
            } else if (argument instanceof Boolean) {
                nativeAddBooleanArgument(nativeArgumentList, (Boolean) argument);
            } else if (argument instanceof Float) {
                nativeAddFloatArgument(nativeArgumentList, (Float) argument);
            } else if (argument instanceof Double) {
                nativeAddDoubleArgument(nativeArgumentList, (Double) argument);
            } else if (argument instanceof Number) {
                Number value = (Number) argument;
                nativeAddIntegerArgument(nativeArgumentList, value.longValue());
            } else if (argument instanceof String) {
                nativeAddStringArgument(nativeArgumentList, (String) argument);
            } else if (argument instanceof byte[]) {
                nativeAddByteArrayArgument(nativeArgumentList, (byte[]) argument);
            } else if (argument instanceof Date) {
                nativeAddDateArgument(nativeArgumentList, ((Date) argument).getTime());
            } else if (argument instanceof Decimal128) {
                Decimal128 value = (Decimal128) argument;
                nativeAddDecimal128Argument(nativeArgumentList, value.getLow(), value.getHigh());
            } else if (argument instanceof ObjectId) {
                ObjectId value = (ObjectId) argument;
                nativeAddObjectIdArgument(nativeArgumentList, value.toString());
            } else if (argument instanceof UUID) {
                UUID value = (UUID) argument;
                nativeAddUUIDArgument(nativeArgumentList, value.toString());
            } else if (argument instanceof RealmModel) {
                RealmModel value = (RealmModel) argument;

                if (!RealmObject.isValid(value) || !RealmObject.isManaged(value)) {
                    throw new IllegalArgumentException("Argument[" + i + "] is not a valid managed object.");
                }

                RealmObjectProxy proxy = (RealmObjectProxy) value;
                UncheckedRow row = (UncheckedRow) proxy.realmGet$proxyState().getRow$realm();
                nativeAddObjectArgument(nativeArgumentList, row.getNativePtr());
            } else {
                throw new IllegalArgumentException("Unsupported query argument type: " + argument.getClass().getSimpleName());
            }
        }

        queryBuilder.appendRawPredicate(filter);

        validateQuery();
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
