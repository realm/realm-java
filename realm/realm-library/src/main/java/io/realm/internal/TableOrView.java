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

import io.realm.Realm;
import io.realm.RealmFieldType;

/**
 * Specification of the common operations for the low-level table and view API.
 */
public interface TableOrView {

    int NO_MATCH = -1;

    void clear();

    /**
     * Returns the table.
     *
     * @return
     */
    Table getTable();

    void close();

    /**
     * Returns the number of entries of the table/view.
     *
     * @return
     */
    long size();

    /**
     * Checks whether the table/view is empty or not.
     *
     * @return {@code true} if empty, otherwise {@code false}.
     */
    boolean isEmpty();

    /**
     * Removes a particular row identified by the index from the table/view.
     * [citation needed] The corresponding row of the table also gets deleted for which the table/view is part of.
     *
     * @param index
     */
    void remove(long index);

    void removeLast();

    long getColumnCount();

    String getColumnName(long columnIndex);

    long getColumnIndex(String name);

    RealmFieldType getColumnType(long columnIndex);

    /**
     * Gets the long value of a cell of the table/view identified by the columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    long getLong(long columnIndex, long rowIndex);

    /**
     * Gets the boolean value of a cell of the table identified by the columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    boolean getBoolean(long columnIndex, long rowIndex);

    /**
     * Gets the float value of a cell of the table identified by the columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    float getFloat(long columnIndex, long rowIndex);

    /**
     * Gets the double value of a cell of the table identified by the columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    double getDouble(long columnIndex, long rowIndex);

    /**
     * Gets the string value of a cell identified by the columnIndex and rowIndex of the cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    String getString(long columnIndex, long rowIndex);

    /**
     * Returns the Date value (java.util.Date) for a particular cell specified by the columnIndex and rowIndex of the
     * cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    Date getDate(long columnIndex, long rowIndex);

    /**
     * Returns the binary data for a cell identified by the columnIndex and rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    //ByteBuffer getBinaryByteBuffer(long columnIndex, long rowIndex);

    byte[] getBinaryByteArray(long columnIndex, long rowIndex);

    Mixed getMixed(long columnIndex, long rowIndex);

    /**
     * Gets the link index of a cell of the table/view identified by the columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    long getLink(long columnIndex, long rowIndex);

    RealmFieldType getMixedType(long columnIndex, long rowIndex);

    Table getSubtable(long columnIndex, long rowIndex);

    void clearSubtable(long columnIndex, long rowIndex);

    long getSubtableSize(long columnIndex, long rowIndex);

    /**
     * Sets the long value for a particular cell identified by columnIndex and rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setLong(long columnIndex, long rowIndex, long value);

    /**
     * Sets the boolean value of a cell identified by the columnIndex and the rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setBoolean(long columnIndex, long rowIndex, boolean value);

    /**
     * Sets the float value of a cell identified by the columnIndex and the rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setFloat(long columnIndex, long rowIndex, float value);

    /**
     * Sets the double value of a cell identified by the columnIndex and the rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setDouble(long columnIndex, long rowIndex, double value);

    /**
     * Sets the string value of a particular cell of the table/view identified by the columnIndex and the rowIndex of
     * this table/view
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setString(long columnIndex, long rowIndex, String value);

    /**
     * Sets the binary value for a particular cell identified by the rowIndex and columnIndex of the cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param data
     */
    //void setBinaryByteBuffer(long columnIndex, long rowIndex, ByteBuffer data);

    void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data);

    void setDate(long columnIndex, long rowIndex, Date date);

    void setMixed(long columnIndex, long rowIndex, Mixed data);

    boolean isNullLink(long columnIndex, long rowIndex);

    void nullifyLink(long columnIndex, long rowIndex);

    /**
     * Sets the link index for a particular cell identified by columnIndex and rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setLink(long columnIndex, long rowIndex, long value);

    long sumLong(long columnIndex);

    Long maximumLong(long columnIndex);

    Long minimumLong(long columnIndex);

    double averageLong(long columnIndex);


    double sumFloat(long columnIndex);

    Float maximumFloat(long columnIndex);

    Float minimumFloat(long columnIndex);

    double averageFloat(long columnIndex);


    double sumDouble(long columnIndex);

    Double maximumDouble(long columnIndex);

    Double minimumDouble(long columnIndex);

    double averageDouble(long columnIndex);


    Date maximumDate(long columnIndex);

    Date minimumDate(long columnIndex);


    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex the column to search in.
     * @param value the value to search for.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstLong(long columnIndex, long value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex the column to search in.
     * @param value the alue to search for.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstBoolean(long columnIndex, boolean value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex the column to search in.
     * @param value the value to search for.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstFloat(long columnIndex, float value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex the column to search in.
     * @param value the value to search for.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstDouble(long columnIndex, double value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex the column to search in.
     * @param value the value to search for.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstDate(long columnIndex, Date value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex the column to search in.
     * @param value the value to search for.
     * @return the row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstString(long columnIndex, String value);

    long lowerBoundLong(long columnIndex, long value);
    long upperBoundLong(long columnIndex, long value);


    TableView findAllLong(long columnIndex, long value);

    TableView findAllBoolean(long columnIndex, boolean value);

    TableView findAllFloat(long columnIndex, float value);

    TableView findAllDouble(long columnIndex, double value);

    TableView findAllDate(long columnIndex, Date value);

    TableView findAllString(long columnIndex, String value);

    String toJson();

    String toString();

    String toString(long maxRows);

    String rowToString(long rowIndex);

    TableQuery where();

    /**
     * Finds a row with in the table or view with the given index.
     *
     * @param rowIndex the index of the row.
     * @return the index if found, or -1 for not found.
     */
    long sourceRowIndex(long rowIndex);

    // Experimental:

    long count(long columnIndex, String value);

    /**
     * Report the current versioning counter for the table. The versioning counter is guaranteed to
     * change when the contents of the table changes after advance_read() or promote_to_write(), or
     * immediately after calls to methods which change the table.
     *
     * @return version_counter for the table.
     */
    long getVersion();

    void removeFirst();

    enum PivotType {
        COUNT(0),
        SUM(1),
        AVG(2),
        MIN(3),
        MAX(4);

        final int value; // Package protected, accessible from Table and TableView

        PivotType(int value) {
            this.value = value;
        }
    }

    Table pivot(long stringCol, long intCol, PivotType pivotType);

    /**
     * Syncs the TableView with the underlying table data. This is effectively the same as rerunning the query, so it
     * should not be called on TableViews created by an async query.
     *
     * This method gets automatically called when calling {@link Realm#refresh()} or when another thread updates
     * the Realm, but it will _not_ be called if the same thread commits a transaction.
     *
     * @return the version number for the updated TableView.
     */
    long syncIfNeeded();
}
