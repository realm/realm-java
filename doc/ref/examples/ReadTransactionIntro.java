// @@Example: ex_java_read_transaction_intro @@

package com.tightdb.refdoc;

import com.tightdb.*;

public class ReadTransactionIntro {

    public static void main(String[] args) {
        // @@Show@@
        //Opens an existing database file. 
        //We assume the file is also being accessed by other processes, 
        //thats why we use a SharedGroup object
        SharedGroup group = new SharedGroup("mydatabase.tightdb");

        //-------------------------------------------------------------------
        //Reading from the group using a transaction
        //-------------------------------------------------------------------

        //Create a read transaction from the group
        ReadTransaction rt = group.beginRead();

        //Inside the read transaction we have a fully consistent and immutable view of the group
        try {
            //Get a table from the group
            Table table = rt.getTable("table");

            //Do all table read operations here
            //Actions inside a ReadTransacton will never affect the original group and tables
            String value = table.getString(1, 0);

            //As a Transaction extends Group, it can be passed as a Group parameter
            analyzeGroup(rt);

        } finally {
            //Always end the read transaction in a finally block, if something should go
            //wrong inside the transaction. If it is not closed, a new transaction can not be initialized
            rt.endRead();
        }  
    }

    private static void analyzeGroup(Group group){
        String tableName = group.getTableName(0);
    }// @@EndShow@@  //@@EndExample@@

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 