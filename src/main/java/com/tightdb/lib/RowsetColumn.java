package com.tightdb.lib;

public interface RowsetColumn<Type> {

	Type[] getAll();

	void setAll(Type value);

}
