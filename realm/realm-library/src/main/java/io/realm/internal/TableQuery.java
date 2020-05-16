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

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.Case;
import io.realm.Sort;
import io.realm.log.RealmLog;


public class TableQuery implements NativeObject {
    private static final boolean DEBUG = false;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    // See documentation in that NativeContext for an explanation of how this is used
    @SuppressWarnings("unused")
    private final NativeContext context;

    private final Table table;
    private final long nativePtr;

    // All actions (find(), findAll(), sum(), etc.) must call validateQuery() before performing
    // the actual action. The other methods must set queryValidated to false in order to enforce
    // the first action to validate the syntax of the query.
    private boolean queryValidated = true;

    // TODO: Can we protect this?
    public TableQuery(NativeContext context, Table table, long nativeQueryPtr) {
        if (DEBUG) {
            RealmLog.debug("New TableQuery: ptr=%x", nativeQueryPtr);
        }
        this.context = context;
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
    void validateQuery() {
        if (!queryValidated) { // If not yet validated, checks if syntax is valid
            String invalidMessage = nativeValidateQuery(nativePtr);
            if (invalidMessage.equals("")) {
                queryValidated = true; // If empty string error message, query is valid
            } else { throw new UnsupportedOperationException(invalidMessage); }
        }
    }

    // Grouping

    public TableQuery group() {
        nativeGroup(nativePtr);
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

    // Queries for integer values.

    public TableQuery equalTo(long[] columnKeys, long[] tablePtrs, long value) {
        nativeEqual(nativePtr, columnKeys, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnKey, long[] tablePtrs, long value) {
        nativeNotEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnKey, long[] tablePtrs, long value) {
        nativeGreater(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnKey, long[] tablePtrs, long value) {
        nativeGreaterEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnKey, long[] tablePtrs, long value) {
        nativeLess(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnKey, long[] tablePtrs, long value) {
        nativeLessEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnKey, long value1, long value2) {
        nativeBetween(nativePtr, columnKey, value1, value2);
        queryValidated = false;
        return this;
    }

    // Queries for float values.

    public TableQuery equalTo(long[] columnKey, long[] tablePtrs, float value) {
        nativeEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnKey, long[] tablePtrs, float value) {
        nativeNotEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnKey, long[] tablePtrs, float value) {
        nativeGreater(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnKey, long[] tablePtrs, float value) {
        nativeGreaterEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnKey, long[] tablePtrs, float value) {
        nativeLess(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnKey, long[] tablePtrs, float value) {
        nativeLessEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnKey, float value1, float value2) {
        nativeBetween(nativePtr, columnKey, value1, value2);
        queryValidated = false;
        return this;
    }

    // Queries for double values.

    public TableQuery equalTo(long[] columnKey, long[] tablePtrs, double value) {
        nativeEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnKey, long[] tablePtrs, double value) {
        nativeNotEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnKey, long[] tablePtrs, double value) {
        nativeGreater(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnKey, long[] tablePtrs, double value) {
        nativeGreaterEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnKey, long[] tablePtrs, double value) {
        nativeLess(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnKey, long[] tablePtrs, double value) {
        nativeLessEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnKey, double value1, double value2) {
        nativeBetween(nativePtr, columnKey, value1, value2);
        queryValidated = false;
        return this;
    }

    // Query for boolean values.

    public TableQuery equalTo(long[] columnKey, long[] tablePtrs, boolean value) {
        nativeEqual(nativePtr, columnKey, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    // Queries for Date values.

    private static final String DATE_NULL_ERROR_MESSAGE = "Date value in query criteria must not be null.";

    public TableQuery equalTo(long[] columnKey, long[] tablePtrs, @Nullable Date value) {
        if (value == null) {
            nativeIsNull(nativePtr, columnKey, tablePtrs);
        } else {
            nativeEqualTimestamp(nativePtr, columnKey, tablePtrs, value.getTime());
        }
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnKey, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeNotEqualTimestamp(nativePtr, columnKey, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnKey, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeGreaterTimestamp(nativePtr, columnKey, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnKey, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeGreaterEqualTimestamp(nativePtr, columnKey, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnKey, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeLessTimestamp(nativePtr, columnKey, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnKey, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeLessEqualTimestamp(nativePtr, columnKey, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnKey, Date value1, Date value2) {
        //noinspection ConstantConditions
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("Date values in query criteria must not be null."); // Different text
        }
        nativeBetweenTimestamp(nativePtr, columnKey, value1.getTime(), value2.getTime());
        queryValidated = false;
        return this;
    }

    // Queries for Binary values.

    public TableQuery equalTo(long[] columnKeys, long[] tablePtrs, byte[] value) {
        nativeEqual(nativePtr, columnKeys, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnKeys, long[] tablePtrs, byte[] value) {
        nativeNotEqual(nativePtr, columnKeys, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    // Equals
    public TableQuery equalTo(long[] columnKeys, long[] tablePtrs, @Nullable String value, Case caseSensitive) {
        nativeEqual(nativePtr, columnKeys, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery equalTo(long[] columnKeys, long[] tablePtrs, String value) {
        nativeEqual(nativePtr, columnKeys, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    // Not Equals
    public TableQuery notEqualTo(long[] columnIndex, long[] tablePtrs, @Nullable String value, Case caseSensitive) {
        nativeNotEqual(nativePtr, columnIndex, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnIndex, long[] tablePtrs, @Nullable String value) {
        nativeNotEqual(nativePtr, columnIndex, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWith(long[] columnKeys, long[] tablePtrs, String value, Case caseSensitive) {
        nativeBeginsWith(nativePtr, columnKeys, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWith(long[] columnKeys, long[] tablePtrs, String value) {
        nativeBeginsWith(nativePtr, columnKeys, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(long[] columnKeys, long[] tablePtrs, String value, Case caseSensitive) {
        nativeEndsWith(nativePtr, columnKeys, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(long[] columnKeys, long[] tablePtrs, String value) {
        nativeEndsWith(nativePtr, columnKeys, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery like(long[] columnKeys, long[] tablePtrs, String value, Case caseSensitive) {
        nativeLike(nativePtr, columnKeys, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery like(long[] columnKeys, long[] tablePtrs, String value) {
        nativeLike(nativePtr, columnKeys, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery contains(long[] columnKeys, long[] tablePtrs, String value, Case caseSensitive) {
        nativeContains(nativePtr, columnKeys, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery contains(long[] columnKeys, long[] tablePtrs, String value) {
        nativeContains(nativePtr, columnKeys, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery isEmpty(long[] columnKeys, long[] tablePtrs) {
        nativeIsEmpty(nativePtr, columnKeys, tablePtrs);
        queryValidated = false;
        return this;
    }

    public TableQuery isNotEmpty(long[] columnKeys, long[] tablePtrs) {
        nativeIsNotEmpty(nativePtr, columnKeys, tablePtrs);
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

    // isNull and isNotNull
    public TableQuery isNull(long[] columnKeys, long[] tablePtrs) {
        nativeIsNull(nativePtr, columnKeys, tablePtrs);
        queryValidated = false;
        return this;
    }

    public TableQuery isNotNull(long[] columnKeys, long[] tablePtrs) {
        nativeIsNotNull(nativePtr, columnKeys, tablePtrs);
        queryValidated = false;
        return this;
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

    public void alwaysTrue() {
        nativeAlwaysTrue(nativePtr);
    }

    public void alwaysFalse() {
        nativeAlwaysFalse(nativePtr);
    }

    private native String nativeValidateQuery(long nativeQueryPtr);

    private native void nativeGroup(long nativeQueryPtr);

    private native void nativeEndGroup(long nativeQueryPtr);

    private native void nativeOr(long nativeQueryPtr);

    private native void nativeNot(long nativeQueryPtr);

    private native void nativeEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeNotEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeGreater(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeGreaterEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeLess(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeLessEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeBetween(long nativeQueryPtr, long[] columnIndex, long value1, long value2);

    private native void nativeEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, float value);

    private native void nativeNotEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, float value);

    private native void nativeGreater(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, float value);

    private native void nativeGreaterEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, float value);

    private native void nativeLess(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, float value);

    private native void nativeLessEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, float value);

    private native void nativeBetween(long nativeQueryPtr, long[] columnIndex, float value1, float value2);

    private native void nativeEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, double value);

    private native void nativeNotEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, double value);

    private native void nativeGreater(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, double value);

    private native void nativeGreaterEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, double value);

    private native void nativeLess(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, double value);

    private native void nativeLessEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, double value);

    private native void nativeBetween(long nativeQueryPtr, long[] columnIndex, double value1, double value2);

    private native void nativeEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, boolean value);

    private native void nativeEqualTimestamp(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeNotEqualTimestamp(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeGreaterTimestamp(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeGreaterEqualTimestamp(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeLessTimestamp(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeLessEqualTimestamp(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, long value);

    private native void nativeBetweenTimestamp(long nativeQueryPtr, long[] columnIndex, long value1, long value2);

    private native void nativeEqual(long nativeQueryPtr, long[] columnKeys, long[] tablePtrs, byte[] value);

    private native void nativeNotEqual(long nativeQueryPtr, long[] columnKeys, long[] tablePtrs, byte[] value);

    private native void nativeEqual(long nativeQueryPtr, long[] columnKeys, long[] tablePtrs, @Nullable String value, boolean caseSensitive);

    private native void nativeNotEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, @Nullable String value, boolean caseSensitive);

    private native void nativeBeginsWith(long nativeQueryPtr, long[] columnKeys, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeEndsWith(long nativeQueryPtr, long[] columnKeys, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeLike(long nativeQueryPtr, long[] columnKeys, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeContains(long nativeQueryPtr, long[] columnKeys, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeIsEmpty(long nativePtr, long[] columnKeys, long[] tablePtrs);

    private native void nativeIsNotEmpty(long nativePtr, long[] columnKeys, long[] tablePtrs);

    private native void nativeAlwaysTrue(long nativeQueryPtr);

    private native void nativeAlwaysFalse(long nativeQueryPtr);

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

    private native Double nativeMaximumDouble(long nativeQueryPtr, long columnKey);

    private native Double nativeMinimumDouble(long nativeQueryPtr, long columnKey);

    private native double nativeAverageDouble(long nativeQueryPtr, long columnKey);

    private native Long nativeMaximumTimestamp(long nativeQueryPtr, long columnKey);

    private native Long nativeMinimumTimestamp(long nativeQueryPtr, long columnKey);

    private native void nativeIsNull(long nativePtr, long[] columnKeys, long[] tablePtrs);

    private native void nativeIsNotNull(long nativePtr, long[] columnIndice, long[] tablePtr);

    private native long nativeCount(long nativeQueryPtr);

    private native long nativeRemove(long nativeQueryPtr);

    private static native long nativeGetFinalizerPtr();
}
