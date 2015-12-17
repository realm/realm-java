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

import java.io.Closeable;
import java.util.Date;
import java.util.List;

import io.realm.RealmFieldType;
import io.realm.Sort;
import io.realm.internal.log.RealmLog;

/**
 * This class represents a view of a particular table. We can think of a tableview as a subset of a table. It contains
 * less than or equal to the number of entries of a table. A table view is often a result of a query.
 *
 * The view doesn't copy data from the table, but contains merely a list of row-references into the original table
 * with the real data.
 */
public class TableView implements TableOrView, Closeable {
    private static final boolean DEBUG = false; //true;
    // Don't convert this into local variable and don't remove this.
    // Core requests TableView to hold the Query reference.
    @SuppressWarnings({"unused"})
    private final TableQuery query; // the query which created this TableView

    /**
     * Creates a TableView. This constructor is used if the TableView is created from a table.
     *
     * @param context
     * @param parent
     * @param nativePtr
     */
    protected TableView(Context context, Table parent, long nativePtr) {
        this.context = context;
        this.parent = parent;
        this.nativePtr = nativePtr;
        this.query = null;
    }

    /**
     * Creates a TableView with already created Java TableView Object and a native native TableView object reference.
     * The method is not supposed to be called by the user of the db. The method is for internal use only.
     *
     * @param context
     * @param parent A table.
     * @param nativePtr pointer to table view.
     * @param query a reference to the query which the table view is based.
     */
    protected TableView(Context context, Table parent, long nativePtr, TableQuery query) {
        this.context = context;
        this.parent = parent;
        this.nativePtr = nativePtr;
        this.query = query;
    }

    @Override
    public Table getTable() {
        return parent;
    }

    @Override
    public void close() {
        synchronized (context) {
            if (nativePtr != 0) {
                nativeClose(nativePtr);
                
                if (DEBUG) {
                    RealmLog.d("==== TableView CLOSE, ptr= " + nativePtr);
                }
                nativePtr = 0;
            } 
        }
    }

