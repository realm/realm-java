

package com.tightdb.refdoc;


import java.io.File;
import java.io.FileNotFoundException;

import com.tightdb.*;
import com.tightdb.Group.OpenMode;

public class GroupExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        constructorPlainExample();
        constructorFileExample();
    }


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
} 
