package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;

public class MixedQueryColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, AbstractCursor<Cursor> cursor, int index, String name) {
		super(types, table, cursor, index, name);
	}

	public MixedQueryColumn(EntityTypes<?, ?, Cursor, Query> types, TableBase table, TableQuery query, int index, String name) {
		super(types, table, query, index, name);
	}

}
