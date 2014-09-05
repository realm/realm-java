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

import io.realm.*;

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
        SharedGroup group = new SharedGroup("mydatabase.realm");

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

        // Remember to close the shared group
        group.close();
    } // @@EndShow@@
    // @@EndExample@@


    // @@Example: ex_java_dyn_write_transaction_intro @@
    // @@Show@@
    public static void dynamicWriteTransactionIntro(){
        // Open existing database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.realm");

        // Begin write transaction
        WriteTransaction wt = group.beginWrite();
        try {
            // Create table, add columns and add row with data
            Table users = wt.getTable("users");
            users.addColumn(ColumnType.STRING, "username");
            users.addColumn(ColumnType.INTEGER, "level");
            users.add("tarzan", 45);

            // Close the transaction. All changes are written to the shared group
            wt.commit();
        } catch (Throwable t) {
            // In case of an error, rollback to close the transaction and discard all changes
            wt.rollback();
        }

        // Remember to close the shared group
        group.close();
    }   // @@EndShow@@
    // @@EndExample@@
}
