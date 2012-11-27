package com.tightdb.lib;

import com.tightdb.Mixed;
import com.tightdb.TableOrViewBase;
import com.tightdb.TableQuery;

public class MixedQueryColumn<Cursor, View, Query> extends AbstractColumn<Mixed, Cursor, View, Query> {

	public MixedQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrViewBase tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

}
