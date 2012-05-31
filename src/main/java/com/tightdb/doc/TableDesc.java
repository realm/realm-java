package com.tightdb.doc;

import java.util.List;

public class TableDesc extends AbstractDesc {

	public TableDesc(List<Method> methods) {
		super(methods);
	}

	public void describe() {
		method("Row", "add", "Insert a new row at the end of the table", "row data", "...");
		method("Row", "at", "Get a row as an object", "long", "rowIndex");
		method("void", "clear", "Delete all rows in the table");
		method("Row", "first", "Get the first row as an object");
		method("String", "getName", "Get the table name");
		method("Row", "insert", "Insert a new row at the end of the table", "long", "position", "row data", "...");
		method("boolean", "isEmpty", "Check if a table is empty");
		method("Iterator", "iterator", "Get an iterator for the table rows");
		method("Row", "last", "Get the last row as an object");
		// method("View", "range", "");
		method("void", "remove", "Remove a row from a table", "long", "rowIndex");
		method("long", "size", "Get the number of rows in a table");
		method("Query", "where", "Create a query for the table");

		// EmployeeTable t = new EmployeeTable();
	}
}
