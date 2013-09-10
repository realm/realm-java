
package com.tightdb.refdoc;

import com.tightdb.*;

public class WriteTransactionIntro {

    public static void main(String[] args) {
        typedWriteTransactionIntro();
        dynamicWriteTransactionIntro();
    }

    // @@Example: ex_java_typed_write_transaction_intro @@
    // @@Show@@
    @DefineTable(table = "UserTable")
    class User {
        String username;
        int level;
    } 
   
    public static void typedWriteTransactionIntro(){
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");
 
        // Begin write transaction 
        WriteTransaction wt = group.beginWrite(); 
        try { 
            // Create table and add row with data
            UserTable users = new UserTable(wt);
            users.add("tarzan", 45);

            // Close the transaction and all changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            // In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }
    } // @@EndShow@@  
    // @@EndExample@@


    // @@Example: ex_java_dyn_write_transaction_intro @@ 
    // @@Show@@
    public static void dynamicWriteTransactionIntro(){
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");

        // Begin write transaction
        WriteTransaction wt = group.beginWrite(); 
        try { 
            // Create table, add columns and add row with data
            Table users = wt.getTable("users");
            users.addColumn(ColumnType.STRING, "username");
            users.addColumn(ColumnType.LONG, "level");
            users.add("tarzan", 45);

            // Close the transaction. All changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            // In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }
    }   // @@EndShow@@  
    // @@EndExample@@
} 
