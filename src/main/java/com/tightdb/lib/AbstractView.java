package com.tightdb.lib;

import com.tightdb.TableViewBase;

public abstract class AbstractView<Cursor, View> extends AbstractRowset<Cursor, View> {

	protected final TableViewBase viewBase;

	public AbstractView(EntityTypes<?, View, Cursor, ?> types, TableViewBase viewBase) {
		super(types, viewBase);
		this.viewBase = viewBase;
	}

	@Override
	public int size() {
		return viewBase.getCount();
	}

	@Override
	public void clear() {
		viewBase.clear();
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return types.getViewClass().getSimpleName() + " {" + size() + " records}";
	}
}
