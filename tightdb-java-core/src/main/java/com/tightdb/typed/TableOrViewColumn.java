package com.tightdb.typed;

public interface TableOrViewColumn<Type> {

	Type[] getAll();

	void setAll(Type value);

}
