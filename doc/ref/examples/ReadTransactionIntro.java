// @@Example: ex_java_read_transaction_intro @@

package com.tightdb.refdoc;

import com.tightdb.*;

public class ReadTransactionIntro {

    public static void main(String[] args) {
        // @@Show@@
        //Opens an existing database file. We assume the file is also being accessed by other processes, thats why we use a SharedGroup object
        SharedGroup group = new SharedGroup("mydatabase.tightdb");
        
        //-------------------------------------------------------------------
        //Reading from the group using a transaction
        //-------------------------------------------------------------------
        
        //Create a read transaction from the group
        ReadTransaction rt = group.beginRead();
        
        //Inside the read transaction we have a fully consistent and non mutable view of the group
        try {
            //Get the the name of the first table in the group, and retrieve the table using the name
            String tableName = rt.getTableName(0);
            Table table = rt.getTable(tableName);

            //Get the size and column number of the table
            long size = table.size();
            long columnCount = table.getColumnCount();
            
            System.out.println(tableName + " has " + columnCount + " columns and " + size + " rows!");
            
            //Determine the type of the first column
            ColumnType type = table.getColumnType(0);
            
            if(type.equals(ColumnType.ColumnTypeString)){
                //Get the String value from column 0, row 0, if the type is a String
                String value = table.getString(0, 0);
                
                System.out.println("Value is " + value);
            }
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
} //@@EndExample@@