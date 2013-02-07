package com.tightdb.typed;

import com.tightdb.Table;

public abstract class AbstractSubtable<Cursor, View, Query> extends AbstractTable<Cursor, View, Query> {

	public AbstractSubtable(EntityTypes<?, View, Cursor, Query> types, Table subtable) {
		super(types, subtable);
	}

	protected static <S> S createSubtable(Class<S> subtableClass, Table subtableBase) {
		try {
			S subtable = subtableClass.getConstructor(Table.class).newInstance(subtableBase);
			return subtable;
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

}