    @Override
    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                context.asyncDisposeTableView(nativePtr);
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
            }
        }
    }

    /**
     * Checks whether this table is empty or not.
     *
     * @return {@code true} if empty, otherwise {@code false}.
     */
    @Override
    public boolean isEmpty(){
        return size() == 0;
    }

    /**
     * Gets the number of entries/rows of this table.
     *
     * @return the number of rows.
     */
    @Override
    public long size() {
        return nativeSize(nativePtr);
    }

    /**
     * Returns the index of the row in the source table.
     *
     * @param rowIndex row index in the TableView.
     * @return the translated row number in the source table.
     */
    public long getSourceRowIndex(long rowIndex) {
        return nativeGetSourceRowIndex(nativePtr, rowIndex);
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the number of columns.
     */
    @Override
    public long getColumnCount() {
        return nativeGetColumnCount(nativePtr);
    }

    /**
     * Returns the name of a column identified by columnIndex. Notice that the index is zero based.
     *
     * @param columnIndex the column index.
     * @return the name of the column.
     */
    @Override
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePtr, columnIndex);
    }

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name.
     * @return the index, {@code -1} if not found.
     */
    @Override
    public long getColumnIndex(String columnName) {
        if (columnName == null)
            throw new IllegalArgumentException("Column name can not be null.");
        return nativeGetColumnIndex(nativePtr, columnName);
    }

    /**
     * Gets the type of a column identified by the columnIndex.
     *
     * @param columnIndex index of the column.
     * @return type of the particular column.
     */
    @Override
    public RealmFieldType getColumnType(long columnIndex) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativePtr, columnIndex));
    }

    /**
     * Gets the value of the particular (integer) cell.
     *
     * @param columnIndex 0 based index value of the column.
     * @param rowIndex 0 based row value of the column.
     * @return value of the particular cell.
     */
    @Override
    public long getLong(long columnIndex, long rowIndex){
        return nativeGetLong(nativePtr, columnIndex, rowIndex);
    }

    /**
     * Gets the value of the particular (boolean) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public boolean getBoolean(long columnIndex, long rowIndex){
        return nativeGetBoolean(nativePtr, columnIndex, rowIndex);
    }

    /**
     * Gets the value of the particular (float) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public float getFloat(long columnIndex, long rowIndex){
        return nativeGetFloat(nativePtr, columnIndex, rowIndex);
    }

    /**
     * Gets the value of the particular (double) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public double getDouble(long columnIndex, long rowIndex){
        return nativeGetDouble(nativePtr, columnIndex, rowIndex);
    }

    /**
     * Gets the value of the particular (date) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public Date getDate(long columnIndex, long rowIndex){
        return new Date(nativeGetDateTimeValue(nativePtr, columnIndex, rowIndex)*1000);
    }

    /**
     * Gets the value of a (string )cell.
     *
     * @param columnIndex 0 based index value of the column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public String getString(long columnIndex, long rowIndex){
        return nativeGetString(nativePtr, columnIndex, rowIndex);
    }

    /**
     * Gets the  value of a (binary) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index value of the cell row.
     * @return value of the particular cell.
     */
    /*
    @Override
    public ByteBuffer getBinaryByteBuffer(long columnIndex, long rowIndex){
        return nativeGetBinary(nativePtr, columnIndex, rowIndex);
    }

    protected native ByteBuffer nativeGetBinary(long nativeViewPtr, long columnIndex, long rowIndex);
*/

    @Override
    public byte[] getBinaryByteArray(long columnIndex, long rowIndex){
        return nativeGetByteArray(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public RealmFieldType getMixedType(long columnIndex, long rowIndex) {
        return RealmFieldType.fromNativeValue(nativeGetMixedType(nativePtr, columnIndex, rowIndex));
    }

    @Override
    public Mixed getMixed(long columnIndex, long rowIndex){
        return nativeGetMixed(nativePtr, columnIndex, rowIndex);
    }

    public long getLink(long columnIndex, long rowIndex){
        return nativeGetLink(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public Table getSubtable(long columnIndex, long rowIndex) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeSubtablePtr = nativeGetSubtable(nativePtr, columnIndex, rowIndex);
        try {
            // Copy context reference from parent
            return new Table(context, this.parent, nativeSubtablePtr);
        }
        catch (RuntimeException e) {
            Table.nativeClose(nativeSubtablePtr);
            throw e;
        }
    }

    @Override
    public long getSubtableSize(long columnIndex, long rowIndex) {
        return nativeGetSubtableSize(nativePtr, columnIndex, rowIndex);
    }

    @Override
    public void clearSubtable(long columnIndex, long rowIndex) {
        if (parent.isImmutable()) throwImmutable();
        nativeClearSubtable(nativePtr, columnIndex, rowIndex);
    }

    // Methods for setting values.

    /**
     * Sets the value for a particular (integer) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param value the value.
     */
    @Override
    public void setLong(long columnIndex, long rowIndex, long value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetLong(nativePtr, columnIndex, rowIndex, value);
    }

    /**
     * Sets the value for a particular (boolean) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param value the value.
     */
    @Override
    public void setBoolean(long columnIndex, long rowIndex, boolean value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetBoolean(nativePtr, columnIndex, rowIndex, value);
    }

    /**
     * Sets the value for a particular (float) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param value the value.
     */
    @Override
    public void setFloat(long columnIndex, long rowIndex, float value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetFloat(nativePtr, columnIndex, rowIndex, value);
    }

    /**
     * Sets the value for a particular (double) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param value the value.
     */
    @Override
    public void setDouble(long columnIndex, long rowIndex, double value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetDouble(nativePtr, columnIndex, rowIndex, value);
    }

    /**
     * Sets the value for a particular (date) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param value the value.
     */
    @Override
    public void setDate(long columnIndex, long rowIndex, Date value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetDateTimeValue(nativePtr, columnIndex, rowIndex, value.getTime()/1000);
    }

    /**
     * Sets the value for a particular (sting) cell.
     *
     * @param columnIndex column index of the.
     * @param rowIndex row index of the cell.
     * @param value the value.
     */
    @Override
    public void setString(long columnIndex, long rowIndex, String value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetString(nativePtr, columnIndex, rowIndex, value);
    }

    /**
     * Sets the value for a particular (binary) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param data the value.
     */
    /*
    @Override
    public void setBinaryByteBuffer(long columnIndex, long rowIndex, ByteBuffer data){
        if (immutable) throwImmutable();
        nativeSetBinary(nativePtr, columnIndex, rowIndex, data);
    }

    protected native void nativeSetBinary(long nativeViewPtr, long columnIndex, long rowIndex, ByteBuffer data);
    */

    @Override
    public void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data){
        if (parent.isImmutable()) throwImmutable();
        nativeSetByteArray(nativePtr, columnIndex, rowIndex, data);
    }

    /**
     * Sets the value for a particular (mixed typed) cell.
     *
     * @param columnIndex column index of the cell.
     * @param rowIndex row index of the cell.
     * @param data the value.
     */
    @Override
    public void setMixed(long columnIndex, long rowIndex, Mixed data){
        if (parent.isImmutable()) throwImmutable();
        nativeSetMixed(nativePtr, columnIndex, rowIndex, data);
    }

    public void setLink(long columnIndex, long rowIndex, long value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetLink(nativePtr, columnIndex, rowIndex, value);
    }

    public boolean isNullLink(long columnIndex, long rowIndex) {
        return nativeIsNullLink(nativePtr, columnIndex, rowIndex);
    }

    public void nullifyLink(long columnIndex, long rowIndex) {
        nativeNullifyLink(nativePtr, columnIndex, rowIndex);
    }

    // Methods for deleting.
    @Override
    public void clear(){
        if (parent.isImmutable()) throwImmutable();
        nativeClear(nativePtr);
    }

    /**
     * Removes a particular row identified by the index from the tableview.
     * The corresponding row of the underlying table also get deleted.
     *
     * @param rowIndex the row index.
     */
    @Override
    public void remove(long rowIndex){
        if (parent.isImmutable()) throwImmutable();
        nativeRemoveRow(nativePtr, rowIndex);
    }

    @Override
    public void removeLast() {
        if (parent.isImmutable()) throwImmutable();
        if (!isEmpty()) {
            nativeRemoveRow(nativePtr, size() - 1);
        }
    }

    // Search for first match
    @Override
    public long findFirstLong(long columnIndex, long value){
        return nativeFindFirstInt(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstBoolean(long columnIndex, boolean value) {
        return nativeFindFirstBool(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstFloat(long columnIndex, float value) {
        return nativeFindFirstFloat(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstDouble(long columnIndex, double value) {
        return nativeFindFirstDouble(nativePtr, columnIndex, value);
    }

    @Override
    public long findFirstDate(long columnIndex, Date date) {
        return nativeFindFirstDate(nativePtr, columnIndex, date.getTime()/1000);
    }

    @Override
    public long findFirstString(long columnIndex, String value){
        return nativeFindFirstString(nativePtr, columnIndex, value);
    }

    // Search for all matches

    // TODO..
    @Override
    public long lowerBoundLong(long columnIndex, long value) {
        throw new RuntimeException("Not implemented yet");
    }

    // TODO..
    @Override
    public long upperBoundLong(long columnIndex, long value) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public TableView findAllLong(long columnIndex, long value){
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllInt(nativePtr, columnIndex, value);
        try { 
            return new TableView(this.context, this.parent, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    @Override
    public TableView findAllBoolean(long columnIndex, boolean value) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllBool(nativePtr, columnIndex, value);
        try { 
            return new TableView(this.context, this.parent, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }  
    }

    @Override
    public TableView findAllFloat(long columnIndex, float value) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllFloat(nativePtr, columnIndex, value);
        try { 
            return new TableView(this.context, this.parent, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }  
    }

    @Override
    public TableView findAllDouble(long columnIndex, double value) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllDouble(nativePtr, columnIndex, value);
        try { 
            return new TableView(this.context, this.parent, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }   
    }

    @Override
    public TableView findAllDate(long columnIndex, Date date) {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllDate(nativePtr, columnIndex, date.getTime()/1000);
        try { 
            return new TableView(this.context, this.parent, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }  
    }

    @Override
    public TableView findAllString(long columnIndex, String value){
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAllString(nativePtr, columnIndex, value);
        try { 
            return new TableView(this.context, this.parent, nativeViewPtr);
        } catch (RuntimeException e) {
            TableView.nativeClose(nativeViewPtr);
            throw e;
        }
    }

    //
    // Integer Aggregates
    //

    /**
     * Calculate the sum of the values in a particular column of this tableview.
     *
     * Note: the type of the column marked by the columnIndex has to be of type RealmFieldType.INTEGER.
     *
     * @param columnIndex column index.
     * @return the sum of the values in the column.
     */
    @Override
    public long sumLong(long columnIndex){
        return nativeSumInt(nativePtr, columnIndex);
    }

    /**
     * Returns the maximum value of the cells in a column.
     *
     * Note: for this method to work the Type of the column identified by the columnIndex has to be
     * RealmFieldType.INTEGER.
     *
     * @param columnIndex column index.
     * @return the maximum value.
     */
    @Override
    public Long maximumLong(long columnIndex){
        return nativeMaximumInt(nativePtr, columnIndex);
    }

    /**
     * Returns the minimum value of the cells in a column.
     *
     * Note: for this method to work the Type of the column identified by the columnIndex has to be
     * RealmFieldType.INTEGER.
     *
     * @param columnIndex column index.
     * @return the minimum value.
     */
    @Override
    public Long minimumLong(long columnIndex){
        return nativeMinimumInt(nativePtr, columnIndex);
    }

    @Override
    public double averageLong(long columnIndex) {
        return nativeAverageInt(nativePtr, columnIndex);
    }

    // Float aggregates

    @Override
    public double sumFloat(long columnIndex){
        return nativeSumFloat(nativePtr, columnIndex);
    }

    @Override
    public Float maximumFloat(long columnIndex){
        return nativeMaximumFloat(nativePtr, columnIndex);
    }

    @Override
    public Float minimumFloat(long columnIndex){
        return nativeMinimumFloat(nativePtr, columnIndex);
    }

    @Override
    public double averageFloat(long columnIndex) {
        return nativeAverageFloat(nativePtr, columnIndex);
    }

    // Double aggregates

    @Override
    public double sumDouble(long columnIndex){
        return nativeSumDouble(nativePtr, columnIndex);
    }

    @Override
    public Double maximumDouble(long columnIndex){
        return nativeMaximumDouble(nativePtr, columnIndex);
    }


    @Override
    public Double minimumDouble(long columnIndex){
        return nativeMinimumDouble(nativePtr, columnIndex);
    }

    @Override
    public double averageDouble(long columnIndex) {
        return nativeAverageDouble(nativePtr, columnIndex);
    }

    // Date aggregates

    @Override
    public Date maximumDate(long columnIndex) {
        Long result = nativeMaximumDate(nativePtr, columnIndex);
        if (result == null) {
            return null;
        }
        return new Date(result * 1000);
    }

    @Override
    public Date minimumDate(long columnIndex) {
        Long result = nativeMinimumDate(nativePtr, columnIndex);
        if (result == null) {
            return null;
        }
        return new Date(result * 1000);
    }

    // Sorting
    public void sort(long columnIndex, Sort sortOrder) {
        // Don't check for immutable. Sorting does not modify original table
        nativeSort(nativePtr, columnIndex, sortOrder.getValue());
    }

    public void sort(long columnIndex) {
        // Don't check for immutable. Sorting does not modify original table
        nativeSort(nativePtr, columnIndex, true);
    }

    public void sort(List<Long> columnIndices, Sort[] sortOrders) {
        long indices[] = new long[columnIndices.size()];
        for (int i = 0; i < columnIndices.size(); i++) {
            indices[i] = columnIndices.get(i);
        }
        boolean nativeSortOrder[] = TableQuery.getNativeSortOrderValues(sortOrders);
        nativeSortMulti(nativePtr, indices, nativeSortOrder);
    }

    @Override
    public String toJson() {
        return nativeToJson(nativePtr);
    }

    @Override
    public String toString() {
        return nativeToString(nativePtr, 500);
    }

    @Override
    public String toString(long maxRows) {
        return nativeToString(nativePtr, maxRows);
    }

    @Override
    public String rowToString(long rowIndex) {
        return nativeRowToString(nativePtr, rowIndex);
    }

    @Override
    public TableQuery where() {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        this.context.executeDelayedDisposal();
        long nativeQueryPtr = nativeWhere(nativePtr);
        try {
            return new TableQuery(this.context, this.parent, nativeQueryPtr, this);
        } catch (RuntimeException e) {
            TableQuery.nativeClose(nativeQueryPtr);
            throw e;
        }
    }

    private void throwImmutable() {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }

    protected long nativePtr;
    protected final Table parent;
    private final Context context;

    @Override
    public long count(long columnIndex, String value) {
        // TODO: implement
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public Table pivot(long stringCol, long intCol, PivotType pivotType){
        if (! this.getColumnType(stringCol).equals(RealmFieldType.STRING ))
            throw new UnsupportedOperationException("Group by column must be of type String");
        if (! this.getColumnType(intCol).equals(RealmFieldType.INTEGER ))
            throw new UnsupportedOperationException("Aggregation column must be of type Int");
        Table result = new Table();
        nativePivot(nativePtr, stringCol, intCol, pivotType.value, result.nativePtr);
        return result;
   }

    @Override
    public long sync() {
        return nativeSync(nativePtr);
    }

    static native void nativeClose(long nativeViewPtr);
    private native long nativeSize(long nativeViewPtr);
    private native long nativeGetSourceRowIndex(long nativeViewPtr, long rowIndex);
    private native long nativeGetColumnCount(long nativeViewPtr);
    private native String nativeGetColumnName(long nativeViewPtr, long columnIndex);
    private native long nativeGetColumnIndex(long nativeViewPtr, String columnName);
    private native int nativeGetColumnType(long nativeViewPtr, long columnIndex);
    private native long nativeGetLong(long nativeViewPtr, long columnIndex, long rowIndex);
    private native boolean nativeGetBoolean(long nativeViewPtr, long columnIndex, long rowIndex);
    private native float nativeGetFloat(long nativeViewPtr, long columnIndex, long rowIndex);
    private native double nativeGetDouble(long nativeViewPtr, long columnIndex, long rowIndex);
    private native long nativeGetDateTimeValue(long nativeViewPtr, long columnIndex, long rowIndex);
    private native String nativeGetString(long nativeViewPtr, long columnIndex, long rowIndex);
    private native byte[] nativeGetByteArray(long nativePtr, long columnIndex, long rowIndex);
    private native int nativeGetMixedType(long nativeViewPtr, long columnIndex, long rowIndex);
    private native Mixed nativeGetMixed(long nativeViewPtr, long columnIndex, long rowIndex);
    private native long nativeGetLink(long nativeViewPtr, long columnIndex, long rowIndex);
    private native long nativeGetSubtable(long nativeViewPtr, long columnIndex, long rowIndex);
    private native long nativeGetSubtableSize(long nativeTablePtr, long columnIndex, long rowIndex);
    private native void nativeClearSubtable(long nativeTablePtr, long columnIndex, long rowIndex);
    private native void nativeSetLong(long nativeViewPtr, long columnIndex, long rowIndex, long value);
    private native void nativeSetBoolean(long nativeViewPtr, long columnIndex, long rowIndex, boolean value);
    private native void nativeSetFloat(long nativeViewPtr, long columnIndex, long rowIndex, float value);
    private native void nativeSetDouble(long nativeViewPtr, long columnIndex, long rowIndex, double value);
    private native void nativeSetDateTimeValue(long nativePtr, long columnIndex, long rowIndex, long dateTimeValue);
    private native void nativeSetString(long nativeViewPtr, long columnIndex, long rowIndex, String value);
    private native void nativeSetByteArray(long nativePtr, long columnIndex, long rowIndex, byte[] data);
    private native void nativeSetMixed(long nativeViewPtr, long columnIndex, long rowIndex, Mixed value);
    private native void nativeSetLink(long nativeViewPtr, long columnIndex, long rowIndex, long value);
    private native boolean nativeIsNullLink(long nativePtr, long columnIndex, long rowIndex);
    private native void nativeNullifyLink(long nativePtr, long columnIndex, long rowIndex);
    private native void nativeClear(long nativeViewPtr);
    private native void nativeRemoveRow(long nativeViewPtr, long rowIndex);
    private native long nativeFindFirstInt(long nativeTableViewPtr, long columnIndex, long value);
    private native long nativeFindFirstBool(long nativePtr, long columnIndex, boolean value);
    private native long nativeFindFirstFloat(long nativePtr, long columnIndex, float value);
    private native long nativeFindFirstDouble(long nativePtr, long columnIndex, double value);
    private native long nativeFindFirstDate(long nativeTablePtr, long columnIndex, long dateTimeValue);
    private native long nativeFindFirstString(long nativePtr, long columnIndex, String value);
    private native long nativeFindAllInt(long nativePtr, long columnIndex, long value);
    private native long nativeFindAllBool(long nativePtr, long columnIndex, boolean value);
    private native long nativeFindAllFloat(long nativePtr, long columnIndex, float value);
    private native long nativeFindAllDouble(long nativePtr, long columnIndex, double value);
    private native long nativeFindAllDate(long nativePtr, long columnIndex, long dateTimeValue);
    private native long nativeSumInt(long nativeViewPtr, long columnIndex);
    private native long nativeFindAllString(long nativePtr, long columnIndex, String value);
    private native Long nativeMaximumInt(long nativeViewPtr, long columnIndex);
    private native Long nativeMinimumInt(long nativeViewPtr, long columnIndex);
    private native double nativeAverageInt(long nativePtr, long columnIndex);
    private native double nativeSumFloat(long nativeViewPtr, long columnIndex);
    private native Float nativeMaximumFloat(long nativeViewPtr, long columnIndex);
    private native Float nativeMinimumFloat(long nativeViewPtr, long columnIndex);
    private native double nativeAverageFloat(long nativePtr, long columnIndex);
    private native double nativeSumDouble(long nativeViewPtr, long columnIndex);
    private native Double nativeMaximumDouble(long nativeViewPtr, long columnIndex);
    private native Double nativeMinimumDouble(long nativeViewPtr, long columnIndex);
    private native double nativeAverageDouble(long nativePtr, long columnIndex);
    private native Long nativeMaximumDate(long nativePtr, long columnIndex);
    private native Long nativeMinimumDate(long nativePtr, long columnIndex);
    private native void nativeSort(long nativeTableViewPtr, long columnIndex, boolean sortOrder);
    private native void nativeSortMulti(long nativeTableViewPtr, long columnIndices[], boolean ascending[]);
    private native long createNativeTableView(Table table, long nativeTablePtr);
    private native String nativeToJson(long nativeViewPtr);
    private native String nativeToString(long nativeTablePtr, long maxRows);
    private native String nativeRowToString(long nativeTablePtr, long rowIndex);
    private native long nativeWhere(long nativeViewPtr);
    private native void nativePivot(long nativeTablePtr, long stringCol, long intCol, int pivotType, long result);
    private native long nativeSync(long nativeTablePtr);
}
