package com.tightdb.typed;

import java.nio.ByteBuffer;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

public class BinaryQueryColumn<Cursor, View, Query> extends AbstractColumn<ByteBuffer, Cursor, View, Query> {

	public BinaryQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
		super(types, tableOrView, query, index, name);
	}

}
