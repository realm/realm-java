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
            //Gets and existing table from the group, and adds values
            Table table = wt.getTable("PeopleTable");
            table.add("Peter", "Johnson", 314);
            table.add("Miranda", "Flint", 502);
            table.add("Jessica", "Appleton", 220);

            //Closes the transaction and all changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            //In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }
    }
    
    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} //@@EndExample@@