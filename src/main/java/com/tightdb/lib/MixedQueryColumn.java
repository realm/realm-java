package com.tightdb.lib;

import java.io.Serializable;

import com.tightdb.TableQuery;

public class MixedQueryColumn<Cursor, Query> extends AbstractColumn<Serializable, Cursor, Query> {

	public MixedQueryColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

}
