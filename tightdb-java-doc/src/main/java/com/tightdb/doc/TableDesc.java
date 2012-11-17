package com.tightdb.doc;

import java.util.List;

public class TableDesc extends AbstractDesc {

	public TableDesc(List<Constructor> constructors, List<Method> methods) {
		super("table", constructors, methods);
	}

	public void describe() {
		constructor("Allocates and instantiates a TightDB table. <br/> Note: Only tables which are part of a Group can be serialized to memory or disk");
		constructor("Allocates and instantiates a TightDB table, as part of the specified group", "Group", "group");
		
		method("Row", 		"add", "Insert a new row at the end of the table", "RowDataTypes...", "rowData...");
		method("Row", 		"at", "Get a specific row as an object (rowIndex starts at 0)", "long", "rowIndex");
		method("void", 		"clear", "Delete all rows in the table");
		method("Row", 		"first", "Get the first row as an object");
		method("long", 		"getColumnCount", "Get number of columns in the table");
		method("String", 	"getColumnName", "Get the name of the column");
		method("ColumnType","getColumnType", "Get the type of the column");
		method("long",      "getColumnIndex", "Get the 0-based index of a column with the specified name", "String", "columnName");
		method("String",	"getName", "Get the table name for tables named in a Group");
		//TODO: hasIndex
		method("Row", 		"insert", "Insert a new row at the index in the table", "long", "rowIndex", "RowDataTypes...", "rowData...");
		method("boolean",	"isEmpty", "Check if a table has no rows");
		method("Iterator", 	"iterator", "Get an iterator for the table rows");
		method("Row", 		"last", "Get the last row as an object");
		// method("View", "range", "");
		method("void", 		"optimize", "Optimize the database size. (Currently pack strings)");
		method("void", 		"remove", "Remove a specific row from the table", "long", "rowIndex");
		method("void", 		"removeLast", "Remove the last row from the table");
		//TODO: setIndex
		method("long", 		"size", "Get the number of rows in the table");
		
		method("String", 	"toJson", "Retrieve JSON representaion of the data");
		method("Query", 	"where", "Create a query for the table");

		// EmployeeTable t = new EmployeeTable();
	}
	
}
