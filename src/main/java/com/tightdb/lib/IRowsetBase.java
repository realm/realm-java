package com.tightdb.lib;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.Mixed;
import com.tightdb.TableBase;
import com.tightdb.TableViewBase;

public interface IRowsetBase {

	void clear();

	/**
	 * Returns the number of entries of the table/view.
	 * 
	 * @return
	 */
	long size();

	/**
	 * Removes a particular row identified by the index from the table/view.
	 * [citation needed] The corresponding row of the table also get deleted for
	 * which the table/view is part of.
	 * 
	 * @param index
	 */
	void remove(long index);

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
	ByteBuffer getBinary(long columnIndex, long rowIndex);

	Mixed getMixed(long columnIndex, long rowIndex);

	TableBase getSubTable(long columnIndex, long rowIndex);

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
	void setBinary(long columnIndex, long rowIndex, ByteBuffer data);

	void setDate(long columnIndex, long rowIndex, Date date);
	
	void setMixed(long columnIndex, long rowIndex, Mixed data);

	long sum(long columnIndex);
	
	long maximum(long columnIndex);
	
	long minimum(long columnIndex);

	long findFirstLong(long columnIndex, long value);
	
	long findFirstString(long columnIndex, String value);

	TableViewBase findAllLong(long columnIndex, long value);
	
	TableViewBase findAllString(long columnIndex, String value);
	
}
