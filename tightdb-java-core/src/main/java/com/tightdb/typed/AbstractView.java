package com.tightdb.typed;

import com.tightdb.TableView;

public abstract class AbstractView<Cursor, View, Query> extends AbstractTableOrView<Cursor, View, Query> {

	protected final TableView viewBase;

	public AbstractView(EntityTypes<?, View, Cursor, Query> types, TableView viewBase) {
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

	protected static <V> V createView(Class<V> viewClass, TableView viewBase) {
		try {
			return viewClass.getConstructor(TableView.class).newInstance(viewBase);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a view!", e);
		}
	}
}
