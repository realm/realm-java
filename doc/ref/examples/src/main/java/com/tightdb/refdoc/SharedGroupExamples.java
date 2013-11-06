package com.tightdb.refdoc;

import java.io.FileNotFoundException;

import com.tightdb.ReadTransaction;
import com.tightdb.SharedGroup;
import com.tightdb.Table;
import com.tightdb.WriteTransaction;

public class SharedGroupExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        
        constructorStringExample();
        beginWriteExample();
        beginReadExample();
        hasChangedExample();
    }

    // **********************
    // Constructor methods
    // **********************

    public static void constructorStringExample(){
        // @@Example: ex_java_shared_group_constructor_string @@
        // @@Show@@
        // Instantiate group by specifying path to tightdb file
        SharedGroup group = new SharedGroup("mydatabase.tightdb"); 
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void beginWriteExample(){
        // @@Example: ex_java_shared_group_begin_write @@
        // @@Show@@
        SharedGroup group = new SharedGroup("mydatabase.tightdb"); 
        
        // Starts a write transaction
        WriteTransaction wt = group.beginWrite();
        
        // Use try / catch when using transactions
        try {
            Table table = wt.getTable("mytable");
            // Do table write operations on table here
            // ...
            
            wt.commit(); // Changes are saved to file, when commit() is called
        } catch (Throwable t){
            wt.rollback(); // If an error occurs, always rollback
        }
        
        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void beginReadExample(){
        // @@Example: ex_java_shared_group_begin_read @@
        // @@Show@@
        SharedGroup group = new SharedGroup("mydatabase.tightdb"); 
        
        // Starts a read transaction
        ReadTransaction rt = group.beginRead();
        
        // Use try / catch when using transactions
        try {
            Table table = rt.getTable("mytable"); // Table must exist in shared group
            // Do table operations on table here
            // ...
            
        } finally{
            rt.endRead(); // Always end read transaction in finally block
        }
        
        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void hasChangedExample(){
        // @@Example: ex_java_shared_group_has_changed @@
        // @@Show@@
        SharedGroup group = new SharedGroup("mydatabase.tightdb"); 
        
        boolean hasChanged = group.hasChanged();
        // @@EndShow@@
        // @@EndExample@@
    }
} 
