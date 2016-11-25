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

import io.realm.Case;
import io.realm.Sort;
import io.realm.internal.async.BadVersionException;

public class TableQuery implements NativeObject {
    protected boolean DEBUG = false;

    protected long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    protected final Table table;
    // Don't convert this into local variable and don't remove this.
    // Core requests Query to hold the TableView reference which it is built from.
    @SuppressWarnings({"unused"})
    private final TableOrView origin; // Table or TableView which created this TableQuery
    private final Context context;

    // All actions (find(), findAll(), sum(), etc.) must call validateQuery() before performing
    // the actual action. The other methods must set queryValidated to false in order to enforce
    // the first action to validate the syntax of the query.
    private boolean queryValidated = true;

    // TODO: Can we protect this?
    public TableQuery(Context context, Table table, long nativeQueryPtr) {
        if (DEBUG) {
            System.err.println("++++++ new TableQuery, ptr= " + nativeQueryPtr);
        }
        this.context = context;
        this.table = table;
        this.nativePtr = nativeQueryPtr;
        this.origin = null;
        context.addReference(this);
    }

    public TableQuery(Context context, Table table, long nativeQueryPtr, TableOrView origin) {
        if (DEBUG) {
            System.err.println("++++++ new TableQuery, ptr= " + nativeQueryPtr);
        }
        this.context = context;
        this.table = table;
        this.nativePtr = nativeQueryPtr;
        this.origin = origin;
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

    /**
     * Checks in core if query syntax is valid. Throws exception, if not.
     */
    private void validateQuery() {
        if (! queryValidated) { // If not yet validated, check if syntax is valid
            String invalidMessage = nativeValidateQuery(nativePtr);
            if (invalidMessage.equals(""))
                queryValidated = true; // If empty string error message, query is valid
            else
                throw new UnsupportedOperationException(invalidMessage);
        }
    }

    // Query TableView
    public TableQuery tableview(TableView tv) {
        nativeTableview(nativePtr, tv.nativePtr);
        return this;
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

    // Query for integer values.

    public TableQuery equalTo(long columnIndexes[], long value) {
        nativeEqual(nativePtr, columnIndexes, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long columnIndex[], long value) {
        nativeNotEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long columnIndex[], long value) {
        nativeGreater(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long columnIndex[], long value) {
        nativeGreaterEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long columnIndex[], long value) {
        nativeLess(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long columnIndex[], long value) {
        nativeLessEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long columnIndex[], long value1, long value2) {
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }

    // Query for float values.

    public TableQuery equalTo(long columnIndex[], float value) {
        nativeEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long columnIndex[], float value) {
        nativeNotEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long columnIndex[], float value) {
        nativeGreater(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long columnIndex[], float value) {
        nativeGreaterEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long columnIndex[], float value) {
        nativeLess(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long columnIndex[], float value) {
        nativeLessEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long columnIndex[], float value1, float value2) {
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }

    // Query for double values.

    public TableQuery equalTo(long columnIndex[], double value) {
        nativeEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long columnIndex[], double value) {
        nativeNotEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long columnIndex[], double value) {
        nativeGreater(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long columnIndex[], double value) {
        nativeGreaterEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long columnIndex[], double value) {
        nativeLess(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long columnIndex[], double value) {
        nativeLessEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    public TableQuery between(long columnIndex[], double value1, double value2) {
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }

    // Query for boolean values.

    public TableQuery equalTo(long columnIndex[], boolean value) {
        nativeEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }

    // Query for Date values

    private final static String DATE_NULL_ERROR_MESSAGE = "Date value in query criteria must not be null.";

    public TableQuery equalTo(long columnIndex[], Date value){
        if (value == null) {
            nativeIsNull(nativePtr, columnIndex);
        } else {
            nativeEqualTimestamp(nativePtr, columnIndex, value.getTime());
        }
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long columnIndex[], Date value){
        if (value == null)
            throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE);
        nativeNotEqualTimestamp(nativePtr, columnIndex, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThan(long columnIndex[], Date value){
        if (value == null)
            throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE);
        nativeGreaterTimestamp(nativePtr, columnIndex, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery greaterThanOrEqual(long columnIndex[], Date value){
        if (value == null)
            throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE);
        nativeGreaterEqualTimestamp(nativePtr, columnIndex, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery lessThan(long columnIndex[], Date value){
        if (value == null)
            throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE);
        nativeLessTimestamp(nativePtr, columnIndex, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery lessThanOrEqual(long columnIndex[], Date value){
        if (value == null)
            throw new IllegalArgumentException(DATE_NULL_ERROR_MESSAGE);
        nativeLessEqualTimestamp(nativePtr, columnIndex, value.getTime());
        queryValidated = false;
        return this;
    }

    public TableQuery between(long columnIndex[], Date value1, Date value2){
        if (value1 == null || value2 == null)
            throw new IllegalArgumentException("Date values in query criteria must not be null."); // Different text
        nativeBetweenTimestamp(nativePtr, columnIndex, value1.getTime(), value2.getTime());
        queryValidated = false;
        return this;
    }

    // Query for Binary values.

    public TableQuery equalTo(long[] columnIndices, byte[] value) {
        nativeEqual(nativePtr, columnIndices, value);
        queryValidated = false;
        return this;
    }

    public TableQuery notEqualTo(long[] columnIndices, byte[] value) {
        nativeNotEqual(nativePtr, columnIndices, value);
        queryValidated = false;
        return this;
    }

    // Query for String values.

    private final static String STRING_NULL_ERROR_MESSAGE = "String value in query criteria must not be null.";

    // Equal
    public TableQuery equalTo(long[] columnIndexes, String value, Case caseSensitive) {
        nativeEqual(nativePtr, columnIndexes, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery equalTo(long[] columnIndexes, String value) {
        nativeEqual(nativePtr, columnIndexes, value, true);
        queryValidated = false;
        return this;
    }

    // Not Equal
    public TableQuery notEqualTo(long columnIndex[], String value, Case caseSensitive) {
        nativeNotEqual(nativePtr, columnIndex, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }
    public TableQuery notEqualTo(long columnIndex[], String value) {
        nativeNotEqual(nativePtr, columnIndex, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWith(long columnIndices[], String value, Case caseSensitive) {
        nativeBeginsWith(nativePtr, columnIndices, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery beginsWith(long columnIndices[], String value) {
        nativeBeginsWith(nativePtr, columnIndices, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(long columnIndices[], String value, Case caseSensitive) {
        nativeEndsWith(nativePtr, columnIndices, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery endsWith(long columnIndices[], String value) {
        nativeEndsWith(nativePtr, columnIndices, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery contains(long columnIndices[], String value, Case caseSensitive) {
        nativeContains(nativePtr, columnIndices, value, caseSensitive.getValue());
        queryValidated = false;
        return this;
    }

    public TableQuery contains(long columnIndices[], String value) {
        nativeContains(nativePtr, columnIndices, value, true);
        queryValidated = false;
        return this;
    }

    public TableQuery isEmpty(long[] columnIndices) {
        nativeIsEmpty(nativePtr, columnIndices);
        queryValidated = false;
        return this;
    }

    public TableQuery isNotEmpty(long[] columnIndices) {
        return not().isEmpty(columnIndices);
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

    /**
     * Performs a find query then handover the resulted Row (ready to be imported by another thread/shared_group).
     *
     * @param sharedRealm current {@link SharedRealm }from which to operate the query.
     * @param ptrQuery query to run the the find against.
     * @return pointer to the handover result (table_view).
     */
    public static long findWithHandover(SharedRealm sharedRealm, long ptrQuery) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        return nativeFindWithHandover(sharedRealm.getNativePtr(), ptrQuery, 0);
    }

    public TableView findAll(long start, long end, long limit) {
        validateQuery();

        long nativeViewPtr = nativeFindAll(nativePtr, start, end, limit);
        return new TableView(this.context, this.table, nativeViewPtr, this);
    }

    public TableView findAll() {
        validateQuery();

        long nativeViewPtr = nativeFindAll(nativePtr, 0, Table.INFINITE, Table.INFINITE);
        return new TableView(this.context, this.table, nativeViewPtr, this);
    }

    // handover find* methods
    // this will use a background SharedGroup to import the query (using the handover object)
    // run the query, and return the table view to the caller SharedGroup using the handover object.
    public static long findAllWithHandover(SharedRealm sharedRealm, long ptrQuery) throws BadVersionException {
        return nativeFindAllWithHandover(sharedRealm.getNativePtr(), ptrQuery, 0, Table.INFINITE, Table.INFINITE);
    }

    public static long findDistinctWithHandover(SharedRealm sharedRealm, long ptrQuery, long columnIndex) throws BadVersionException {
        return nativeGetDistinctViewWithHandover(sharedRealm.getNativePtr(), ptrQuery, columnIndex);
    }

    public static long findAllSortedWithHandover(SharedRealm sharedRealm, long ptrQuery, long columnIndex, Sort sortOrder) throws BadVersionException {
        return nativeFindAllSortedWithHandover(sharedRealm.getNativePtr(), ptrQuery, 0, Table.INFINITE, Table.INFINITE, columnIndex, sortOrder.getValue());
    }

    public static long findAllMultiSortedWithHandover(SharedRealm sharedRealm, long ptrQuery, long[] columnIndices, Sort[] sortOrders) throws BadVersionException {
        boolean[] ascendings = getNativeSortOrderValues(sortOrders);
        return nativeFindAllMultiSortedWithHandover(sharedRealm.getNativePtr(), ptrQuery, 0, Table.INFINITE, Table.INFINITE, columnIndices, ascendings);
    }

    public static long[] batchUpdateQueries(SharedRealm sharedRealm, long[] handoverQueries, long[][] parameters,
                                            long[][] queriesParameters, boolean[][] multiSortOrder)
            throws BadVersionException {
        return nativeBatchUpdateQueries(sharedRealm.getNativePtr(), handoverQueries, parameters, queriesParameters,
                multiSortOrder);
    }
    /**
     * Imports a TableView from a worker thread to the caller thread.
     *
     * @param handoverPtr pointer to the handover object
     * @param sharedRealm the SharedRealm on the caller thread.
     * @return the TableView on the caller thread.
     * @throws BadVersionException if the worker thread and caller thread are not at the same version.
     */
    public TableView importHandoverTableView(long handoverPtr, SharedRealm sharedRealm) throws BadVersionException {
        long nativeTvPtr = nativeImportHandoverTableViewIntoSharedGroup(handoverPtr, sharedRealm.getNativePtr());
        return new TableView(this.context, this.table, nativeTvPtr);
    }

    /**
     * Imports a row from a worker thread to the caller thread.
     *
     * @param handoverRowPtr pointer to the handover row object
     * @param sharedRealm the SharedRealm on the caller thread.
     * @return the row pointer on the caller thread.
     */
    public static long importHandoverRow(long handoverRowPtr, SharedRealm sharedRealm) {
        return nativeImportHandoverRowIntoSharedGroup(handoverRowPtr, sharedRealm.getNativePtr());
    }

    /**
     * Handovers the query, so it can be used by other SharedGroup (in different thread)
     *
     * @param sharedRealm the SharedGroup holding the query
     * @return native pointer to the handover query
     */
    public long handoverQuery(SharedRealm sharedRealm) {
        return nativeHandoverQuery(sharedRealm.getNativePtr(), nativePtr);
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

    // float aggregation

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

    // double aggregation

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

    // date aggregation

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
    public TableQuery isNull(long columnIndices[]) {
        nativeIsNull(nativePtr, columnIndices);
        queryValidated = false;
        return this;
    }

    public TableQuery isNotNull(long columnIndices[]) {
        nativeIsNotNull(nativePtr, columnIndices);
        queryValidated = false;
        return this;
    }

    // count

    // TODO: Rename all start, end parameter names to firstRow, lastRow
    public long count(long start, long end, long limit) {
        validateQuery();
        return nativeCount(nativePtr, start, end, limit);
    }

    public long count() {
        validateQuery();
        return nativeCount(nativePtr, 0, Table.INFINITE, Table.INFINITE);
    }

    public long remove() {
        validateQuery();
        if (table.isImmutable()) throwImmutable();
        return nativeRemove(nativePtr);
    }

    /**
     * Converts a list of sort orders to their native values.
     */
    public static boolean[] getNativeSortOrderValues(Sort[] sortOrders) {
        boolean[] nativeValues = new boolean[sortOrders.length];
        for (int i = 0; i < sortOrders.length; i++) {
            nativeValues[i] = sortOrders[i].getValue();
        }
        return nativeValues;
    }

    private void throwImmutable() {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }

    private native String nativeValidateQuery(long nativeQueryPtr);
    private native void nativeTableview(long nativeQueryPtr, long nativeTableViewPtr);
    private native void nativeGroup(long nativeQueryPtr);
    private native void nativeEndGroup(long nativeQueryPtr);
    private native void nativeOr(long nativeQueryPtr);
    private native void nativeNot(long nativeQueryPtr);
    private native void nativeEqual(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeNotEqual(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeGreater(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeLess(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeLessEqual(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeBetween(long nativeQueryPtr, long columnIndex[], long value1, long value2);
    private native void nativeEqual(long nativeQueryPtr, long columnIndex[], float value);
    private native void nativeNotEqual(long nativeQueryPtr, long columnIndex[], float value);
    private native void nativeGreater(long nativeQueryPtr, long columnIndex[], float value);
    private native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex[], float value);
    private native void nativeLess(long nativeQueryPtr, long columnIndex[], float value);
    private native void nativeLessEqual(long nativeQueryPtr, long columnIndex[], float value);
    private native void nativeBetween(long nativeQueryPtr, long columnIndex[], float value1, float value2);
    private native void nativeEqual(long nativeQueryPtr, long columnIndex[], double value);
    private native void nativeNotEqual(long nativeQueryPtr, long columnIndex[], double value);
    private native void nativeGreater(long nativeQueryPtr, long columnIndex[], double value);
    private native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex[], double value);
    private native void nativeLess(long nativeQueryPtr, long columnIndex[], double value);
    private native void nativeLessEqual(long nativeQueryPtr, long columnIndex[], double value);
    private native void nativeBetween(long nativeQueryPtr, long columnIndex[], double value1, double value2);
    private native void nativeEqual(long nativeQueryPtr, long columnIndex[], boolean value);
    private native void nativeEqualTimestamp(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeNotEqualTimestamp(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeGreaterTimestamp(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeGreaterEqualTimestamp(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeLessTimestamp(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeLessEqualTimestamp(long nativeQueryPtr, long columnIndex[], long value);
    private native void nativeBetweenTimestamp(long nativeQueryPtr, long columnIndex[], long value1, long value2);
    private native void nativeEqual(long nativeQueryPtr, long[] columnIndices, byte[] value);
    private native void nativeNotEqual(long nativeQueryPtr, long[] columnIndices, byte[] value);
    private native void nativeEqual(long nativeQueryPtr, long[] columnIndexes, String value, boolean caseSensitive);
    private native void nativeNotEqual(long nativeQueryPtr, long columnIndex[], String value, boolean caseSensitive);
    private native void nativeBeginsWith(long nativeQueryPtr, long columnIndices[], String value, boolean caseSensitive);
    private native void nativeEndsWith(long nativeQueryPtr, long columnIndices[], String value, boolean caseSensitive);
    private native void nativeContains(long nativeQueryPtr, long columnIndices[], String value, boolean caseSensitive);
    private native void nativeIsEmpty(long nativePtr, long[] columnIndices);
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
    private native void nativeIsNull(long nativePtr, long columnIndices[]);
    private native void nativeIsNotNull(long nativePtr, long columnIndices[]);
    private native long nativeCount(long nativeQueryPtr, long start, long end, long limit);
    private native long nativeRemove(long nativeQueryPtr);
    private native long nativeImportHandoverTableViewIntoSharedGroup(long handoverTableViewPtr, long callerSharedRealmPtr) throws BadVersionException;
    private native long nativeHandoverQuery(long callerSharedRealmPtr, long nativeQueryPtr);
    private static native long nativeFindAllSortedWithHandover(long bgSharedRealmPtr, long nativeQueryPtr, long start, long end, long limit, long columnIndex, boolean ascending) throws BadVersionException;
    private static native long nativeFindAllWithHandover(long bgSharedRealmPtr, long nativeQueryPtr, long start, long end, long limit) throws BadVersionException;
    private  static native long nativeGetDistinctViewWithHandover(long bgSharedRealmPtr, long nativeQueryPtr, long columnIndex) throws BadVersionException;
    private static native long nativeFindWithHandover(long bgSharedRealmPtr, long nativeQueryPtr, long fromTableRow);
    private static native long nativeFindAllMultiSortedWithHandover(long bgSharedRealmPtr, long nativeQueryPtr, long start, long end, long limit, long[] columnIndices, boolean[] ascending) throws BadVersionException;
    private static native long nativeImportHandoverRowIntoSharedGroup(long handoverRowPtr, long callerSharedRealmPtr);
    public static native void nativeCloseQueryHandover(long nativePtr);
    private static native long[] nativeBatchUpdateQueries(long bgSharedRealmPtr, long[] handoverQueries, long[][] parameters, long[][] queriesParameters, boolean[][] multiSortOrder) throws BadVersionException;
    private static native long nativeGetFinalizerPtr();
}
