package com.tightdb.doc;

import java.util.List;

public class ViewDesc extends AbstractDesc {

	public ViewDesc(List<Method> methods) {
		super(methods);
	}

	public void describe() {
		method("Row", "at", "Get a row as an object", "long", "rowIndex");
		method("void", "clear", "Delete all rows in the view");
		method("Row", "first", "Get the first row as an object");
		method("boolean", "isEmpty", "Check if a view is empty");
		method("Iterator", "iterator", "Get an iterator for the view rows");
		method("Row", "last", "Get the last row as an object");
		// method("View", "range", "");
		// method("void", "remove", "Remove a row from a view", "long", "rowIndex");
		method("long", "size", "Get the number of rows in a view");

		// EmployeeView v = null;

	}
}
