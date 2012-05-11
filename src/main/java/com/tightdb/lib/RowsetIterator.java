package com.tightdb.lib;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RowsetIterator<T> implements Iterator<T> {

	private final AbstractRowset<?, ?, ?> rowset;
	private int endIndex = 0;
	private int index = 0;

	public RowsetIterator(final AbstractRowset<T, ?, ?> rowset) {
		this.rowset = rowset;
		this.endIndex = rowset.size();
		this.index = 0;
	}

	public boolean hasNext() {
		return (index < endIndex);
	}

	public T next() {
		if (hasNext() == false) {
			throw new NoSuchElementException();
		}
		return (T) rowset.at(index++);
	}

	public void remove() {
		throw new UnsupportedOperationException("The method remove() is currently not supported!");
	}

}
