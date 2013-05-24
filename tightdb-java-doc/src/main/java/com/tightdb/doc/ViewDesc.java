package com.tightdb.doc;

import java.util.List;

public class ViewDesc extends AbstractDesc {

	public ViewDesc(List<Constructor> constructors, List<Method> methods) {
		super("view", constructors, methods);
	}

	public void describe() {
		method("Row",       "at", "[Deprecated] Get a specific row", "long", "rowIndex");
		method("void",      "clear", "Delete all rows in the view");
		method("Row",       "first", "Get the first row");
		method("Row",       "get", "Get a specific row", "long", "rowIndex");
		method("boolean",   "isEmpty", "Check if the view has no rows");
		method("Iterator",  "iterator", "Get an iterator for the view rows");
		method("Row",       "last", "Get the last row");
		// method("View",   "range", "");
		method("void", 		"remove", "Remove a specific row from the view", "long", "rowIndex");
		method("void", 		"removeLast", "Remove the last row from the view");
		method("long",      "size", "Get the number of rows in the view");
		method("String", 	"toJson", "Retrieve JSON representaion of the data");

		// EmployeeView v = null;

	}
}
