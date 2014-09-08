/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// @@Example: ex_java_shared_group_intro @@
package io.realm.refdoc;

import java.io.File;
import io.realm.*;

public class SharedGroupIntro {

    public static void main(String[] args) {
        // Delete file to start from scratch
        (new File("mydatabase.realm")).delete();

        // @@Show@@
        // Opens an existing database file or creates a
        // new database file and opens it into a shared group.
        SharedGroup group = new SharedGroup("mydatabase.realm");

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


        // Remember to close the shared group
        group.close();
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
}
// @@EndExample@@
