package com.tightdb.doc;

import java.util.List;

public class QueryDesc extends AbstractDesc {

    public QueryDesc(List<Constructor> constructors, List<Method> methods) {
        super("query", constructors, methods);
    }

    public void describe() {
        method("void",      "clear", "Execute the query and delete all matching rows");
        method("long",      "count", "Get the number of rows in a view");
        method("long",      "count", "Get the number of rows in a view for the specified range and limit", "long", "start", "long", "end", "long", "limit");
        method("Query",     "endGroup", "Group conditions ('right' parenthesis). Group of conditions can be nested and they are conceptually a parenthesis");

        method("View",      "findAll", "Execute a query and retrieve a View of all results");
        method("Row",       "findFirst", "Execute a query and retrieve the first result");
        method("Row",       "findLast", "Execute a query and retrieve the last result");
        method("Row",       "findNext", "Retrieve the next result for the previously executed query");

        method("Query",     "group", "Group conditions ('left' parenthesis). Group of conditions can be nested and they are conceptually a parenthesis");
        method("Query",     "or", "Logical OR");

        method("long",      "remove", "Remove the matching rows from the table, return the number of removed rows");
        method("long",      "remove", "Remove the matching rows from the table for the specified range and limit, return the number of removed rows", "long", "start", "long", "end", "long", "limit");

        // FIXME: what about these:
//      method("Query",     "subTable", "");
//      method("Query",     "endSubTable", "");

    }
}
