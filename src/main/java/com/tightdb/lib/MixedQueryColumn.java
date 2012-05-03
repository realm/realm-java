package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;

public class MixedQueryColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public MixedQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, int index, String name) {
		super(types, table, index, name);
	}

}
