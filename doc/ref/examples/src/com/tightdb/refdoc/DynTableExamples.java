

package com.tightdb.refdoc;


import com.tightdb.*;

public class DynTableExamples {

    public static void main(String[] args)  {
        addAtExample();
        addAtExample();
        setExample();
        removeExample();
        removeLastExample();
        addEmptyRowExample();
        addEmptyRowsExample();
        clearExample();
        isEmptyExample();
        sizeExample();

    }


    public static void addExample(){
        // @@Example: ex_java_dyn_table_add @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void addAtExample(){
        // @@Example: ex_java_dyn_table_add_at @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");

        // Add a row at row index 2 and shift the subsequent rows one down
        table.addAt(2, 250, "Texas");
        // @@EndShow@@
        // @@EndExample@@
    }



    public static void setExample(){
        // @@Example: ex_java_dyn_table_set @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Replaces the the first row
        table.set(0, 100, "Washington DC");

        Assert(table.getString(1, 0).equals("Washington DC"));
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void removeExample(){
        // @@Example: ex_java_dyn_table_remove @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Removes the the first row
        table.remove(0);

        //Table.size is now 1
        Assert(table.size() == 1);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void removeLastExample(){
        // @@Example: ex_java_dyn_table_remove_last_row @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");

        // Removes the the last row
        table.removeLast();

        //Table.size is now 3
        Assert(table.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }

    
    public static void addEmptyRowExample(){
        // @@Example: ex_java_dyn_table_add_empty_row @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        
        // Add an empty row with default values
        table.addEmptyRow();

        // Table.size is now 3
        Assert(table.size() == 3);
        
        //Default values check
        Assert(table.getLong(0, 2) == 0);
        Assert(table.getString(1, 2).equals(""));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void addEmptyRowsExample(){
        // @@Example: ex_java_dyn_table_add_empty_rows @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        
        // Add 10 empty rows with default values
        table.addEmptyRows(10);

        // Table.size is now 12
        Assert(table.size() == 12);
        
        //Default values check
        Assert(table.getLong(0, 11) == 0);
        Assert(table.getString(1, 11).equals(""));
        // @@EndShow@@
        // @@EndExample@@
    }

    
    public static void clearExample(){
        // @@Example: ex_java_dyn_table_clear @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        
        // Remove all rows in table
        table.clear();

        // Table.size is now 0
        Assert(table.size() == 0);
        // Table is empty
        Assert(table.isEmpty());
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void isEmptyExample(){
        // @@Example: ex_java_dyn_table_is_empty @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");
        
        // No data has been added to the table
        // Table is empty
        Assert(table.isEmpty());
        
        // Table.size is 0
        Assert(table.size() == 0);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void sizeExample(){
        // @@Example: ex_java_dyn_table_size @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.LONG, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        
        // 2 rows have been added
        Assert(table.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }


    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
