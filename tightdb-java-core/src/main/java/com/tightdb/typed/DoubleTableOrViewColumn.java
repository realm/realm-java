package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class DoubleTableOrViewColumn<Cursor, View, Query> extends DoubleQueryColumn<Cursor, View, Query> implements
		TableOrViewColumn<Double> {

	public DoubleTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index,
			String name) {
		this(types, tableOrView, null, index, name);
	}

	public DoubleTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView,
			TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

	@Override
	public double sum() {
		return tableOrView.sumDouble(columnIndex);
	}

	@Override
	public double maximum() {
		return tableOrView.maximumDouble(columnIndex);
	}

	@Override
	public double minimum() {
		return tableOrView.minimumDouble(columnIndex);
	}

	@Override
	public double average() {
		return tableOrView.averageDouble(columnIndex);
	}

	/*
	 * public void setIndex() { tableOrView.setIndex(columnIndex); }
	 * 
	 * public boolean hasIndex() { return tableOrView.hasIndex(columnIndex); }
	 */

	@Override
	public Double[] getAll() {
		long count = tableOrView.size();
		Double[] values = new Double[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = tableOrView.getDouble(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Double value) {
		long count = tableOrView.size();
		for (int i = 0; i < count; i++) {
			tableOrView.setDouble(columnIndex, i, value);
		}
	}

	public void setAll(double value) {
		setAll(new Double(value));
	}

	// public void addDouble(double value) {
	// tableOrView.addDouble(columnIndex, value);
	// }

	public Cursor findFirst(long value) {
		return cursor(tableOrView.findFirstDouble(columnIndex, value));
	}

	public View findAll(long value) {
		return view(tableOrView.findAllDouble(columnIndex, value));
	}

}
