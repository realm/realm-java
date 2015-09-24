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

/**
 * This class represents a view of a particular table. We can think of
 * a tableview is a subset of a table. It contains less than or
 * equal to the number of entries of a table. A table view is often a
 * result of a query.
 *
 * The view don't copy data from the table, but merely has a list of
 * row-references into the original table with the real data.
 *
 * The class serves as a base class of all table view. It is a raw level table view, users
 * are not encouraged to use this class. Instead users are advised to use
 * the generated subclass version of the table.
 *
 * Let us assume we are going to keep track of a table to store the employees
 * of a company or an organization.
 *
 * Following is a small example how to use the auto-generated class of the
 * tableview. For that purpose we will first define the spec of an employee
 * entity
 *
 *  @DefineTable
 *  public class employee {
 *      String name;
 *      long age;
 *      boolean hired;
 *      byte[] imageData;
 *  }
 *
 * Once this class is compiled along with Realm annotation processor
 * this will produce following classes.
 *
 * 1. Employee
 * 2. EmployeeTable
 * 3. EmployeeView.
 *
 * In this class context our interest is EmployeeView which will be inherited from
 * this class.
 *
 * The generated class will have more specialized method to do operations on TableView.
 *
 */
public class TableView implements TableOrView, Closeable {
    protected boolean DEBUG = false; //true;
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
     * Creates a TableView with already created Java TableView Object and a
     * native native TableView object reference. The method is not supposed to
     * be called by the user of the db. The method is for internal use only.
     *
     * @param context
     * @param parent A table.
     * @param nativePtr pointer to table view.
     * @param query a reference to the query which the table view is based
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
                
                if (DEBUG) 
                    System.err.println("==== TableView CLOSE, ptr= " + nativePtr);
              
