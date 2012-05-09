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
		throw new UnsupportedOperationException("Not implemented yet!");
	}

}
