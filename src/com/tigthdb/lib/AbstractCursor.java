package com.tigthdb.lib;

public abstract class AbstractCursor<E> {

	public E next() {
		return null;
	}

	public E previous() {
		return null;
	}

	public E before(long delta) {
		return null;
	}

	public E after(long delta) {
		return null;
	}

}
