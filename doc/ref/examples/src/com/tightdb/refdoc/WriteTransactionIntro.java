
package com.tightdb.refdoc;

import com.tightdb.*;

public class WriteTransactionIntro {

    public static void main(String[] args) {
        typedWriteTransactionIntro();
        dynamicWriteTransactionIntro();
    }

    // @@Example: ex_java_typed_write_transaction_intro @@
    // @@Show@@
    @DefineTable(table = "PeopleTable")
    class People {
        String name;
        int age;
    }
   
    public static void typedWriteTransactionIntro(){
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.tightdb");

        // Begin write transaction 
        WriteTransaction wt = group.beginWrite(); 
        try { 
            // Create table and add row with data
            PeopleTable people = new PeopleTable(wt);
            people.add("John Adams", 45);

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
            Table people = wt.getTable("people");
            people.addColumn(ColumnType.STRING, "name");
            people.addColumn(ColumnType.LONG, "age");
            people.add("John Adams", 45);

            // Close the transaction. All changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            // In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }
    }   // @@EndShow@@  
    // @@EndExample@@
} 
