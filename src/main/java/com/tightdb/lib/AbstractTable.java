package com.tightdb.lib;

import java.io.Serializable;
import java.util.Date;

import com.tightdb.ColumnType;
import com.tightdb.TableBase;

public abstract class AbstractTable<Cursor, View> extends AbstractRowset<Cursor, View> implements Iterable<Cursor> {

	protected final TableBase table = new TableBase();
	protected final Class<Cursor> cursorClass;
	protected final Class<View> viewClass;

	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public int size() {
		return table.getCount();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	public AbstractTable(Class<Cursor> cursorClass, Class<View> viewClass) {
		this.cursorClass = cursorClass;
		this.viewClass = viewClass;
	}

	protected void registerLongColumn(String name) {
		table.registerColumn(ColumnType.ColumnTypeInt, name);
	}

	protected void registerStringColumn(String name) {
		table.registerColumn(ColumnType.ColumnTypeString, name);
	}

	protected void registerBooleanColumn(String name) {
		table.registerColumn(ColumnType.ColumnTypeBool, name);
	}

	protected void registerBinaryColumn(String name) {
		table.registerColumn(ColumnType.ColumnTypeBinary, name);
	}

	protected void registerDateColumn(String name) {
		table.registerColumn(ColumnType.ColumnTypeInt, name);  // FIXME: use real type when available
	}

	protected void registerMixedColumn(String name) {
		table.registerColumn(ColumnType.ColumnTypeBinary, name); // FIXME: use real type when available
	}

	protected void insertLong(long columnIndex, long rowIndex, long value) {
		table.insertLong((int) columnIndex, (int) rowIndex, value);
	}

	protected void insertString(long columnIndex, long rowIndex, String value) {
		table.insertString((int) columnIndex, (int) rowIndex, value);
	}

	protected void insertBoolean(long columnIndex, long rowIndex, boolean value) {
		table.insertBoolean((int) columnIndex, (int) rowIndex, value);
	}

	protected void insertBinary(long columnIndex, long rowIndex, byte[] value) {
		table.insertBinaryData((int) columnIndex, (int) rowIndex, value);
	}

	protected void insertDate(long columnIndex, long rowIndex, Date value) {
		table.insertLong((int) columnIndex, (int) rowIndex, value.getTime());  // FIXME: use real type when available
	}

	protected void insertMixed(long columnIndex, long rowIndex, Serializable value) {
		table.insertBinaryData((int) columnIndex, (int) rowIndex, TDBUtils.serialize(value));  // FIXME: use real type when available
	}

	protected void insertMixed(long columnIndex, long rowIndex, Object value) {
		if (value instanceof Serializable) {
			table.insertBinaryData((int) columnIndex, (int) rowIndex, TDBUtils.serialize((Serializable) value));  // FIXME: use real type when available
		} else {
			throw new RuntimeException("Cannot insert non-serializable value!");
		}
	}

	protected void insertDone() {
		table.insertDone();
	}

	public View range(long from, long to) {
		throw new UnsupportedOperationException();
	}

	public Cursor at(long position) {
		return cursor(position);
	}

	public Cursor first() {
		return cursor(0);
	}

	public Cursor last() {
		return cursor(size() - 1);
	}

	public void remove(long id) {
		table.removeRow((int) id);
	}

	@Override
	public Cursor remove(int index) {
		table.removeRow(index);
		return null;
	}

	@Override
	public void clear() {
		table.clear();
	}

	protected Cursor cursor(long position) {
		Cursor cursor;

		try {
			cursor = cursorClass.getDeclaredConstructor(TableBase.class, long.class).newInstance(table, position);
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate a cursor!", e);
		}

		return cursor;
	}

	protected RuntimeException addRowException(Exception e) throws RuntimeException {
		return new RuntimeException("Error occured while adding a new row!", e);
	}

	protected RuntimeException insertRowException(Exception e) throws RuntimeException {
		return new RuntimeException("Error occured while inserting a new row!", e);
	}

}
