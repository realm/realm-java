package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Type of the fields that represent a mixed column in the generated XyzTable
 * class for the Xyz entity.
 */
public class MixedTableColumn<Cursor, View, Query> extends MixedTableOrViewColumn<Cursor, View, Query> {

	public MixedTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, int index, String name) {
		super(types, table, index, name);
	}

	public MixedTableColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView table, TableQuery query, int index,
			String name) {
		super(types, table, query, index, name);
	}

}
