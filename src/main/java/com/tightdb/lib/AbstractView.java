package com.tightdb.lib;

import com.tightdb.TableViewBase;

public abstract class AbstractView<Cursor, View> extends AbstractRowset<Cursor, View> {

	protected final TableViewBase viewBase;

	public AbstractView(TableViewBase viewBase) {
		this.viewBase = viewBase;
	}
	
	@Override
	public int size() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

}
