package com.tigthdb.lib;

import java.util.List;

public abstract class AbstractColumn<Type, Query> {

	// TODO: please consider keeping get() and set() due to many cool advantages

	public Type get() {
		return null;
	}

	public void set(Type value) {

	}

	public List<Type> all() {
		return null;
	}

	public Query is(Type value) {
		return null;
	}

	public Query isnt(Type value) {
		return null;
	}

}
