package com.tightdb.lib;

import com.tightdb.TableViewBase;

public abstract class AbstractView<Cursor, View, Query> extends AbstractTableOrView<Cursor, View, Query> {

	protected final TableViewBase viewBase;

	public AbstractView(EntityTypes<?, View, Cursor, Query> types, TableViewBase viewBase) {
		super(types, viewBase);
		this.viewBase = viewBase;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return types.getViewClass().getSimpleName() + " {" + size() + " records}";
	}

	public static <V> V createView(Class<V> viewClass, TableViewBase viewBase) {
		try {
			return viewClass.getConstructor(TableViewBase.class).newInstance(viewBase);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a view!", e);
		}
	}
}
