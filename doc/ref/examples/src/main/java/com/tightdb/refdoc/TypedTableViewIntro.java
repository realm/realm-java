 // @@Example: ex_java_typed_table_view_intro @@
package com.tightdb.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.tightdb.*;

public class TypedTableViewIntro {

    @DefineTable
    class City {
        int id;
        String name;
    }

    public static void main(String[] args) throws FileNotFoundException  {
        // @@Show@@
        // Create a new table
        CityTable cities = new CityTable();

        // Add data to the table
        cities.add(100, "Washington");
        cities.add(200, "Los Angeles");
        cities.add(300, "New York");

        // Create a query object from the table without any filters 
        // and execute it to retrieve a table view
        CityView view = cities.where().findAll();

        // Remove the first row from the view and thereby also the original table
        // and check that the number of rows in the original table is 2
        view.remove(0);
        Assert(cities.size() == 2);

        // Change the value of column 1, row 1 to 'London'. 
        // The changes are reflected in the original table
        view.get(1).setName("London");
        Assert(cities.get(1).getName().equals("London"));

        // Simple aggregations
        Assert(view.id.sum() == 500);
        Assert(view.id.maximum() == 300);
        Assert(view.id.maximum() == 300);
        Assert(view.id.average() == 250);


        // Get JSON representation of the data in the view 
        // and print it using e.g. a PrintWriter object
        PrintWriter out = new PrintWriter("fromServlet");
        out.print(view.toJson());
        out.close();
        System.out.println(view.toJson());
        // @@EndShow@@
        
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
}
//@@EndExample@@
