package com.tightdb.doc;

import java.util.List;

public class RowDesc extends AbstractDesc {

	public RowDesc(List<Method> methods) {
		super(methods);
	}

	public void describe() {
		method("Row", "after", "");
		method("Row", "before", "");
		method("AbstractColumn[]", "columns", "");
		method("Row", "next", "");
		method("Row", "previous", "");
		method("FooType", "getFoo", "");
		method("void", "setFoo", "", "FooType", "value");
	}

}
