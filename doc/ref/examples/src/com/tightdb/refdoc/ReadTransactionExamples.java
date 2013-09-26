

package com.tightdb.refdoc;


import java.io.FileNotFoundException;

import com.tightdb.*;

public class ReadTransactionExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        endReadExample();
    }
    
    
    public static void endReadExample(){
        // @@Example: ex_java_shared_group_end_read @@
        // @@Show@@
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb"); 
        
        // Start read transaction
        ReadTransaction rt = group.beginRead();
        
        // Always do try / finally when using read transactions
        try { 
            Table table = rt.getTable("mytable");
            // More table read operations here
        } finally {
            rt.endRead(); // End read transaction in finally
        }
        
        // @@EndShow@@
        // @@EndExample@@
    }
} 
