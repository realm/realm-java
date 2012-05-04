package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class TableRowsetColumn<Cursor, Query, Subtable> extends TableQueryColumn<Cursor, Query, Subtable> {

	public TableRowsetColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name,
			Class<Subtable> subtableClass) {
		super(types, table, query, index, name, subtableClass);
	}

	@Override
	public Subtable get() {
		return subtable;
	}

	@Override
	public void set(Subtable value) {
		throw new UnsupportedOperationException(); // FIXME: implement this
	}

}
