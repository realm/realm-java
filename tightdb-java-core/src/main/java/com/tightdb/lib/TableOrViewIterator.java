package com.tightdb.lib;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TableOrViewIterator<T> implements Iterator<T> {

	private final AbstractTableOrView<T, ?, ?> tableOrView;
	private long endIndex = 0;
	private long index = 0;

	public TableOrViewIterator(final AbstractTableOrView<T, ?, ?> tableOrView) {
		this.tableOrView = tableOrView;
		this.endIndex = tableOrView.size();
		this.index = 0;
	}

	public boolean hasNext() {
		return (index < endIndex);
	}

	public T next() {
		if (hasNext() == false) {
			throw new NoSuchElementException();
		}
		return tableOrView.at(index++);
	}

	public void remove() {
		throw new UnsupportedOperationException("The method remove() is currently not supported!");
	}

}
