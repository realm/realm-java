package com.tightdb.lib;

import java.io.Serializable;
import java.util.Date;

import com.tightdb.ColumnType;
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
		spec.addColumn(ColumnType.ColumnTypeInt, name); // FIXME: use real
														// type when
														// available
	}

	protected void registerMixedColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeBinary, name); // FIXME: use
															// real type
															// when
															// available
	}

	protected void registerTableColumn(TableSpec spec, String name, AbstractTable<?, ?, ?> subtable) {
		TableSpec subspec = spec.addColumnTable("phoneNumbers");
		subtable.specifyStructure(subspec);
	}

	protected abstract void specifyStructure(TableSpec spec);

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
		table.insertLong((int) columnIndex, (int) rowIndex, value.getTime()); // FIXME:
																				// use
																				// real
																				// type
																				// when
																				// available
	}

	protected void insertMixed(long columnIndex, long rowIndex, Serializable value) {
		table.insertBinaryData((int) columnIndex, (int) rowIndex, TightDB.serialize(value)); // FIXME:
																								// use
																								// real
																								// type
																								// when
																								// available
	}

	protected void insertMixed(long columnIndex, long rowIndex, Object value) {
		if (value instanceof Serializable) {
			table.insertBinaryData((int) columnIndex, (int) rowIndex, TightDB.serialize((Serializable) value)); // FIXME:
																													// use
																													// real
																													// type
																													// when
																													// available
		} else {
			throw new RuntimeException("Cannot insert non-serializable value!");
		}
	}

	protected void insertTable(long columnIndex, long rowIndex) {
		table.insertSubTable(columnIndex, rowIndex);
	}

	protected void insertDone() {
		table.insertDone();
	}

	public void remove(long id) {
		table.removeRow((int) id);
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

	public Query query() {
		return AbstractQuery.createQuery(types.getQueryClass(), table, new TableQuery());
	}
}
