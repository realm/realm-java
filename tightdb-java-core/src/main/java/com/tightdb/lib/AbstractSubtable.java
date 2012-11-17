package com.tightdb.lib;

import com.tightdb.TableBase;

public abstract class AbstractSubtable<Cursor, View, Query> extends AbstractTable<Cursor, View, Query> {

	public AbstractSubtable(EntityTypes<?, View, Cursor, Query> types, TableBase subtable) {
		super(types, subtable);
	}

	public static <S> S createSubtable(Class<S> subtableClass, TableBase subtableBase) {
		try {
			S subtable = subtableClass.getConstructor(TableBase.class).newInstance(subtableBase);
			return subtable;
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

}
