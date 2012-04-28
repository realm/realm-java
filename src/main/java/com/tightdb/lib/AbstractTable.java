package com.tightdb.lib;

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
