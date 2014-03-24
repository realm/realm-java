package com.tightdb.refdoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.tightdb.*;
import com.tightdb.Group.OpenMode;

public class GroupExamples {

    public static void main(String[] args) throws FileNotFoundException  {

        new File("mydatabasefile.tightdb").delete();

        // Constructor methods
        constructorPlainExample();
        constructorFileExample();
        constructorStringExample();
        constructorStringModeExample();
        constructorByteArrayExample();

        // Table methods
        createTableExample();
        getTableExample();
        getTableNameExample();
        hasTableExample();

        // Serialization methods
        writeToFileExample();
        writeToMemExample();
        toStringExample();
        toJSONExample();

        // Group methods
        sizeExample();
        isEmptyExample();
        equalsExample();
    }


    // **********************
    // Constructor methods
    // **********************


    public static void constructorPlainExample(){
        // @@Example: ex_java_group_constructor_plain @@
        // @@Show@@
        // Create a group in memory. Can be saved to disk later
        Group group = new Group();

        Table table = group.createTable("mytable");
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void constructorFileExample(){
        // @@Example: ex_java_group_constructor_file @@
        // @@Show@@
        // Point to file
        File file = new File("mydatabase.tightdb");
        if (file.exists()) {
            // If file exists, instantiate group from the file
            Group group = new Group(file);

            Table table = group.getTable("mytable");
            // More table operations...
        }
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void constructorStringExample(){
        // @@Example: ex_java_group_constructor_string @@
        Group group1 = new Group("mydatabase.tightdb", OpenMode.READ_WRITE);
        Table table1 = group1.getTable("mytable");
        group1.commit();
                
        // @@Show@@
        // Instantiate group by pointing to the tightdb file path
        // The group is by default opened in READ_ONLY mode.
        Group group = new Group("mydatabase.tightdb");

        Table table = group.getTable("mytable");
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void constructorStringModeExample(){
        // @@Example: ex_java_group_constructor_string_mode @@
        // @@Show@@
        // Point to the non-existing file. This mode will create a file, if it does not exist.
        Group group = new Group("non-exisiting-db.tightdb", OpenMode.READ_WRITE);

        Table table = group.createTable("mytable");
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void constructorByteArrayExample(){
        // @@Example: ex_java_group_constructor_memory @@
        // @@Show@@
        // Existing group
        Group existingGroup = new Group("mydatabase.tightdb");
        // Group is written to a byte array
        byte[] groupMem = existingGroup.writeToMem();

        // A new group can be created from this byte array
        Group group = new Group(groupMem);

        Table table = group.getTable("mytable");
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@
    }

    // **********************
    // Table methods
    // **********************

    public static void createTableExample(){
        // @@Example: ex_java_group_create_table @@
        // @@Show@@
        Group group = new Group("mydatabase.tightdb", OpenMode.READ_WRITE);

        // Create table and return it
        Table table = group.createTable("mytable");

        // Add columns and data
        table.addColumn(ColumnType.STRING, "String");
        table.addColumn(ColumnType.INTEGER, "int");
        table.addColumn(ColumnType.BOOLEAN, "boolean");

        table.add("String value", 400, true); // String, long, boolean
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@

        group.commit();
    }

    public static void getTableExample(){
        // @@Example: ex_java_group_get_table @@
        // @@Show@@
        Group group = new Group("mydatabase.tightdb", OpenMode.READ_WRITE);

        // Get table and return it
        Table table = group.getTable("mytable");

        table.add("String value", 400, true); // String, long, boolean
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void hasTableExample(){
        // @@Example: ex_java_group_has_table @@
        // @@Show@@
        Group group = new Group("mydatabase.tightdb", OpenMode.READ_WRITE);

        // A table is created (if not already in the group)
        if(!group.hasTable("myTable")) {
            group.createTable("myTable");
        }
        // More table operations...

        // Use has table to check if group contains a table with the specified name
        Assert(group.hasTable("myTable"));
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void getTableNameExample(){
        // @@Example: ex_java_group_get_table_name @@
        // @@Show@@
        Group group = new Group();

        // Add 2 tables to the group
        Table table1 = group.createTable("mytable1"); // Will be positioned at index 0
        Table table2 = group.createTable("mytable2"); // Will be positioned at index 1

        // Get name of a table by it's index
        Assert(group.getTableName(0).equals("mytable1"));
        Assert(group.getTableName(1).equals("mytable2"));
        // @@EndShow@@
        // @@EndExample@@
    }

    // **********************
    // Serialization methods
    // **********************

    public static void writeToFileExample(){
        // @@Example: ex_java_group_write_to_file @@
        // @@Show@@
        Group group = new Group();

        Table table = group.createTable("mytable");
        // More table operations...

        try {
            // Write to the specified file
            group.writeToFile("mydatabasefile.tightdb");
        } catch (IOException e) {
            // Exception if file already exists
            e.printStackTrace();
        }
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void writeToMemExample() {
        OutputStream outputStream = null;

        // @@Example: ex_java_group_write_to_mem @@
        // @@Show@@
        Group group = new Group();

        Table table = group.createTable("mytable");
        // More table operations...

        // Write group to byte array.
        byte[] array = group.writeToMem();

        // E.g send byte array through output stream
        try {
            outputStream.write(array);
        } catch (Exception e) {

        }
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void toStringExample(){
        // @@Example: ex_java_group_to_string @@
        // @@Show@@
        Group group = new Group();

        Table table = group.createTable("mytable");
        // More table operations...

        // Get a String representation of the group
        String toString = group.toString();
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void toJSONExample() throws FileNotFoundException{
        // @@Example: ex_java_group_to_json @@
        // @@Show@@
        Group group = new Group();

        Table table = group.createTable("mytable");
        // More table operations...

        // Get a JSON representation of the group
        String json = group.toJson();

        // Print json e.g. using a printwriter
        PrintWriter out = new PrintWriter("fromServlet");
        out.print(json);
        out.close();
        // @@EndShow@@
        // @@EndExample@@
    }

    // **********************
    // Group methods
    // **********************

    public static void sizeExample(){
        // @@Example: ex_java_group_size @@
        // @@Show@@
        Group group = new Group();

        // Add 2 tables to the group
        Table table1 = group.createTable("mytable1");
        Table table2 = group.createTable("mytable2");

        // Get size of the group
        Assert(group.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void isEmptyExample(){
        // @@Example: ex_java_group_is_empty @@
        // @@Show@@
        // New empty group
        Group group = new Group();

        // Group is empty
        Assert(group.isEmpty() == true);

        // Add 2 tables to the group
        Table table1 = group.createTable("mytable1");
        Table table2 = group.createTable("mytable2");

        // Group is not empty
        Assert(group.isEmpty() == false);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void equalsExample(){
        // @@Example: ex_java_group_operator_equal @@
        // @@Show@@
        // Group1 with 1 table with 1 row of data
        Group group1 = new Group();
        Table table1 = group1.createTable("mytable1");
        table1.addColumn(ColumnType.STRING, "stringCol");
        table1.add("StringVal");

        // Group2 with 1 table with 1 row of data
        Group group2 = new Group();
        Table table2 = group2.createTable("mytable1");
        table2.addColumn(ColumnType.STRING, "stringCol");
        table2.add("StringVal");

        // Groups are equal
        Assert(group1.equals(group2));

        // Add 1 extra row to table in group 2
        table2.add("new String val");

        // Groups are not equal
        Assert(group1.equals(group2) == false);

        // @@EndShow@@
        // @@EndExample@@
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
}
