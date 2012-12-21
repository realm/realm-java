package com.tightdb.typed;

import com.tightdb.Mixed;
import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class MixedQueryColumn<Cursor, View, Query> extends AbstractColumn<Mixed, Cursor, View, Query> {

	public MixedQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

}