                nativePtr = 0;
            } 
        }
    }
    
    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                context.asyncDisposeTableView(nativePtr);
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
            }
        }
    }

    protected static native void nativeClose(long nativeViewPtr);

    /**
     * Checks whether this table is empty or not.
     *
     * @return true if empty, otherwise false.
     */
    @Override
    public boolean isEmpty(){
        return size() == 0;
    }

    /**
     * Get the number of entries/rows of this table.
     *
     * @return The number of rows.
     */
    @Override
    public long size() {
        return nativeSize(nativePtr);
    }

    protected native long nativeSize(long nativeViewPtr);

    /**
     * Returns the index of the row in the source table
     * @param rowIndex in the TableView
     * @return the translated row number in the source table
     */
    public long getSourceRowIndex(long rowIndex) {
        return nativeGetSourceRowIndex(nativePtr, rowIndex);
    }

    protected native long nativeGetSourceRowIndex(long nativeViewPtr, long rowIndex);

    /**
     * Returns the number of columns in the table.
     *
     * @return the number of columns.
     */
    @Override
    public long getColumnCount() {
        return nativeGetColumnCount(nativePtr);
    }

    protected native long nativeGetColumnCount(long nativeViewPtr);

    /**
     * Returns the name of a column identified by columnIndex. Notice that the
     * index is zero based.
     *
     * @param columnIndex the column index
     * @return the name of the column
     */
    @Override
    public String getColumnName(long columnIndex) {
        return nativeGetColumnName(nativePtr, columnIndex);
    }

    protected native String nativeGetColumnName(long nativeViewPtr, long columnIndex);

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name
     * @return the index, -1 if not found
     */
    @Override
    public long getColumnIndex(String columnName) {
        if (columnName == null)
            throw new IllegalArgumentException("Column name can not be null.");
        return nativeGetColumnIndex(nativePtr, columnName);
    }
    
    protected native long nativeGetColumnIndex(long nativeViewPtr, String columnName);


    /**
     * Get the type of a column identified by the columnIndex.
     *
     * @param columnIndex index of the column.
     * @return Type of the particular column.
     */
    @Override
    public RealmFieldType getColumnType(long columnIndex) {
        return RealmFieldType.fromNativeValue(nativeGetColumnType(nativePtr, columnIndex));
    }

    protected native int nativeGetColumnType(long nativeViewPtr, long columnIndex);

    /**
     * Get the value of the particular (integer) cell.
     *
     * @param columnIndex 0 based index value of the column.
     * @param rowIndex 0 based row value of the column.
     * @return value of the particular cell.
     */
    @Override
    public long getLong(long columnIndex, long rowIndex){
        return nativeGetLong(nativePtr, columnIndex, rowIndex);
    }

    protected native long nativeGetLong(long nativeViewPtr, long columnIndex, long rowIndex);

    /**
     * Get the value of the particular (boolean) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public boolean getBoolean(long columnIndex, long rowIndex){
        return nativeGetBoolean(nativePtr, columnIndex, rowIndex);
    }

    protected native boolean nativeGetBoolean(long nativeViewPtr, long columnIndex, long rowIndex);

    /**
     * Get the value of the particular (float) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public float getFloat(long columnIndex, long rowIndex){
        return nativeGetFloat(nativePtr, columnIndex, rowIndex);
    }

    protected native float nativeGetFloat(long nativeViewPtr, long columnIndex, long rowIndex);

    /**
     * Get the value of the particular (double) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public double getDouble(long columnIndex, long rowIndex){
        return nativeGetDouble(nativePtr, columnIndex, rowIndex);
    }

    protected native double nativeGetDouble(long nativeViewPtr, long columnIndex, long rowIndex);

    /**
     * Get the value of the particular (date) cell.
     *
     * @param columnIndex 0 based index value of the cell column.
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell.
     */
    @Override
    public Date getDate(long columnIndex, long rowIndex){
        return new Date(nativeGetDateTimeValue(nativePtr, columnIndex, rowIndex)*1000);
    }

    protected native long nativeGetDateTimeValue(long nativeViewPtr, long columnIndex, long rowIndex);

    /**
     * Get the value of a (string )cell.
     *
     * @param columnIndex 0 based index value of the column
     * @param rowIndex 0 based index of the row.
     * @return value of the particular cell
     */
    @Override
    public String getString(long columnIndex, long rowIndex){
        return nativeGetString(nativePtr, columnIndex, rowIndex);
    }

    protected native String nativeGetString(long nativeViewPtr, long columnIndex, long rowIndex);


    /**
     * Get the  value of a (binary) cell.
     *
     * @param columnIndex 0 based index value of the cell column
     * @param rowIndex 0 based index value of the cell row
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

    protected native byte[] nativeGetByteArray(long nativePtr, long columnIndex, long rowIndex);

    @Override
    public RealmFieldType getMixedType(long columnIndex, long rowIndex) {
        return RealmFieldType.fromNativeValue(nativeGetMixedType(nativePtr, columnIndex, rowIndex));
    }

    protected native int nativeGetMixedType(long nativeViewPtr, long columnIndex, long rowIndex);

    @Override
    public Mixed getMixed(long columnIndex, long rowIndex){
        return nativeGetMixed(nativePtr, columnIndex, rowIndex);
    }

    protected native Mixed nativeGetMixed(long nativeViewPtr, long columnIndex, long rowIndex);


    public long getLink(long columnIndex, long rowIndex){
        return nativeGetLink(nativePtr, columnIndex, rowIndex);
    }

    protected native long nativeGetLink(long nativeViewPtr, long columnIndex, long rowIndex);

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

    protected native long nativeGetSubtable(long nativeViewPtr, long columnIndex, long rowIndex);

    @Override
    public long getSubtableSize(long columnIndex, long rowIndex) {
        return nativeGetSubtableSize(nativePtr, columnIndex, rowIndex);
    }

    protected native long nativeGetSubtableSize(long nativeTablePtr, long columnIndex, long rowIndex);

    @Override
    public void clearSubtable(long columnIndex, long rowIndex) {
        if (parent.isImmutable()) throwImmutable();
        nativeClearSubtable(nativePtr, columnIndex, rowIndex);
    }

    protected native void nativeClearSubtable(long nativeTablePtr, long columnIndex, long rowIndex);


    // Methods for setting values.

    /**
     * Sets the value for a particular (integer) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param value
     */
    @Override
    public void setLong(long columnIndex, long rowIndex, long value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetLong(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetLong(long nativeViewPtr, long columnIndex, long rowIndex, long value);

    /**
     * Sets the value for a particular (boolean) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param value
     */
    @Override
    public void setBoolean(long columnIndex, long rowIndex, boolean value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetBoolean(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetBoolean(long nativeViewPtr, long columnIndex, long rowIndex, boolean value);

    /**
     * Sets the value for a particular (float) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param value
     */
    @Override
    public void setFloat(long columnIndex, long rowIndex, float value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetFloat(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetFloat(long nativeViewPtr, long columnIndex, long rowIndex, float value);

    /**
     * Sets the value for a particular (double) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param value
     */
    @Override
    public void setDouble(long columnIndex, long rowIndex, double value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetDouble(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetDouble(long nativeViewPtr, long columnIndex, long rowIndex, double value);

    /**
     * Sets the value for a particular (date) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param value
     */
    @Override
    public void setDate(long columnIndex, long rowIndex, Date value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetDateTimeValue(nativePtr, columnIndex, rowIndex, value.getTime()/1000);
    }

    protected native void nativeSetDateTimeValue(long nativePtr, long columnIndex, long rowIndex, long dateTimeValue);

    /**
     * Sets the value for a particular (sting) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param value
     */
    @Override
    public void setString(long columnIndex, long rowIndex, String value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetString(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetString(long nativeViewPtr, long columnIndex, long rowIndex, String value);

    /**
     * Sets the value for a particular (binary) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param data
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

    protected native void nativeSetByteArray(long nativePtr, long columnIndex, long rowIndex, byte[] data);

    /**
     * Sets the value for a particular (mixed typed) cell.
     *
     * @param columnIndex column index of the cell
     * @param rowIndex row index of the cell
     * @param data
     */
    @Override
    public void setMixed(long columnIndex, long rowIndex, Mixed data){
        if (parent.isImmutable()) throwImmutable();
        nativeSetMixed(nativePtr, columnIndex, rowIndex, data);
    }

    protected native void nativeSetMixed(long nativeViewPtr, long columnIndex, long rowIndex, Mixed value);

    public void setLink(long columnIndex, long rowIndex, long value){
        if (parent.isImmutable()) throwImmutable();
        nativeSetLink(nativePtr, columnIndex, rowIndex, value);
    }

    protected native void nativeSetLink(long nativeViewPtr, long columnIndex, long rowIndex, long value);


    public boolean isNullLink(long columnIndex, long rowIndex) {
        return nativeIsNullLink(nativePtr, columnIndex, rowIndex);
    }

    protected native boolean nativeIsNullLink(long nativePtr, long columnIndex, long rowIndex);

    public void nullifyLink(long columnIndex, long rowIndex) {
        nativeNullifyLink(nativePtr, columnIndex, rowIndex);
    }

    protected native void nativeNullifyLink(long nativePtr, long columnIndex, long rowIndex);

    // Methods for deleting.
    @Override
    public void clear(){
        if (parent.isImmutable()) throwImmutable();
        nativeClear(nativePtr);
    }

    protected native void nativeClear(long nativeViewPtr);

    /**
     * Removes a particular row identified by the index from the tableview.
     * The corresponding row of the underlying table also get deleted.
     *
     * @param rowIndex the row index
     */
    @Override
    public void remove(long rowIndex){
        if (parent.isImmutable()) throwImmutable();
        nativeRemoveRow(nativePtr, rowIndex);
    }

    protected native void nativeRemoveRow(long nativeViewPtr, long rowIndex);

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

    protected native long nativeFindFirstInt(long nativeTableViewPtr, long columnIndex, long value);

    @Override
    public long findFirstBoolean(long columnIndex, boolean value) {
        return nativeFindFirstBool(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstBool(long nativePtr, long columnIndex, boolean value);

    @Override
    public long findFirstFloat(long columnIndex, float value) {
        return nativeFindFirstFloat(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstFloat(long nativePtr, long columnIndex, float value);

    @Override
    public long findFirstDouble(long columnIndex, double value) {
        return nativeFindFirstDouble(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstDouble(long nativePtr, long columnIndex, double value);

    @Override
    public long findFirstDate(long columnIndex, Date date) {
        return nativeFindFirstDate(nativePtr, columnIndex, date.getTime()/1000);
    }

    protected native long nativeFindFirstDate(long nativeTablePtr, long columnIndex, long dateTimeValue);

    @Override
    public long findFirstString(long columnIndex, String value){
        return nativeFindFirstString(nativePtr, columnIndex, value);
    }

    protected native long nativeFindFirstString(long nativePtr, long columnIndex, String value);


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

    protected native long nativeFindAllInt(long nativePtr, long columnIndex, long value);

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

    protected native long nativeFindAllBool(long nativePtr, long columnIndex, boolean value);

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

    protected native long nativeFindAllFloat(long nativePtr, long columnIndex, float value);

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

    protected native long nativeFindAllDouble(long nativePtr, long columnIndex, double value);

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

    protected native long nativeFindAllDate(long nativePtr, long columnIndex, long dateTimeValue);

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

    protected native long nativeFindAllString(long nativePtr, long columnIndex, String value);



    //
    // Integer Aggregates
    //

    /**
     * Calculate the sum of the values in a particular column of this
     * tableview.
     *
     * Note: the type of the column marked by the columnIndex has to be of
     * type RealmFieldType.ColumnTypeInt.
     *
     * @param columnIndex column index
     * @return the sum of the values in the column
     */
    @Override
    public long sumLong(long columnIndex){
        return nativeSumInt(nativePtr, columnIndex);
    }

    protected native long nativeSumInt(long nativeViewPtr, long columnIndex);

    /**
     * Returns the maximum value of the cells in a column.
     *
     * Note: for this method to work the Type of the column
     * identified by the columnIndex has to be RealmFieldType.ColumnTypeInt.
     *
     * @param columnIndex column index
     * @return the maximum value
     */
    @Override
    public long maximumLong(long columnIndex){
        return nativeMaximumInt(nativePtr, columnIndex);
    }

    protected native long nativeMaximumInt(long nativeViewPtr, long columnIndex);

    /**
     * Returns the minimum value of the cells in a column.
     *
     * Note: for this method to work the Type of the column
     * identified by the columnIndex has to be RealmFieldType.ColumnTypeInt.
     *
     * @param columnIndex column index
     * @return the minimum value
     */
    @Override
    public long minimumLong(long columnIndex){
        return nativeMinimumInt(nativePtr, columnIndex);
    }

    protected native long nativeMinimumInt(long nativeViewPtr, long columnIndex);

    @Override
    public double averageLong(long columnIndex) {
        return nativeAverageInt(nativePtr, columnIndex);
    }

    protected native double nativeAverageInt(long nativePtr, long columnIndex);


    // Float aggregates

    @Override
    public double sumFloat(long columnIndex){
        return nativeSumFloat(nativePtr, columnIndex);
    }

    protected native double nativeSumFloat(long nativeViewPtr, long columnIndex);

    @Override
    public float maximumFloat(long columnIndex){
        return nativeMaximumFloat(nativePtr, columnIndex);
    }

    protected native float nativeMaximumFloat(long nativeViewPtr, long columnIndex);

    @Override
    public float minimumFloat(long columnIndex){
        return nativeMinimumFloat(nativePtr, columnIndex);
    }

    protected native float nativeMinimumFloat(long nativeViewPtr, long columnIndex);

    @Override
    public double averageFloat(long columnIndex) {
        return nativeAverageFloat(nativePtr, columnIndex);
    }

    protected native double nativeAverageFloat(long nativePtr, long columnIndex);


    // Double aggregates

    @Override
    public double sumDouble(long columnIndex){
        return nativeSumDouble(nativePtr, columnIndex);
    }

    protected native double nativeSumDouble(long nativeViewPtr, long columnIndex);

    @Override
    public double maximumDouble(long columnIndex){
        return nativeMaximumDouble(nativePtr, columnIndex);
    }

    protected native double nativeMaximumDouble(long nativeViewPtr, long columnIndex);

    @Override
    public double minimumDouble(long columnIndex){
        return nativeMinimumDouble(nativePtr, columnIndex);
    }

    protected native double nativeMinimumDouble(long nativeViewPtr, long columnIndex);

    @Override
    public double averageDouble(long columnIndex) {
        return nativeAverageDouble(nativePtr, columnIndex);
    }

    protected native double nativeAverageDouble(long nativePtr, long columnIndex);


    // Date aggregates

    @Override
    public Date maximumDate(long columnIndex) {
        return new Date(nativeMaximumDate(nativePtr, columnIndex) * 1000);
    }

    protected native long nativeMaximumDate(long nativePtr, long columnIndex);

    @Override
    public Date minimumDate(long columnIndex) {
        return new Date(nativeMinimumDate(nativePtr, columnIndex) * 1000);
    }

    protected native long nativeMinimumDate(long nativePtr, long columnIndex);


    // Sorting

    public enum Order { ascending, descending }

    public void sort(long columnIndex, Order order) {
        // Don't check for immutable. Sorting does not modify original table
        nativeSort(nativePtr, columnIndex, (order == Order.ascending));
    }

    public void sort(long columnIndex) {
        // Don't check for immutable. Sorting does not modify original table
        nativeSort(nativePtr, columnIndex, true);
    }

    protected native void nativeSort(long nativeTableViewPtr, long columnIndex, boolean ascending);

    public void sort(List<Long> columnIndices, List<Order> order) {
        long indices[] = new long[columnIndices.size()];
        boolean sortOrder[] = new boolean[order.size()];
        for (int i = 0; i < columnIndices.size(); i++) {
            indices[i] = columnIndices.get(i);
        }
        for (int i = 0; i < order.size(); i++) {
            sortOrder[i] = order.get(i) == Order.ascending;
        }
        nativeSortMulti(nativePtr, indices, sortOrder);
    }

    protected native void nativeSortMulti(long nativeTableViewPtr, long columnIndices[], boolean ascending[]);

    protected native long createNativeTableView(Table table, long nativeTablePtr);

    @Override
    public String toJson() {
        return nativeToJson(nativePtr);
    }

    protected native String nativeToJson(long nativeViewPtr);

    @Override
    public String toString() {
        return nativeToString(nativePtr, 500);
    }

    @Override
    public String toString(long maxRows) {
        return nativeToString(nativePtr, maxRows);
    }

    protected native String nativeToString(long nativeTablePtr, long maxRows);

    @Override
    public String rowToString(long rowIndex) {
        return nativeRowToString(nativePtr, rowIndex);
    }

    protected native String nativeRowToString(long nativeTablePtr, long rowIndex);

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

    protected native long nativeWhere(long nativeViewPtr);

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

    protected native void nativePivot(long nativeTablePtr, long stringCol, long intCol, int pivotType, long result);


    @Override
    public long sync() {
        return nativeSync(nativePtr);
    }

    protected native long nativeSync(long nativeTablePtr);
}
