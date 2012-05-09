package com.tightdb.lib;

import java.util.Date;

import com.tightdb.Mixed;
import com.tightdb.SubTableBase;

public interface IRowsetBase {

	void clear();

	/**
	 * Returns the number of entries of the table/view.
	 * 
	 * @return
	 */
	int getCount();

	/**
	 * Removes a particular row identified by the index from the table/view.
	 * [citation needed] The corresponding row of the table also get deleted for
	 * which the table/view is part of.
	 * 
	 * @param index
	 */
	void removeRow(int index);

	/**
	 * Get the long value of a cell of the table/view identified by the
	 * columnIndex and rowIndex.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @return
	 */
	long getLong(int columnIndex, int rowIndex);

	/**
	 * Get the boolean value of a cell of the table identified by the
	 * columnIndex and rowIndex.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @return
	 */
	boolean getBoolean(int columnIndex, int rowIndex);

	/**
	 * Gets the string value of a cell identified by the columnIndex and
	 * rowIndex of the cell.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @return
	 */
	String getString(int columnIndex, int rowIndex);

	/**
	 * Returns the Date value (java.util.Date) for a particular cell specified
	 * by the columnIndex and rowIndex of the cell.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @return
	 */
	Date getDate(int columnIndex, int rowIndex);

	/**
	 * Returns the binary byte[] data for a cell identified by the columnIndex
	 * and rowIndex of that cell.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @return
	 */
	byte[] getBinaryData(int columnIndex, int rowIndex);

	Mixed getMixed(int columnIndex, int rowIndex, Mixed value);

	SubTableBase getSubTable(int columnIndex, int rowIndex);

	/**
	 * Sets the long value for a particular cell identified by columnIndex and
	 * rowIndex of that cell.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 */
	void setLong(int columnIndex, int rowIndex, long value);

	/**
	 * Sets the boolean value of a cell identified by the columnIndex and the
	 * rowIndex of that cell.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 */
	void setBoolean(int columnIndex, int rowIndex, boolean value);

	/**
	 * Sets the string value of a particular cell of the table/view identified
	 * by the columnIndex and the rowIndex of this table/view
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @param value
	 */
	void setString(int columnIndex, int rowIndex, String value);

	/**
	 * Sets the binary value byte[] for a particular cell identified by the
	 * rowIndex and columnIndex of the cell.
	 * 
	 * @param columnIndex
	 * @param rowIndex
	 * @param data
	 */
	void setBinaryData(int columnIndex, int rowIndex, byte[] data);

	void setMixed(int columnIndex, int rowIndex, Mixed data);

}
