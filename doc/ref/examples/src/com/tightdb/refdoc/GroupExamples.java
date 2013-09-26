

package com.tightdb.refdoc;


import java.io.File;
import java.io.FileNotFoundException;

import com.tightdb.*;

public class GroupExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        constructorPlainExample();
        constructorFileExample
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
} 
