// @@Example: ex_java_shared_group_intro @@
package com.tightdb.refdoc;

import java.io.File;
import com.tightdb.*;

public class SharedGroupIntro {

    public static void main(String[] args) {
        // Delete file to start from scratch
        (new File("mydatabase.tightdb")).delete();
        
        // @@Show@@
        // Opens an existing database file or creates a
        // new database file and opens it into a shared group.
        SharedGroup group = new SharedGroup("mydatabase.tightdb");

        // -------------------------------------------------------------------
        // Writing to the group using transaction
        // -------------------------------------------------------------------

        // Begins a write transaction
        WriteTransaction wt = group.beginWrite(); 
        try { 
            // Get the table (or create it if it's not there)
            Table table = wt.getTable("people");
            // Define the table schema if the table is new
            if (table.getColumnCount() == 0) {
                // Define 2 columns
                table.addColumn(ColumnType.STRING,  "Name");
                table.addColumn(ColumnType.INTEGER, "Age");
            }
            // Add 3 rows of data
            table.add("Ann",   26);
            table.add("Peter", 14);
            table.add("Oldie", 117);

            // Close the transaction. 
            // All changes are written to the shared group.
            wt.commit();
        } catch (Throwable t) {
            // In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }

        // -------------------------------------------------------------------
        // Reading from the group using transaction
        // -------------------------------------------------------------------

        // Create a read transaction from the group
        ReadTransaction rt = group.beginRead();

        try {
            // Get the newly created table
            Table table = rt.getTable("people");

            // Get the size of the table
            long size = table.size();

            // Size should be 3 rows.
            Assert(size == 3);

        } finally {
            // Always end the read transaction
            rt.endRead();
        }  // @@EndShow@@
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
// @@EndExample@@
