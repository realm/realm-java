package com.tightdb.lib;

import com.tightdb.Mixed;
import com.tightdb.TableQuery;

public class MixedQueryColumn<Cursor, Query> extends AbstractColumn<Mixed, Cursor, Query> {

	public MixedQueryColumn(EntityTypes<?, ?, Cursor, Query> types, IRowsetBase rowset, TableQuery query, int index, String name) {
		super(types, rowset, query, index, name);
	}

}
