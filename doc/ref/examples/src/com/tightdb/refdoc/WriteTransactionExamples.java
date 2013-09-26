

package com.tightdb.refdoc;


import java.io.FileNotFoundException;

import com.tightdb.*;

public class WriteTransactionExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        commitExample();
        roolbackExample
    }
    
    
    public static void commitExample(){
        // @@Example: ex_java_write_transaction_commit @@
        // @@Show@@
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");
 
        // Begin write transaction 
        WriteTransaction wt = group.beginWrite(); 
        try { 
            // Get table from transaction
            Table table = wt.getTable("mytable");
            
            // Do some changes
            table.add("String value", 1000, true); // String, long, boolean

            // Close the transaction. All changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            // In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void roolbackExample(){
        // @@Example: ex_java_write_transaction_rollback @@
        // @@Show@@
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");
 
        // Begin write transaction 
        WriteTransaction wt = group.beginWrite(); 
        try { 
            // Get table from transaction
            Table table = wt.getTable("mytable");
            
            // Do some changes
            table.add("String value", 1000, true); // String, long, boolean

            // Close the transaction. All changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            // In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }
        // @@EndShow@@
        // @@EndExample@@
    }
} 
