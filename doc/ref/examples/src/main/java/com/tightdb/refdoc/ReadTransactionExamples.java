package com.tightdb.refdoc;

import java.io.File;
import java.io.FileNotFoundException;

import com.tightdb.*;

public class ReadTransactionExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        endReadExample();
    }

    public static void endReadExample(){
        // @@Example: ex_java_shared_group_end_read @@
        {
            // Delete file to start from scratch
            (new File("mydatabase.tightdb")).delete();
            (new File("mydatabase.tightdb.lock")).delete();
            // Create table, add columns and add row with data
            SharedGroup group = new SharedGroup("mydatabase.tightdb");
            WriteTransaction wt = group.beginWrite();

            try{
                Table users = wt.getTable("myTable");
                users.addColumn(ColumnType.STRING, "username");
                users.addColumn(ColumnType.INTEGER, "level");
                users.add("tarzan", 45);
                wt.commit();
            } catch (Throwable t){
                wt.rollback();
            }

            // Remember to close the shared group
            group.close();
        }

        // @@Show@@
        // Open database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");

        // Start read transaction
        ReadTransaction rt = group.beginRead();

        // Always do try / finally when using read transactions
        try {
            if (rt.hasTable("myTable")) {
                Table table = rt.getTable("myTable");
                // More table read operations here....
            }
        } finally {
            rt.endRead(); // End read transaction in finally
        }

        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }
}
