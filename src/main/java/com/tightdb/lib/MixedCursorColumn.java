package com.tightdb.lib;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.ColumnType;
import com.tightdb.Mixed;
import com.tightdb.TableBase;
import com.tightdb.TableSpec;

public class MixedCursorColumn<Cursor, View, Query> extends AbstractColumn<Mixed, Cursor, View, Query> {

	public MixedCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, cursor, index, name);
	}

	@Override
	public Mixed get() {
		return cursor.rowset.getMixed(columnIndex, cursor.getPosition());
	}

	@Override
	public void set(Mixed value) {
		cursor.rowset.setMixed(columnIndex, cursor.getPosition(), value);
	}

	public void set(String value) {
		cursor.rowset.setMixed(columnIndex, cursor.getPosition(), Mixed.mixedValue(value));
	}

	public void set(boolean value) {
		cursor.rowset.setMixed(columnIndex, cursor.getPosition(), Mixed.mixedValue(value));
	}

	public void set(long value) {
		cursor.rowset.setMixed(columnIndex, cursor.getPosition(), Mixed.mixedValue(value));
	}

	public void set(Date value) {
		cursor.rowset.setMixed(columnIndex, cursor.getPosition(), Mixed.mixedValue(value));
	}

	public void set(ByteBuffer value) {
		cursor.rowset.setMixed(columnIndex, cursor.getPosition(), Mixed.mixedValue(value));
	}

	public void set(byte[] value) {
		cursor.rowset.setMixed(columnIndex, cursor.getPosition(), Mixed.mixedValue(value));
	}

	public <Tbl> Tbl createSubtable(Class<Tbl> tableClass) {
		set(new Mixed(ColumnType.ColumnTypeTable));
		TableBase subtable = cursor.rowset.getSubTable(columnIndex, cursor.getPosition());
		return AbstractSubtable.createSubtable(tableClass, subtable);
	}

	public <Tbl extends AbstractTable<?, ?, ?>> Tbl getSubtable(Class<Tbl> tableClass) {
		if (!isSubtable(tableClass)) {
			throw new IllegalArgumentException("Wrong subtable type!");
		}

		TableBase subtableBase = cursor.rowset.getSubTable(columnIndex, cursor.getPosition());
		return AbstractSubtable.createSubtable(tableClass, subtableBase);
	}

	/**
	 * Check if the subtable (this cursor points at) is the same as the tableClass provided as parameter
	 */
	public <Tbl extends AbstractTable<?, ?, ?>> boolean isSubtable(Class<Tbl> tableClass) {
		if (get().getType() != ColumnType.ColumnTypeTable) {
			throw new IllegalArgumentException("The mixed value doesn't contain a sub-table!");
		}

		TableBase subtableBase = cursor.rowset.getSubTable(columnIndex, cursor.getPosition());
		TableSpec spec = subtableBase.getTableSpec();
		
		// Build table schema
		final TableSpec spec2 = new TableSpec();
		AbstractTable.specifyTableStructure(tableClass, spec2);
		
		return spec.equals(spec2);
	}

}
