package com.tightdb.typed;

import java.lang.reflect.Array;

import com.tightdb.Table;
import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class TableTableOrViewColumn<Cursor, View, Query, Subtable> extends TableQueryColumn<Cursor, View, Query, Subtable> {

	public TableTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index, String name, Class<Subtable> subtableClass) {
		this(types, tableOrView, null, index, name, subtableClass);
	}

	public TableTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, tableOrView, query, index, name, subtableClass);
	}

	@SuppressWarnings("unchecked")
	public Subtable[] getAll() {
		long count = tableOrView.size();
		Subtable[] values = (Subtable[]) Array.newInstance(subtableClass, (int) count);
		for (int i = 0; i < count; i++) {
			Table subTableBase = tableOrView.getSubTable(columnIndex, i);
			values[i] = AbstractSubtable.createSubtable(subtableClass, subTableBase);
		}
		return values;
	}

}
