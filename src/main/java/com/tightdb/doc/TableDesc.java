package com.tightdb.doc;

import java.util.List;

public class TableDesc extends AbstractDesc {

	public TableDesc(List<Method> methods) {
		super(methods);
	}

	public void describe() {
		method("Row", 		"add", "Insert a new row at the end of the table", "RowDataTypes...", "rowData...");
		method("Row", 		"at", "Get a specific row as an object (rowIndex starts at 0)", "long", "rowIndex");
		method("void", 		"clear", "Delete all rows in the table");
		method("Row", 		"first", "Get the first row as an object");
		method("String",	"getName", "Get the table name for tables named in a Group");
		//TODO: hasIndex
		method("Row", 		"insert", "Insert a new row at the index in the table", "long", "rowIndex", "RowDataTypes...", "rowData...");
		method("boolean",	"isEmpty", "Check if a table has no rows");
		method("Iterator", 	"iterator", "Get an iterator for the table rows");
		method("Row", 		"last", "Get the last row as an object");
		// method("View", "range", "");
		method("void", 		"remove", "Remove a specific row from the table", "long", "rowIndex");
		//TODO: setIndex
		method("long", 		"size", "Get the number of rows in the table");
		method("Query", 	"where", "Create a query for the table");

		// EmployeeTable t = new EmployeeTable();
	}
}
