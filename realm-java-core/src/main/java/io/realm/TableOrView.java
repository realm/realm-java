package io.realm;

import java.util.Date;

/**
 * Specification of the common operations for the low-level table and view API.
 */
public interface TableOrView {

    void clear();
    
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
     * @return true if empty, otherwise false.
     */
    boolean isEmpty();

    /**
     * Removes a particular row identified by the index from the table/view.
     * [citation needed] The corresponding row of the table also get deleted for
     * which the table/view is part of.
     *
     * @param index
     */
    void remove(long index);

    void removeLast();

    long getColumnCount();

    String getColumnName(long columnIndex);

    long getColumnIndex(String name);

    ColumnType getColumnType(long columnIndex);

    /**
     * Get the long value of a cell of the table/view identified by the
     * columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    long getLong(long columnIndex, long rowIndex);

    /**
     * Get the boolean value of a cell of the table identified by the
     * columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    boolean getBoolean(long columnIndex, long rowIndex);

    /**
     * Get the float value of a cell of the table identified by the
     * columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    float getFloat(long columnIndex, long rowIndex);

    /**
     * Get the double value of a cell of the table identified by the
     * columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    double getDouble(long columnIndex, long rowIndex);

    /**
     * Gets the string value of a cell identified by the columnIndex and
     * rowIndex of the cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    String getString(long columnIndex, long rowIndex);

    /**
     * Returns the Date value (java.util.Date) for a particular cell specified
     * by the columnIndex and rowIndex of the cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    Date getDate(long columnIndex, long rowIndex);

    /**
     * Returns the binary data for a cell identified by the columnIndex
     * and rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    //ByteBuffer getBinaryByteBuffer(long columnIndex, long rowIndex);

    byte[] getBinaryByteArray(long columnIndex, long rowIndex);

    Mixed getMixed(long columnIndex, long rowIndex);

    /**
     * Get the link index of a cell of the table/view identified by the
     * columnIndex and rowIndex.
     *
     * @param columnIndex
     * @param rowIndex
     * @return
     */
    long getLink(long columnIndex, long rowIndex);

    ColumnType getMixedType(long columnIndex, long rowIndex);

    Table getSubtable(long columnIndex, long rowIndex);

    void clearSubtable(long columnIndex, long rowIndex);

    long getSubtableSize(long columnIndex, long rowIndex);

    /**
     * Sets the long value for a particular cell identified by columnIndex and
     * rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setLong(long columnIndex, long rowIndex, long value);

    /**
     * Sets the boolean value of a cell identified by the columnIndex and the
     * rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setBoolean(long columnIndex, long rowIndex, boolean value);

    /**
     * Sets the float value of a cell identified by the columnIndex and the
     * rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setFloat(long columnIndex, long rowIndex, float value);

    /**
     * Sets the double value of a cell identified by the columnIndex and the
     * rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setDouble(long columnIndex, long rowIndex, double value);

    /**
     * Sets the string value of a particular cell of the table/view identified
     * by the columnIndex and the rowIndex of this table/view
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
     */
    void setString(long columnIndex, long rowIndex, String value);

    /**
     * Sets the binary value for a particular cell identified by the
     * rowIndex and columnIndex of the cell.
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
     * Sets the link index for a particular cell identified by columnIndex and
     * rowIndex of that cell.
     *
     * @param columnIndex
     * @param rowIndex
     * @param value
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


    long findFirstLong(long columnIndex, long value);

    long findFirstBoolean(long columnIndex, boolean value);

    long findFirstFloat(long columnIndex, float value);

    long findFirstDouble(long columnIndex, double value);

    long findFirstDate(long columnIndex, Date value);

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

    long lookup(String value);

    long count(long columnIndex, String value);
    
    public enum PivotType {
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
    
    public Table pivot(long stringCol, long intCol, PivotType pivotType);

}
