package com.tightdb;

import java.util.Date;

public class TableViewBase {
	public TableViewBase(TableBase table) {
		this.table = table;
		this.tableView = null;
		this.nativePtr = createNativeTableView(this.table);
	}

	public TableViewBase(TableBase table, long nativePtr) {
		this.table = table;
		this.tableView = null;
		this.nativePtr = nativePtr;
	}

	public TableViewBase(TableViewBase tableView, long nativePtr) {
		this.table = null;
		this.tableView = tableView;
		this.nativePtr = nativePtr;
	}

	public int getCount() {
		return nativeGetCount();
	}

	protected native int nativeGetCount();

	public boolean isEmpty() {
		return getCount() != 0;
	}

	public void removeRow(int index) {
		nativeRemoveRow(index);
	}

	protected native void nativeRemoveRow(int index);

	public long getLong(int columnIndex, int rowIndex) {
		return nativeGetLong(columnIndex, rowIndex);
	}

	protected native long nativeGetLong(int columnIndex, int rowIndex);

	public boolean getBoolean(int columnIndex, int rowIndex) {
		return nativeGetBoolean(columnIndex, rowIndex);
	}

	protected native boolean nativeGetBoolean(int columnIndex, int rowIndex);

	public String getString(int columnIndex, int rowIndex) {
		return nativeGetString(columnIndex, rowIndex);
	}

	protected native String nativeGetString(int columnInde, int rowIndex);

	public Date getDate(int columnIndex, int rowIndex) {
		throw new UnsupportedOperationException();
	}

	public byte[] getBinaryData(int columnIndex, int rowIndex) {
		return nativeGetBinaryData(columnIndex, rowIndex);
	}

	protected native byte[] nativeGetBinaryData(int columnIndex, int rowIndex);

	public void setLong(int columnIndex, int rowIndex, long value) {
		nativeSetLong(columnIndex, rowIndex, value);
	}

	protected native void nativeSetLong(int columnIndex, int rowIndex, long value);

	public void setBoolean(int columnIndex, int rowIndex, boolean value) {
		nativeSetBoolean(columnIndex, rowIndex, value);
	}

	protected native void nativeSetBoolean(int columnIndex, int rowIndex, boolean value);

	public void setString(int columnIndex, int rowIndex, String value) {
		nativeSetString(columnIndex, rowIndex, value);
	}

	protected native void nativeSetString(int columnIndex, int rowIndex, String value);

	public void setBinaryData(int columnIndex, int rowIndex, byte[] data) {
		nativeSetBinaryData(columnIndex, rowIndex, data);
	}

	protected native void nativeSetBinaryData(int columnIndex, int rowIndex, byte[] data);

	public long sum(int columnIndex) {
		return nativeSum(columnIndex);
	}

	protected native long nativeSum(int columnIndex);

	public long max(int columnIndex) {
		return nativeMax(columnIndex);
	}

	protected native long nativeMax(int columnIndex);

	public long min(int columnIndex) {
		return nativeMin(columnIndex);
	}

	protected native long nativeMin(int columnIndex);

	protected native long createNativeTableView(TableBase table);

	protected TableBase getRootTable() {
		if (table != null)
			return table;
		return tableView.getRootTable();
	}

	protected long nativePtr;
	protected TableBase table;
	protected TableViewBase tableView;
}
