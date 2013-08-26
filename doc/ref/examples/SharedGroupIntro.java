// @@Example: ex_java_shared_group_intro @@

package com.tightdb.refdoc;

import com.tightdb.*;

public class SharedGroupIntro {

    public static void main(String[] args) {
        // @@Show@@
        //Opens an existing database file or creates a new database file and opens it into a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");
        
        //-------------------------------------------------------------------
        //Writing to the group using transaction
        //-------------------------------------------------------------------

        //Begins a write transaction
        WriteTransaction wt = group.beginWrite(); 
        try { 
            //Creates a new table by using getTable with the new table name as parameter
            Table table = wt.getTable("newTable");
            
            //Specify 2 columns and add 3 rows of data
            table.addColumn(ColumnType.ColumnTypeInt, "ID");
            table.addColumn(ColumnType.ColumnTypeString, "City");
            table.add(1, "Washington");
            table.add(2, "Los Angeles");
            table.add(3, "New York");

            //Commit the changes, otherwise no data is written to the table
            wt.commit();
        } catch (Throwable t) {
            wt.rollback();
        }
        
        //-------------------------------------------------------------------
        //Reading from the group using transaction
        //-------------------------------------------------------------------
        
        //Create a read transaction from the group
        ReadTransaction rt = group.beginRead();
        
        try {
            //Get the newly created table
            Table table = rt.getTable("newTable");

            //Get the size of the table
            long size = table.size();

            //Size should be 3, as we have added 3 rows
            Assert(size == 3);

        } finally {
            //Always end the read transaction
            rt.endRead();
        }  // @@EndShow@@
    }
    
    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
//@@EndExample@@