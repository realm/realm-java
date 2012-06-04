package com.tightdb.doc;

import java.util.List;

public class RowDesc extends AbstractDesc {

	public RowDesc(List<Method> methods) {
		super(methods);
	}

	public void describe() {
		method("Row", "after", "Get the row after the next delta rows", "long", "delta");
		method("Row", "before", "Get the row before the previous delta rows", "long", "delta");
		method("AbstractColumn[]", "columns", "Get the columns with values from the row");
		method("Row", "next", "Get the next row as an object");
		method("Row", "previous", "Get the previous row as an object");
		method("FooType", "getFoo", "(Generic) Get the value of the 'foo' column. For each column there will be such method");
		method("void", "setFoo", "(Generic) Set the value of the 'foo' column. For each column there will be such method", "FooType", "value");
	}

}
