package com.tightdb.refdoc;
// @@Example: ex_java_dyn_read_transaction_intro @@

import java.io.File;

import com.tightdb.*;

public class DynamicReadTransactionIntro {

    public static void main(String[] args) {
        {
            // Delete file to start from scratch
            (new File("mydatabase.tightdb")).delete();
            // Create table, add columns and add row with data
            SharedGroup group = new SharedGroup("mydatabase.tightdb");
            WriteTransaction wt = group.beginWrite();
            try {
                Table users = wt.createTable("people");
                users.addColumn(ColumnType.STRING, "username");
                users.addColumn(ColumnType.INTEGER, "level");
                users.add("tarzan", 45);
                wt.commit();
            } catch(Throwable t){
                wt.rollback();
            }

            // Remember to close the shared group
            group.close();
        }

        dynamicReadTransactionIntro();
    }


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

        // Remember to close the shared group
        group.close();
    } // @@EndShow@@
}
// @@EndExample@@
