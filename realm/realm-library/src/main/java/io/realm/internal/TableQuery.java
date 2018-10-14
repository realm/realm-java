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

    public TableQuery equalTo(long[] columnIndexes, long[] tablePtrs, long value) {
        nativeEqual(nativePtr, columnIndexes, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnIndex, long[] tablePtrs, long value) {
        nativeNotEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnIndex, long[] tablePtrs, long value) {
        nativeGreater(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnIndex, long[] tablePtrs, long value) {
        nativeGreaterEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnIndex, long[] tablePtrs, long value) {
        nativeLess(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnIndex, long[] tablePtrs, long value) {
        nativeLessEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnIndex, long value1, long value2) {
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }

    // Queries for float values.

    public TableQuery equalTo(long[] columnIndex, long[] tablePtrs, float value) {
        nativeEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnIndex, long[] tablePtrs, float value) {
        nativeNotEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnIndex, long[] tablePtrs, float value) {
        nativeGreater(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnIndex, long[] tablePtrs, float value) {
        nativeGreaterEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnIndex, long[] tablePtrs, float value) {
        nativeLess(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnIndex, long[] tablePtrs, float value) {
        nativeLessEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnIndex, float value1, float value2) {
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }

    // Queries for double values.

    public TableQuery equalTo(long[] columnIndex, long[] tablePtrs, double value) {
        nativeEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnIndex, long[] tablePtrs, double value) {
        nativeNotEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnIndex, long[] tablePtrs, double value) {
        nativeGreater(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnIndex, long[] tablePtrs, double value) {
        nativeGreaterEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnIndex, long[] tablePtrs, double value) {
        nativeLess(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnIndex, long[] tablePtrs, double value) {
        nativeLessEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnIndex, double value1, double value2) {
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }

    // Query for boolean values.

    public TableQuery equalTo(long[] columnIndex, long[] tablePtrs, boolean value) {
        nativeEqual(nativePtr, columnIndex, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    // Queries for Date values.

    private static final String DATE_NULL_ERROR_MESSAGE = "Date value in query criteria must not be null.";

    public TableQuery equalTo(long[] columnIndex, long[] tablePtrs, @Nullable Date value) {
        if (value == null) {
            nativeIsNull(nativePtr, columnIndex, tablePtrs);
        } else {
            nativeEqualTimestamp(nativePtr, columnIndex, tablePtrs, value.getTime());
        }
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnIndex, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeNotEqualTimestamp(nativePtr, columnIndex, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long[] columnIndex, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeGreaterTimestamp(nativePtr, columnIndex, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long[] columnIndex, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeGreaterEqualTimestamp(nativePtr, columnIndex, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long[] columnIndex, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeLessTimestamp(nativePtr, columnIndex, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long[] columnIndex, long[] tablePtrs, Date value) {
        //noinspection ConstantConditions
        if (value == null) { throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE); }
        nativeLessEqualTimestamp(nativePtr, columnIndex, tablePtrs, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery between(long[] columnIndex, Date value1, Date value2) {
        //noinspection ConstantConditions
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("Date values in query criteria must not be null."); // Different text
        }
        nativeBetweenTimestamp(nativePtr, columnIndex, value1.getTime(), value2.getTime());
        queryValidated = false;
        return this;
    }

    // Queries for Binary values.

    public TableQuery equalTo(long[] columnIndices, long[] tablePtrs, byte[] value) {
        nativeEqual(nativePtr, columnIndices, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnIndices, long[] tablePtrs, byte[] value) {
        nativeNotEqual(nativePtr, columnIndices, tablePtrs, value);
        queryValidated = false;
        return this;
    }

    // Equals
    public TableQuery equalTo(long[] columnIndexes, long[] tablePtrs, @Nullable String value, Case caseSensitive) {
        nativeEqual(nativePtr, columnIndexes, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery equalTo(long[] columnIndexes, long[] tablePtrs, String value) {
        nativeEqual(nativePtr, columnIndexes, tablePtrs, value, true);
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

    public TableQuery beginsWith(long[] columnIndices, long[] tablePtrs, String value, Case caseSensitive) {
        nativeBeginsWith(nativePtr, columnIndices, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWith(long[] columnIndices, long[] tablePtrs, String value) {
        nativeBeginsWith(nativePtr, columnIndices, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(long[] columnIndices, long[] tablePtrs, String value, Case caseSensitive) {
        nativeEndsWith(nativePtr, columnIndices, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(long[] columnIndices, long[] tablePtrs, String value) {
        nativeEndsWith(nativePtr, columnIndices, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery like(long[] columnIndices, long[] tablePtrs, String value, Case caseSensitive) {
        nativeLike(nativePtr, columnIndices, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery like(long[] columnIndices, long[] tablePtrs, String value) {
        nativeLike(nativePtr, columnIndices, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery contains(long[] columnIndices, long[] tablePtrs, String value, Case caseSensitive) {
        nativeContains(nativePtr, columnIndices, tablePtrs, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery contains(long[] columnIndices, long[] tablePtrs, String value) {
        nativeContains(nativePtr, columnIndices, tablePtrs, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery isEmpty(long[] columnIndices, long[] tablePtrs) {
        nativeIsEmpty(nativePtr, columnIndices, tablePtrs);
        queryValidated = false;
        return this;
    }

    public TableQuery isNotEmpty(long[] columnIndices, long[] tablePtrs) {
        nativeIsNotEmpty(nativePtr, columnIndices, tablePtrs);
        queryValidated = false;
        return this;
    }

    // Searching methods.

    @Deprecated // Doesn't seem to be used
    public long find(long fromTableRow) {
        validateQuery();
        return nativeFind(nativePtr, fromTableRow);
    }

    /**
     * Returns the table row index for the first element matching the query.
     */
    public long find() {
        validateQuery();
        return nativeFind(nativePtr, 0);
    }

    //
    // Aggregation methods
    //

    // Integer aggregation

    public long sumInt(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeSumInt(nativePtr, columnIndex, start, end, limit);
    }

    public long sumInt(long columnIndex) {
        validateQuery();
        return nativeSumInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public Long maximumInt(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeMaximumInt(nativePtr, columnIndex, start, end, limit);
    }

    public Long maximumInt(long columnIndex) {
        validateQuery();
        return nativeMaximumInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public Long minimumInt(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeMinimumInt(nativePtr, columnIndex, start, end, limit);
    }

    public Long minimumInt(long columnIndex) {
        validateQuery();
        return nativeMinimumInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public double averageInt(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeAverageInt(nativePtr, columnIndex, start, end, limit);
    }

    public double averageInt(long columnIndex) {
        validateQuery();
        return nativeAverageInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    // Float aggregation

    public double sumFloat(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeSumFloat(nativePtr, columnIndex, start, end, limit);
    }

    public double sumFloat(long columnIndex) {
        validateQuery();
        return nativeSumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public Float maximumFloat(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeMaximumFloat(nativePtr, columnIndex, start, end, limit);
    }

    public Float maximumFloat(long columnIndex) {
        validateQuery();
        return nativeMaximumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public Float minimumFloat(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeMinimumFloat(nativePtr, columnIndex, start, end, limit);
    }

    public Float minimumFloat(long columnIndex) {
        validateQuery();
        return nativeMinimumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public double averageFloat(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeAverageFloat(nativePtr, columnIndex, start, end, limit);
    }

    public double averageFloat(long columnIndex) {
        validateQuery();
        return nativeAverageFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    // Double aggregation

    public double sumDouble(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeSumDouble(nativePtr, columnIndex, start, end, limit);
    }

    public double sumDouble(long columnIndex) {
        validateQuery();
        return nativeSumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public Double maximumDouble(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeMaximumDouble(nativePtr, columnIndex, start, end, limit);
    }

    public Double maximumDouble(long columnIndex) {
        validateQuery();
        return nativeMaximumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public Double minimumDouble(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeMinimumDouble(nativePtr, columnIndex, start, end, limit);
    }

    public Double minimumDouble(long columnIndex) {
        validateQuery();
        return nativeMinimumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    public double averageDouble(long columnIndex, long start, long end, long limit) {
        validateQuery();
        return nativeAverageDouble(nativePtr, columnIndex, start, end, limit);
    }

    public double averageDouble(long columnIndex) {
        validateQuery();
        return nativeAverageDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }

    // Date aggregation

    public Date maximumDate(long columnIndex, long start, long end, long limit) {
        validateQuery();
        Long result = nativeMaximumTimestamp(nativePtr, columnIndex, start, end, limit);
        if (result != null) {
            return new Date(result);
        }
        return null;
    }

    public Date maximumDate(long columnIndex) {
        validateQuery();
        Long result = nativeMaximumTimestamp(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
        if (result != null) {
            return new Date(result);
        }
        return null;
    }

    public Date minimumDate(long columnIndex, long start, long end, long limit) {
        validateQuery();
        Long result = nativeMinimumTimestamp(nativePtr, columnIndex, start, end, limit);
        if (result != null) {
            return new Date(result * 1000);
        }
        return null;
    }

    public Date minimumDate(long columnIndex) {
        validateQuery();
        Long result = nativeMinimumTimestamp(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
        if (result != null) {
            return new Date(result);
        }
        return null;
    }

    // isNull and isNotNull
    public TableQuery isNull(long[] columnIndices, long[] tablePtrs) {
        nativeIsNull(nativePtr, columnIndices, tablePtrs);
        queryValidated = false;
        return this;
    }

    public TableQuery isNotNull(long[] columnIndices, long[] tablePtrs) {
        nativeIsNotNull(nativePtr, columnIndices, tablePtrs);
        queryValidated = false;
        return this;
    }

    // Count

    // TODO: Rename all start, end parameter names to firstRow, lastRow
    public long count(long start, long end, long limit) {
        validateQuery();
        return nativeCount(nativePtr, start, end, limit);
    }

    /**
     * Returns only the number of matching objects.
     * This method is very fast compared to evaluating a query completely, but it does not
     * goes around any logic implemented in Object Store and other parts of the API that works
     * on query results. So the primary use case for this method is testing.
     */
    @Deprecated
    public long count() {
        validateQuery();
        return nativeCount(nativePtr, 0, Table.INFINITE, Table.INFINITE);
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

    private native void nativeEqual(long nativeQueryPtr, long[] columnIndices, long[] tablePtrs, byte[] value);

    private native void nativeNotEqual(long nativeQueryPtr, long[] columnIndices, long[] tablePtrs, byte[] value);

    private native void nativeEqual(long nativeQueryPtr, long[] columnIndexes, long[] tablePtrs, @Nullable String value, boolean caseSensitive);

    private native void nativeNotEqual(long nativeQueryPtr, long[] columnIndex, long[] tablePtrs, @Nullable String value, boolean caseSensitive);

    private native void nativeBeginsWith(long nativeQueryPtr, long[] columnIndices, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeEndsWith(long nativeQueryPtr, long[] columnIndices, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeLike(long nativeQueryPtr, long[] columnIndices, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeContains(long nativeQueryPtr, long[] columnIndices, long[] tablePtrs, String value, boolean caseSensitive);

    private native void nativeIsEmpty(long nativePtr, long[] columnIndices, long[] tablePtrs);

    private native void nativeIsNotEmpty(long nativePtr, long[] columnIndices, long[] tablePtrs);

    private native void nativeAlwaysTrue(long nativeQueryPtr);

    private native void nativeAlwaysFalse(long nativeQueryPtr);

    private native long nativeFind(long nativeQueryPtr, long fromTableRow);

    private native long nativeFindAll(long nativeQueryPtr, long start, long end, long limit);

    private native long nativeSumInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Long nativeMaximumInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Long nativeMinimumInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native double nativeAverageInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native double nativeSumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Float nativeMaximumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Float nativeMinimumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native double nativeAverageFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native double nativeSumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Double nativeMaximumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Double nativeMinimumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native double nativeAverageDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Long nativeMaximumTimestamp(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native Long nativeMinimumTimestamp(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    private native void nativeIsNull(long nativePtr, long[] columnIndices, long[] tablePtrs);

    private native void nativeIsNotNull(long nativePtr, long[] columnIndice, long[] tablePtr);

    private native long nativeCount(long nativeQueryPtr, long start, long end, long limit);

    private native long nativeRemove(long nativeQueryPtr);

    private static native long nativeGetFinalizerPtr();
}
