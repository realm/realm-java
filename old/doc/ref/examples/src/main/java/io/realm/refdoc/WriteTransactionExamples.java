package io.realm.refdoc;

import java.io.FileNotFoundException;
import io.realm.*;

public class WriteTransactionExamples {

    public static void main(String[] args) throws FileNotFoundException {
        transactionExample();
    }

    public static void transactionExample() {
        // @@Example: ex_java_write_transaction_rollback @@
        // @@Example: ex_java_write_transaction_commit @@
        // @@Show@@
        // Open an existing database file or create a
        // new database file and open as a shared group.
        SharedGroup group = new SharedGroup("mydatabase.realm");

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

        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }
}
