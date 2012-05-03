package com.tightdb.lib;

import com.tightdb.TableBase;

public class BooleanQueryColumn<Cursor, Query> extends AbstractColumn<Boolean, Cursor, Query> {

	public BooleanQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public BooleanQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		super(types, table, index, name);
	}

}
