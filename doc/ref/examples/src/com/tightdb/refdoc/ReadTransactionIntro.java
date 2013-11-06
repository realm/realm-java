package com.tightdb.refdoc;
// @@Example: ex_java_typed_read_transaction_intro @@

import java.io.File;

import com.tightdb.*;

public class ReadTransactionIntro {

    public static void main(String[] args) {
        {
            // Delete file to start from scratch
            (new File("mydatabase.tightdb")).delete();
            // Create table, add columns and add row with data
            SharedGroup group = new SharedGroup("mydatabase.tightdb"); 
            WriteTransaction wt = group.beginWrite(); 
            Table users = wt.getTable("myTable");
            users.addColumn(ColumnType.STRING, "username");
            users.addColumn(ColumnType.INTEGER, "level");
            users.add("tarzan", 45);
            wt.commit();
        }

        typedReadTransactionIntro();
        dynamicReadTransactionIntro();
    }

    // @@Show@@
    public static void typedReadTransactionIntro() {
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb"); 

        // Create read transaction from the shared group
        ReadTransaction rt = group.beginRead();

        // Inside transaction is a fully consistent and immutable view of the group
        try {
            // Get a table from the group
            PeopleTable people = new PeopleTable(rt);

            // Read from the first row, the name column
            String name = people.get(0).getName();

            // Do more table read operations here...

        } finally {
            // End the read transaction in a finally block. If the read-transaction is not
            // closed, a new one cannot be started using the same SharedGroup instance.
            rt.endRead();
        }  
    } // @@EndShow@@ 
    // @@EndExample@@

    // @@Example: ex_java_dyn_read_transaction_intro @@
    // @@Show@@
    public static void dynamicReadTransactionIntro() {
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");

        // Create a read transaction from the group
        ReadTransaction rt = group.beginRead();

        // Inside transaction is a fully consistent and immutable view of the group
        try {
            // Get a table from the group
            Table table = rt.getTable("people");

            // Actions inside a ReadTransacton will never affect the original group and tables
            String name = table.getString(0, 0);

            // Do more table read operations here...
            
        } finally {
            // End the read transaction in a finally block. If the read-transaction is not
            // closed, a new one cannot be started using the same SharedGroup instance.
            rt.endRead();
        }  
    } // @@EndShow@@ 
} 
// @@EndExample@@
