package com.tightdb.lib;

import com.tightdb.TableQuery;

public class LongTableOrViewColumn<Cursor, View, Query> extends
		LongQueryColumn<Cursor, View, Query> implements TableOrViewColumn<Long> {

	public LongTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types,
			TableOrViewBase tableOrView, int index, String name) {
		this(types, tableOrView, null, index, name);
	}

	public LongTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types,
			TableOrViewBase tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

	@Override
	public long sum() {
		return tableOrView.sum(columnIndex);
	}

	@Override
	public long maximum() {
		return tableOrView.maximum(columnIndex);
	}

	@Override
	public long minimum() {
		return tableOrView.minimum(columnIndex);
	}

	@Override
	public double average() {
		return tableOrView.average(columnIndex);
	}

	/*
		public void setIndex() {
			tableOrView.setIndex(columnIndex);
		}
		
		public boolean hasIndex() {
			return tableOrView.hasIndex(columnIndex);
		}
	*/
	
	@Override
	public Long[] getAll() {
		long count = tableOrView.size();
		Long[] values = new Long[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = tableOrView.getLong(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Long value) {
		long count = tableOrView.size();
		for (int i = 0; i < count; i++) {
			tableOrView.setLong(columnIndex, i, value);
		}
	}

	public void setAll(long value) {
		setAll(new Long(value));
	}

	public void addLong(long value) {
		tableOrView.addLong(columnIndex, value);
	}

	public Cursor findFirst(long value) {
		return cursor(tableOrView.findFirstLong(columnIndex, value));
	}

	public View findAll(long value) {
		return view(tableOrView.findAllLong(columnIndex, value));
	}

}
