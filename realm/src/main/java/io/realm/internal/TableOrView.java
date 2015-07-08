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

/**
 * Specification of the common operations for the low-level table and view API.
 */
public interface TableOrView {

    int NO_MATCH = -1;

    void clear();

    /**
     * Returns the table.
     */
    Table getTable();

    void close();

    /**
     * Returns the number of entries of the table/view.
     */
    long size();

    /**
     * Checks whether the table/view is empty or not.
     *
     * @return true if empty, otherwise false.
     */
    boolean isEmpty();

    /**
     * Removes a particular row identified by the index from the table/view. [citation needed] The corresponding row of
     * the table also get deleted for which the table/view is part of.
     */
    void remove(long index);

    void removeLast();

    long getColumnCount();

    String getColumnName(long columnIndex);

    long getColumnIndex(String name);

    ColumnType getColumnType(long columnIndex);

    /**
     * Get the long value of a cell of the table/view identified by the columnIndex and rowIndex.
     */
    long getLong(long columnIndex, long rowIndex);

    /**
     * Get the boolean value of a cell of the table identified by the columnIndex and rowIndex.
     */
    boolean getBoolean(long columnIndex, long rowIndex);

    /**
     * Get the float value of a cell of the table identified by the columnIndex and rowIndex.
     */
    float getFloat(long columnIndex, long rowIndex);

    /**
     * Get the double value of a cell of the table identified by the columnIndex and rowIndex.
     */
    double getDouble(long columnIndex, long rowIndex);

    /**
     * Gets the string value of a cell identified by the columnIndex and rowIndex of the cell.
     */
    String getString(long columnIndex, long rowIndex);

    /**
     * Returns the Date value (java.util.Date) for a particular cell specified by the columnIndex and rowIndex of the
     * cell.
     */
    Date getDate(long columnIndex, long rowIndex);

    /**
     * Returns the binary data for a cell identified by the columnIndex and rowIndex of that cell.
     */
    //ByteBuffer getBinaryByteBuffer(long columnIndex, long rowIndex);

    byte[] getBinaryByteArray(long columnIndex, long rowIndex);

    Mixed getMixed(long columnIndex, long rowIndex);

    /**
     * Get the link index of a cell of the table/view identified by the columnIndex and rowIndex.
     */
    long getLink(long columnIndex, long rowIndex);

    ColumnType getMixedType(long columnIndex, long rowIndex);

    Table getSubtable(long columnIndex, long rowIndex);

    void clearSubtable(long columnIndex, long rowIndex);

    long getSubtableSize(long columnIndex, long rowIndex);

    /**
     * Sets the long value for a particular cell identified by columnIndex and rowIndex of that cell.
     */
    void setLong(long columnIndex, long rowIndex, long value);

    /**
     * Sets the boolean value of a cell identified by the columnIndex and the rowIndex of that cell.
     */
    void setBoolean(long columnIndex, long rowIndex, boolean value);

    /**
     * Sets the float value of a cell identified by the columnIndex and the rowIndex of that cell.
     */
    void setFloat(long columnIndex, long rowIndex, float value);

    /**
     * Sets the double value of a cell identified by the columnIndex and the rowIndex of that cell.
     */
    void setDouble(long columnIndex, long rowIndex, double value);

    /**
     * Sets the string value of a particular cell of the table/view identified by the columnIndex and the rowIndex of
     * this table/view.
     */
    void setString(long columnIndex, long rowIndex, String value);

    /**
     * Sets the binary value for a particular cell identified by the rowIndex and columnIndex of the cell.
     */
    //void setBinaryByteBuffer(long columnIndex, long rowIndex, ByteBuffer data);

    void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data);

    void setDate(long columnIndex, long rowIndex, Date date);

    void setMixed(long columnIndex, long rowIndex, Mixed data);

    boolean isNullLink(long columnIndex, long rowIndex);

    void nullifyLink(long columnIndex, long rowIndex);

    /**
     * Sets the link index for a particular cell identified by columnIndex and rowIndex of that cell.
     */
    void setLink(long columnIndex, long rowIndex, long value);

    //Increments all rows in the specified column with the provided value
    void adjust(long columnIndex, long value);

    long sumLong(long columnIndex);

    long maximumLong(long columnIndex);

    long minimumLong(long columnIndex);

    double averageLong(long columnIndex);

    double sumFloat(long columnIndex);

    float maximumFloat(long columnIndex);

    float minimumFloat(long columnIndex);

    double averageFloat(long columnIndex);

    double sumDouble(long columnIndex);

    double maximumDouble(long columnIndex);

    double minimumDouble(long columnIndex);

    double averageDouble(long columnIndex);

    Date maximumDate(long columnIndex);

    Date minimumDate(long columnIndex);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex Column to search in.
     * @param value Value to search for.
     * @return Row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstLong(long columnIndex, long value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex Column to search in.
     * @param value Value to search for.
     * @return Row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstBoolean(long columnIndex, boolean value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex Column to search in.
     * @param value Value to search for.
     * @return Row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstFloat(long columnIndex, float value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex Column to search in.
     * @param value Value to search for.
     * @return Row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstDouble(long columnIndex, double value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex Column to search in.
     * @param value Value to search for.
     * @return Row index for the first match found or {@link #NO_MATCH}.
     */
    long findFirstDate(long columnIndex, Date value);

    /**
     * Searches for first occurrence of a value. Beware that the order in the column is undefined.
     *
     * @param columnIndex Column to search in.
     * @param value Value to search for.
     * @return Row index for the first match found or {@link #NO_MATCH}.
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

    // Experimental:

    long count(long columnIndex, String value);

    enum PivotType {
        COUNT(0),
        SUM(1),
        AVG(2),
        MIN(3),
        MAX(4);

        final int value; // Package protected, accessible from Table and TableView

        private PivotType(int value) {
            this.value = value;
        }
    }

    Table pivot(long stringCol, long intCol, PivotType pivotType);

    /**
     * Syncs the tableview with the underlying table data. It is not required to call this explicitly, all other API
     * methods will automatically sync the view as well.
     *
     * @return Version number for the updated tableview.
     */
    long sync();
}
