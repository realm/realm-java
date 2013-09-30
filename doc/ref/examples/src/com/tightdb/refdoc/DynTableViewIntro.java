
package com.tightdb.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.tightdb.*;

public class DynTableViewIntro {

    public static void main(String[] args) throws FileNotFoundException  {
        // @@Example: ex_java_dyn_table_view_intro @@
        // @@Show@@
        // Create a new table
        Table table = new Table();

        // Specify the column types and names
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");

        // Create a query object from the table without any filters 
        // and execute it to retrieve a table view
        TableView view = table.where().findAll();

        // Remove the first row from the view and thereby also the original table
        // and check that the number of rows in the original table is 2
        view.remove(0);
        Assert(table.size() == 2);

        // Change the value of column 1, row 1 to 'London'. 
        // The changes are reflected in the original table
        view.setString(1, 1, "London");
        Assert(table.getString(1, 1).equals("London"));

        // Simple aggregations
        Assert(view.sumInt(0) == 500);
        Assert(view.maximumInt(0) == 300);
        Assert(view.maximumInt(0) == 300);
        Assert(view.averageInt(0) == 250);


        // Get JSON representation of the data in the view 
        // and print it using e.g. a PrintWriter object
        PrintWriter out = new PrintWriter("fromServlet");
        out.print(view.toJson());
        out.close();
        // @@EndShow@@
        // @@EndExample@@
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 

