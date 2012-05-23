package com.tightdb.lib;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.ColumnType;
import com.tightdb.Mixed;
import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.TableSpec;

public abstract class AbstractTable<Cursor, View, Query> extends AbstractRowset<Cursor, View, Query> {

	static {
		TightDB.loadLibrary();
	}

	protected final TableBase table;

	public AbstractTable(EntityTypes<?, View, Cursor, Query> types) {
		super(types, new TableBase());
		table = (TableBase) rowset;
		defineTableStructure();
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public long size() {
		return table.size();
	}

	private void defineTableStructure() {
		final TableSpec spec = new TableSpec();
		specifyStructure(spec);
		table.updateFromSpec(spec);
	}

	protected void registerLongColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeInt, name);
	}

	protected void registerStringColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeString, name);
	}

	protected void registerBooleanColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeBool, name);
	}

	protected void registerBinaryColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeBinary, name);
	}

	protected void registerDateColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeDate, name);
	}

	protected void registerMixedColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeMixed, name);
	}

	protected void registerTableColumn(TableSpec spec, String name, AbstractTable<?, ?, ?> subtable) {
		TableSpec subspec = spec.addSubtableColumn(name);
		subtable.specifyStructure(subspec);
	}

	protected abstract void specifyStructure(TableSpec spec);

	protected void insertLong(long columnIndex, long rowIndex, long value) {
		table.insertLong(columnIndex, rowIndex, value);
	}

	protected void insertString(long columnIndex, long rowIndex, String value) {
		table.insertString(columnIndex, rowIndex, value);
	}

	protected void insertBoolean(long columnIndex, long rowIndex, boolean value) {
		table.insertBoolean(columnIndex, rowIndex, value);
	}

	protected void insertBinary(long columnIndex, long rowIndex, byte[] value) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(value.length);
		buffer.put(value);
		
		table.insertBinary(columnIndex, rowIndex, buffer);
		//table.insertBinary(columnIndex, rowIndex, ByteBuffer.wrap(value));
	}

	protected void insertDate(long columnIndex, long rowIndex, Date value) {
		table.insertDate(columnIndex, rowIndex, value);
	}

	protected void insertMixed(long columnIndex, long rowIndex, Object value) {
		Mixed mixed = TightDB.mixedValue(value);
		table.insertMixed(columnIndex, rowIndex, mixed);
	}

	protected void insertTable(long columnIndex, long rowIndex) {
		table.insertSubTable(columnIndex, rowIndex);
	}

	protected void insertDone() {
		table.insertDone();
	}

	public void remove(long id) {
		table.remove(id);
	}

	// @Override
	// public Cursor remove(int index) {
	// table.removeRow(index);
	// return null;
	// }

	@Override
	public void clear() {
		table.clear();
	}

	protected RuntimeException addRowException(Exception e) throws RuntimeException {
		return new RuntimeException("Error occured while adding a new row!", e);
	}

	protected RuntimeException insertRowException(Exception e) throws RuntimeException {
		return new RuntimeException("Error occured while inserting a new row!", e);
	}

	public Query where() {
		return AbstractQuery.createQuery(types.getQueryClass(), table, new TableQuery());
	}
}
