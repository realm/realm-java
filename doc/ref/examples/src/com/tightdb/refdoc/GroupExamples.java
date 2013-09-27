

package com.tightdb.refdoc;


import java.io.File;
import java.io.FileNotFoundException;

import com.tightdb.*;
import com.tightdb.Group.OpenMode;

public class GroupExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        
        // Constrcutor methods
        constructorPlainExample();
        constructorFileExample();
        constructorStringExample();
        constructorStringModeExample();
        constructorByteArrayExample();
        
        
        // Table methods
        getTableExample();
        getTableNameExample();
        hasTableExample();
    }
    
    
    // **********************
    // Constructor methods
    // **********************


    public static void constructorPlainExample(){
        // @@Example: ex_java_group_constructor_plain @@
        // @@Show@@
        // Create a group in memory. Can be saved to disk later
        Group group = new Group(); 

        Table table = group.getTable("mytable");
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void constructorFileExample(){
        // @@Example: ex_java_group_constructor_file @@
        // @@Show@@
        // Point to file
        File file = new File("mydatabase.tightdb");
        if(file.exists()){
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
        // @@Show@@
        // Instantiate group by pointing to the tightdb file path
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

        Table table = group.getTable("mytable");
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
    
    public static void getTableExample(){
        // @@Example: ex_java_group_get_table @@
        // @@Show@@
        Group group = new Group("mydatabase.tightdb");
        
        // A table is created (if not already in the group) and returned
        Table table = group.getTable("mytable");

        table.add("String value", 400, true); // String, long, boolean
        // More table operations...
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void hasTableExample(){
        // @@Example: ex_java_group_has_table @@
        // @@Show@@
        Group group = new Group("mydatabase.tightdb");
        
        // A table is created (if not already in the group) and returned
        Table table = group.getTable("mytable");
        // More table operations...

        // Use has table to check if group contains a table with the specified name
        Assert(group.hasTable("mytable"));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void getTableNameExample(){
        // @@Example: ex_java_group_get_table_name @@
        // @@Show@@
        Group group = new Group("mydatabase.tightdb");
        
        // Add 2 tables to the group
        Table table1 = group.getTable("mytable1"); // Will be positioned at index 0
        Table table2 = group.getTable("mytable2"); // Will be positioned at index 1

        // Get name of a table by it's index
        Assert(group.getTableName(0).equals("mytable1"));
        Assert(group.getTableName(1).equals("mytable2"));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    
    // **********************
    // Serialization methods
    // **********************   
    
    
    
    
    // **********************
    // Group methods
    // **********************   
    
    
    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
