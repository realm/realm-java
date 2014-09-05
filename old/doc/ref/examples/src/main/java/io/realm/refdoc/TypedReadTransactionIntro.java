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

package io.realm.refdoc;
// @@Example: ex_java_typed_read_transaction_intro @@

import java.io.File;
import io.realm.*;

public class TypedReadTransactionIntro {

    public static void main(String[] args) {
        {
            // Delete file to start from scratch
            (new File("mydatabase.realm")).delete();
            (new File("mydatabase.realm.lock")).delete();
            // Create table, add columns and add row with data
            SharedGroup group = new SharedGroup("mydatabase.realm");
            WriteTransaction wt = group.beginWrite();
            try {
                Table users = wt.getTable("PeopleTable");
                users.addColumn(ColumnType.STRING, "username");
                users.addColumn(ColumnType.INTEGER, "level");
                users.add("tarzan", 45);
                wt.commit();
            } catch(Throwable t){
                wt.rollback();
            }
        }

        typedReadTransactionIntro();
    }

    // @@Show@@
    public static void typedReadTransactionIntro() {
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.realm");

        // Create read transaction from the shared group
        ReadTransaction rt = group.beginRead();

        // Inside transaction is a fully consistent and immutable view of the group
        try {
            // Get a table from the group
            PeopleTable people = new PeopleTable(rt);

            // Read from the first row, the name column
            String name = people.get(0).getName();

            // Do more table read operations here...

        } finally {
            // End the read transaction in a finally block. If the read-transaction is not
            // closed, a new one cannot be started using the same SharedGroup instance.
            rt.endRead();
        }

        // Remember to close the shared group
        group.close();
    } // @@EndShow@@

}
// @@EndExample@@
