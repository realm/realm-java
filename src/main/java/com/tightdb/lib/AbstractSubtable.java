package com.tightdb.lib;

import com.tightdb.SubTableBase;

public abstract class AbstractSubtable<Cursor, View, Query> extends AbstractTable<Cursor, View, Query> {

	protected final SubTableBase subtable;

	public AbstractSubtable(EntityTypes<?, View, Cursor, Query> types, SubTableBase subtable) {
		super(types);
		this.subtable = subtable;
	}

	public static <S> S createSubtable(Class<S> subtableClass, SubTableBase subTableBase) {
		try {
			S subtable = subtableClass.getConstructor(SubTableBase.class).newInstance(subTableBase);
			return subtable;
		} catch (Exception e) {
			throw new RuntimeException("Cannot create subtable instance!", e);
		}
	}

}
