package com.tightdb.lib;

import com.tightdb.TableQuery;

public class BooleanTableOrViewColumn<Cursor, View, Query> extends BooleanQueryColumn<Cursor, View, Query> implements TableOrViewColumn<Boolean> {

	public BooleanTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrViewBase tableOrView, int index, String name) {
		this(types, tableOrView, null, index, name);
	}

	public BooleanTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrViewBase tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

	@Override
	public Boolean[] getAll() {
		long count = tableOrView.size();
		Boolean[] values = new Boolean[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = tableOrView.getBoolean(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Boolean value) {
		long count = tableOrView.size();
		for (int i = 0; i < count; i++) {
			tableOrView.setBoolean(columnIndex, i, value);
		}
	}

	public Cursor findFirst(boolean value) {
		return cursor(tableOrView.findFirstBoolean(columnIndex, value));
	}
	
	public View findAll(boolean value) {
		return view(tableOrView.findAllBoolean(columnIndex, value));
	}

}
