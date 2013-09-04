// @@Example: ex_java_dyn_table_add_at @@

package com.tightdb.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.tightdb.*;

public class TableViewIntro {

    public static void main(String[] args) throws FileNotFoundException {
        // @@Show@@
        // Creates a new table
        Table table = new Table();
        
        // Specify the column types and names
        table.addColumn(ColumnType.ColumnTypeInt, "ID");
        table.addColumn(ColumnType.ColumnTypeString, "City");
        
        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");

        // Add a row at row index 2 and shift the subsequent rows one down
        table.addAt(2, 250, "Texas");
        // @@EndShow@@
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
// @@EndExample@@