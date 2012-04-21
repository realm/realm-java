package com.tigthdb.lib;

import java.util.Iterator;

public abstract class AbstractTable<E, V> implements Iterable<E> {

	public V range(long from, long to) {
		return null;
	}

	public Iterator<E> iterator() {
		return null;
	}

	public E first() {
		return null;
	}

	public E last() {
		return null;
	}

}
