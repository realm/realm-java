package com.tightdb.typed;

import com.tightdb.Mixed;
import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class MixedTableOrViewColumn<Cursor, View, Query> extends MixedQueryColumn<Cursor, View, Query> implements TableOrViewColumn<Mixed> {

	public MixedTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index, String name) {
		this(types, tableOrView, null, index, name);
	}

	public MixedTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

	@Override
	public Mixed[] getAll() {
		long count = tableOrView.size();
		Mixed[] values = new Mixed[(int) count];
		for (int i = 0; i < count; i++) {
			values[i] = tableOrView.getMixed(columnIndex, i);
		}
		return values;
	}

	@Override
	public void setAll(Mixed value) {
		long count = tableOrView.size();
		for (int i = 0; i < count; i++) {
			tableOrView.setMixed(columnIndex, i, value);
		}
	}

}
