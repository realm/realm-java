package com.tightdb.lib;

public abstract class AbstractView<Cursor, View> extends AbstractRowset<Cursor, View> {

	@Override
	public int size() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

}
