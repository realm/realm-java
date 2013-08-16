// @@Example: ex_java_write_transaction_intro @@

package com.tightdb.refdoc;

import com.tightdb.*;

public class WriteTransactionIntro {

    public static void main(String[] args) {
        // @@Show@@
        //Opens an existing database file. We assume the file is also being accessed by other processes, thats why we use a SharedGroup object
        SharedGroup group = new SharedGroup("mydatabase.tightdb");

        //-------------------------------------------------------------------
        //Writing to the group using transactions
        //-------------------------------------------------------------------

        //Begins a write transaction. Any other process trying to initiate a write transaction will be stalled until this transaction ends.
        WriteTransaction wt = group.beginWrite(); 
        try { 
            //Transaction extends from Group and can be used on methods that take Group object as input
            update(wt);

            //Closes the transaction and all changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            //In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }
    }

    public static void update(Group group){
        Table people = group.getTable("people");
        //All updates here will be persisted when wt.commit() is called
        //...
    } // @@EndShow@@ 

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} //@@EndExample@@