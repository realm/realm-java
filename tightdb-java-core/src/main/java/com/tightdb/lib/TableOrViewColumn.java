package com.tightdb.lib;

public interface TableOrViewColumn<Type> {

	Type[] getAll();

	void setAll(Type value);

}
