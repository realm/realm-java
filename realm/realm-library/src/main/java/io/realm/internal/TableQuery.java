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

import javax.annotation.Nullable;

import io.realm.Case;
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

    private final QueryBuilder queryBuilder = new QueryBuilder();
    private final NativeArgumentList nativeArgumentList;

    public TableQuery(NativeContext context,
            Table table,
            long nativeQueryPtr) {
        if (DEBUG) {
            RealmLog.debug("New TableQuery: ptr=%x", nativeQueryPtr);
        }
        this.table = table;
        this.nativePtr = nativeQueryPtr;
        this.nativeArgumentList = new NativeArgumentList(context);

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

            OsKeyPathMapping mapping = table.getOsKeyPathMapping();

            nativeRawPredicate(nativePtr,
                    isOrConnected,
                    predicate,
                    nativeArgumentList.getNativePtr(),
                    (mapping != null) ? mapping.getNativePtr() : 0);
        }
    }

    // Grouping

    public TableQuery group() {
        queryBuilder.beingGroup();
        return this;
    }

    public TableQuery endGroup() {
        queryBuilder.endGroup();
        return this;
    }

    public TableQuery or() {
        queryBuilder.or();
        return this;
    }

    public TableQuery not() {
        queryBuilder.not();
        return this;
    }

    public TableQuery sort(String[] fieldNames, Sort[] sortOrders) {
        queryBuilder.sort(fieldNames, sortOrders);
        return this;
    }

    public TableQuery distinct(String[] fieldNames) {
        queryBuilder.distinct(fieldNames);
        return this;
    }

    public TableQuery limit(long limit) {
        queryBuilder.limit(limit);
        return this;
    }

    // Queries for integer values.

    public TableQuery equalTo(String fieldName, @Nullable Number value) {
        long position = nativeArgumentList.insertLong(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable Number value) {
        long position = nativeArgumentList.insertLong(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery greaterThan(String fieldName, @Nullable Number value) {
        long position = nativeArgumentList.insertLong(value);
        queryBuilder.appendGreaterThan(fieldName, position);
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, @Nullable Number value) {
        long position = nativeArgumentList.insertLong(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
        return this;
    }

    public TableQuery lessThan(String fieldName, @Nullable Number value) {
        long position = nativeArgumentList.insertLong(value);
        queryBuilder.appendLessThan(fieldName, position);
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, @Nullable Number value) {
        long position = nativeArgumentList.insertLong(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
        return this;
    }

    public TableQuery between(String fieldName, @Nullable Number value1, @Nullable Number value2) {
        long position1 = nativeArgumentList.insertLong(value1);
        long position2 = nativeArgumentList.insertLong(value2);

        queryBuilder.appendBetween(fieldName, position1, position2);
        return this;
    }

    // Queries for float values.

    public TableQuery equalTo(String fieldName, @Nullable Float value) {
        long position = nativeArgumentList.insertFloat(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable Float value) {
        long position = nativeArgumentList.insertFloat(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery greaterThan(String fieldName, @Nullable Float value) {
        long position = nativeArgumentList.insertFloat(value);
        queryBuilder.appendGreaterThan(fieldName, position);
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, @Nullable Float value) {
        long position = nativeArgumentList.insertFloat(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
        return this;
    }

    public TableQuery lessThan(String fieldName, @Nullable Float value) {
        long position = nativeArgumentList.insertFloat(value);
        queryBuilder.appendLessThan(fieldName, position);
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, @Nullable Float value) {
        long position = nativeArgumentList.insertFloat(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
        return this;
    }

    public TableQuery between(String fieldName, @Nullable Float value1, @Nullable Float value2) {
        long position1 = nativeArgumentList.insertFloat(value1);
        long position2 = nativeArgumentList.insertFloat(value2);
        queryBuilder.appendBetween(fieldName, position1, position2);

        return this;
    }

    // Queries for double values.

    public TableQuery equalTo(String fieldName, @Nullable Double value) {
        long position = nativeArgumentList.insertDouble(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable Double value) {
        long position = nativeArgumentList.insertDouble(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery greaterThan(String fieldName, @Nullable Double value) {
        long position = nativeArgumentList.insertDouble(value);
        queryBuilder.appendGreaterThan(fieldName, position);
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, @Nullable Double value) {
        long position = nativeArgumentList.insertDouble(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
        return this;
    }

    public TableQuery lessThan(String fieldName, @Nullable Double value) {
        long position = nativeArgumentList.insertDouble(value);
        queryBuilder.appendLessThan(fieldName, position);
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, @Nullable Double value) {
        long position = nativeArgumentList.insertDouble(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
        return this;
    }

    public TableQuery between(String fieldName, @Nullable Double value1, @Nullable Double value2) {
        long position1 = nativeArgumentList.insertDouble(value1);
        long position2 = nativeArgumentList.insertDouble(value2);

        queryBuilder.appendBetween(fieldName, position1, position2);
        return this;
    }

    // Query for boolean values.

    public TableQuery equalTo(String fieldName, @Nullable Boolean value) {
        long position = nativeArgumentList.insertBoolean(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable Boolean value) {
        long position = nativeArgumentList.insertBoolean(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    // Queries for Date values.

    public TableQuery equalTo(String fieldName, @Nullable Date value) {
        long position = nativeArgumentList.insertDate(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable Date value) {
        long position = nativeArgumentList.insertDate(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery greaterThan(String fieldName, Date value) {
        long position = nativeArgumentList.insertDate(value);
        queryBuilder.appendGreaterThan(fieldName, position);
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, Date value) {
        long position = nativeArgumentList.insertDate(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
        return this;
    }

    public TableQuery lessThan(String fieldName, Date value) {
        long position = nativeArgumentList.insertDate(value);
        queryBuilder.appendLessThan(fieldName, position);
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, Date value) {
        long position = nativeArgumentList.insertDate(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
        return this;
    }

    public TableQuery between(String fieldName, Date value1, Date value2) {
        long position1 = nativeArgumentList.insertDate(value1);
        long position2 = nativeArgumentList.insertDate(value2);
        queryBuilder.appendBetween(fieldName, position1, position2);
        return this;
    }

    // Queries for Binary values.

    public TableQuery equalTo(String fieldName, @Nullable byte[] value) {
        long position = nativeArgumentList.insertByteArray(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable byte[] value) {
        long position = nativeArgumentList.insertByteArray(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery equalTo(String fieldName, @Nullable String value, Case caseSensitive) {
        long position = nativeArgumentList.insertString(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendEqualTo(fieldName, position);
        } else {
            queryBuilder.appendEqualToNotSensitive(fieldName, position);
        }

        return this;
    }

    public TableQuery equalTo(String fieldName, String value) {
        return equalTo(fieldName, value, Case.SENSITIVE);
    }

    public TableQuery notEqualTo(String fieldName, String value) {
        return notEqualTo(fieldName, value, Case.SENSITIVE);
    }

    // Not Equals
    public TableQuery notEqualTo(String fieldName, @Nullable String value, Case caseSensitive) {
        long position = nativeArgumentList.insertString(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendNotEqualTo(fieldName, position);
        } else {
            queryBuilder.appendNotEqualToNotSensitive(fieldName, position);
        }

        return this;
    }

    public TableQuery beginsWith(String fieldName, String value) {
        return beginsWith(fieldName, value, Case.SENSITIVE);
    }

    public TableQuery like(String fieldName, String value) {
        return like(fieldName, value, Case.SENSITIVE);
    }

    public TableQuery contains(String fieldName, String value) {
        return contains(fieldName, value, Case.SENSITIVE);
    }

    public TableQuery beginsWith(String fieldName, String value, Case caseSensitive) {
        long position = nativeArgumentList.insertString(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendBeginsWith(fieldName, position);
        } else {
            queryBuilder.appendBeginsWithNotSensitive(fieldName, position);
        }

        return this;
    }

    public TableQuery endsWith(String fieldName, String value, Case caseSensitive) {
        long position = nativeArgumentList.insertString(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendEndsWith(fieldName, position);
        } else {
            queryBuilder.appendEndsWithNotSensitive(fieldName, position);
        }

        return this;
    }

    public TableQuery endsWith(String fieldName, String value) {
        return endsWith(fieldName, value, Case.SENSITIVE);
    }

    public TableQuery like(String fieldName, String value, Case caseSensitive) {
        long position = nativeArgumentList.insertString(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendLike(fieldName, position);
        } else {
            queryBuilder.appendLikeNotSensitive(fieldName, position);
        }

        return this;
    }

    public TableQuery contains(String fieldName, String value, Case caseSensitive) {
        long position = nativeArgumentList.insertString(value);

        if (caseSensitive == Case.SENSITIVE) {
            queryBuilder.appendContains(fieldName, position);
        } else {
            queryBuilder.appendContainsNotSensitive(fieldName, position);
        }

        return this;
    }

    public TableQuery isEmpty(String fieldName) {
        equalTo(fieldName + ".@count", 0);
        return this;
    }

    public TableQuery isNotEmpty(String fieldName) {
        notEqualTo(fieldName + ".@count", 0);
        return this;
    }

    // Queries for Decimal128

    public TableQuery between(String fieldName, @Nullable Decimal128 value1, @Nullable Decimal128 value2) {
        long position1 = nativeArgumentList.insertDecimal128(value1);
        long position2 = nativeArgumentList.insertDecimal128(value2);
        queryBuilder.appendBetween(fieldName, position1, position2);
        return this;
    }

    public TableQuery equalTo(String fieldName, @Nullable Decimal128 value) {
        long position = nativeArgumentList.insertDecimal128(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable Decimal128 value) {
        long position = nativeArgumentList.insertDecimal128(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery lessThan(String fieldName, @Nullable Decimal128 value) {
        long position = nativeArgumentList.insertDecimal128(value);
        queryBuilder.appendLessThan(fieldName, position);
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, @Nullable Decimal128 value) {
        long position = nativeArgumentList.insertDecimal128(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
        return this;
    }

    public TableQuery greaterThan(String fieldName, @Nullable Decimal128 value) {
        long position = nativeArgumentList.insertDecimal128(value);
        queryBuilder.appendGreaterThan(fieldName, position);
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, @Nullable Decimal128 value) {
        long position = nativeArgumentList.insertDecimal128(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
        return this;
    }

    // Queries for ObjectId

    public TableQuery equalTo(String fieldName, @Nullable ObjectId value) {
        long position = nativeArgumentList.insertObjectId(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable ObjectId value) {
        long position = nativeArgumentList.insertObjectId(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery lessThan(String fieldName, @Nullable ObjectId value) {
        long position = nativeArgumentList.insertObjectId(value);
        queryBuilder.appendLessThan(fieldName, position);
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, @Nullable ObjectId value) {
        long position = nativeArgumentList.insertObjectId(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
        return this;
    }

    public TableQuery greaterThan(String fieldName, @Nullable ObjectId value) {
        long position = nativeArgumentList.insertObjectId(value);
        queryBuilder.appendGreaterThan(fieldName, position);
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, @Nullable ObjectId value) {
        long position = nativeArgumentList.insertObjectId(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
        return this;
    }

    // Queries for UUID

    public TableQuery equalTo(String fieldName, @Nullable UUID value) {
        long position = nativeArgumentList.insertUUID(value);
        queryBuilder.appendEqualTo(fieldName, position);
        return this;
    }

    public TableQuery notEqualTo(String fieldName, @Nullable UUID value) {
        long position = nativeArgumentList.insertUUID(value);
        queryBuilder.appendNotEqualTo(fieldName, position);
        return this;
    }

    public TableQuery lessThan(String fieldName, @Nullable UUID value) {
        long position = nativeArgumentList.insertUUID(value);
        queryBuilder.appendLessThan(fieldName, position);
        return this;
    }

    public TableQuery lessThanOrEqual(String fieldName, @Nullable UUID value) {
        long position = nativeArgumentList.insertUUID(value);
        queryBuilder.appendLessThanEquals(fieldName, position);
        return this;
    }

    public TableQuery greaterThan(String fieldName, @Nullable UUID value) {
        long position = nativeArgumentList.insertUUID(value);
        queryBuilder.appendGreaterThan(fieldName, position);
        return this;
    }

    public TableQuery greaterThanOrEqual(String fieldName, @Nullable UUID value) {
        long position = nativeArgumentList.insertUUID(value);
        queryBuilder.appendGreaterThanEquals(fieldName, position);
        return this;
    }

    // isNull and isNotNull
    public TableQuery isNull(String fieldName) {
        queryBuilder.isNull(fieldName);
        return this;
    }

    public TableQuery isNotNull(String fieldName) {
        queryBuilder.isNotNull(fieldName);
        return this;
    }

    public TableQuery alwaysTrue() {
        queryBuilder.alwaysTrue();
        return this;
    }

    public TableQuery alwaysFalse() {
        queryBuilder.alwaysFalse();
        return this;
    }

    public TableQuery rawPredicate(String filter, Object[] args) {
        // Validates any pending query. It is done because the query argument indexes might clash with the raw predicate
        // ones.
        validateQuery();

        for (int i = 0; i < args.length; i++) {
            Object argument = args[i];
            if (argument == null) {
                nativeArgumentList.insertNull();
            } else if (argument instanceof Boolean) {
                nativeArgumentList.insertBoolean((Boolean) argument);
            } else if (argument instanceof Float) {
                nativeArgumentList.insertFloat((Float) argument);
            } else if (argument instanceof Double) {
                nativeArgumentList.insertDouble((Double) argument);
            } else if (argument instanceof Decimal128) {
                nativeArgumentList.insertDecimal128((Decimal128) argument);
            } else if (argument instanceof Number) {
                nativeArgumentList.insertLong((Number) argument);
            } else if (argument instanceof String) {
                nativeArgumentList.insertString((String) argument);
            } else if (argument instanceof byte[]) {
                nativeArgumentList.insertByteArray((byte[]) argument);
            } else if (argument instanceof Date) {
                nativeArgumentList.insertDate((Date) argument);
            } else if (argument instanceof ObjectId) {
                nativeArgumentList.insertObjectId((ObjectId) argument);
            } else if (argument instanceof UUID) {
                nativeArgumentList.insertUUID((UUID) argument);
            } else if (argument instanceof RealmModel) {
                RealmModel value = (RealmModel) argument;

                if (!RealmObject.isValid(value) || !RealmObject.isManaged(value)) {
                    throw new IllegalArgumentException("Argument[" + i + "] is not a valid managed object.");
                }

                RealmObjectProxy proxy = (RealmObjectProxy) value;
                UncheckedRow row = (UncheckedRow) proxy.realmGet$proxyState().getRow$realm();
                nativeArgumentList.insertObject(row.getNativePtr());
            } else {
                throw new IllegalArgumentException("Unsupported query argument type: " + argument.getClass().getSimpleName());
            }
        }

        queryBuilder.appendRawPredicate(filter);

        // Validate current pending to prevent argument clash with a possible upcoming query.
        validateQuery();

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

    private static native void nativeRawPredicate(long nativeQueryPtr, boolean isOrConnected, String filter, long argsPtr, long mappingPtr);

    private static native long nativeGetFinalizerPtr();
}
