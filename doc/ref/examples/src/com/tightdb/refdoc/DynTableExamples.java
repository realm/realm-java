

package com.tightdb.refdoc;

import java.io.FileNotFoundException;

import com.tightdb.*;

public class DynTableExamples {

    public static void main(String[] args) throws FileNotFoundException {
        addAtExample();
        addAtExample();
        setExample();
        removeExample();
        removeLastExample();
        
    }
    
    
    
    public static void addExample(){
        // @@Example: ex_java_dyn_table_add @@
        // @@Show@@
        // Creates a new table
        Table table = new Table();
        
        // Specify the column types and names
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");
        
        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void addAtExample(){
        // @@Example: ex_java_dyn_table_add_at @@
        // @@Show@@
        // Creates a new table
        Table table = new Table();
        
        // Specify the column types and names
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");
        
        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");
        
        // Add a row at row index 2 and shift the subsequent rows one down
        table.addAt(2, 250, "Texas");
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    
    public static void setExample(){
        // @@Example: ex_java_dyn_table_set @@
        // @@Show@@
        // Creates a new table
        Table table = new Table();
        
        // Specify the column types and names
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");
        
        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");
        
        //Replaces the the first row
        table.set(0, 100, "Washington DC");
        
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void removeExample(){
        // @@Example: ex_java_dyn_table_remove @@
        // @@Show@@
        // Creates a new table
        Table table = new Table();
        
        // Specify the column types and names
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");
        
        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");
        
        //Removes the the first row. Table.size is now 3
        table.remove(0);
        
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void removeLastExample(){
        // @@Example: ex_java_dyn_table_remove_last_row @@
        // @@Show@@
        // Creates a new table
        Table table = new Table();
        
        // Specify the column types and names
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");
        
        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");
        
        //Removes the the last row. Table.size is now 3
        table.removeLast();
        
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    
    
    
    

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
