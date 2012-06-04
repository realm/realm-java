package com.tightdb.lib;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.ColumnType;
import com.tightdb.Group;
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
		this(types, new TableBase());
	}

	public AbstractTable(EntityTypes<?, View, Cursor, Query> types, Group group) {
		this(types, group.getTable(types.getTableClass().getCanonicalName()));
	}

	protected AbstractTable(EntityTypes<?, View, Cursor, Query> types, TableBase table) {
		super(types, table);
		this.table = table;
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
		if (table != null) {
			final TableSpec spec = new TableSpec();
			specifyStructure(spec);
			table.updateFromSpec(spec);
		}
	}

	protected void addLongColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeInt, name);
	}

	protected void addStringColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeString, name);
	}

	protected void addBooleanColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeBool, name);
	}

	protected void addBinaryColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeBinary, name);
	}

	protected void addDateColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeDate, name);
	}

	protected void addMixedColumn(TableSpec spec, String name) {
		spec.addColumn(ColumnType.ColumnTypeMixed, name);
	}

	protected void addTableColumn(TableSpec spec, String name, AbstractTable<?, ?, ?> subtable) {
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

	public void remove(long rowIndex) {
		table.remove(rowIndex);
	}

	public void setIndex(long columnIndex) {
		table.setIndex(columnIndex);
	}

	public boolean hasIndex(long columnIndex) {
		return table.hasIndex(columnIndex);
	}
	
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
