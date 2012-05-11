package com.tightdb.lib;

import java.lang.reflect.Array;

import com.tightdb.SubTableBase;
import com.tightdb.TableQuery;

public class TableRowsetColumn<Cursor, Query, Subtable> extends TableQueryColumn<Cursor, Query, Subtable> {

	public TableRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, int index, String name, Class<Subtable> subtableClass) {
		this(types, rowset, null, index, name, subtableClass);
	}

	public TableRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, rowset, query, index, name, subtableClass);
	}

	@SuppressWarnings("unchecked")
	public Subtable[] getAll() {
		int count = rowset.getCount();
		Subtable[] values = (Subtable[]) Array.newInstance(subtableClass, count);
		for (int i = 0; i < count; i++) {
			SubTableBase subTableBase = rowset.getSubTable(columnIndex, i);
			values[i] = AbstractSubtable.createSubtable(subtableClass, subTableBase);
		}
		return values;
	}

}
