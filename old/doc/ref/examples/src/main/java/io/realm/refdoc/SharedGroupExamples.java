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

import java.io.FileNotFoundException;

import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.Table;
import io.realm.WriteTransaction;

public class SharedGroupExamples {

    public static void main(String[] args) throws FileNotFoundException  {

        constructorStringExample();
        beginWriteExample();
        beginReadExample();
        closeExample();
        hasChangedExample();
    }

    // **********************
    // Constructor methods
    // **********************

    public static void constructorStringExample(){
        // @@Example: ex_java_shared_group_constructor_string @@
        // @@Show@@
        // Instantiate group by specifying path to realm file
        SharedGroup group = new SharedGroup("mydatabase.realm");
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void beginWriteExample(){
        // @@Example: ex_java_shared_group_begin_write @@
        // @@Show@@
        SharedGroup group = new SharedGroup("mydatabase.realm");

        // Starts a write transaction
        WriteTransaction wt = group.beginWrite();

        // Use try / catch when using transactions
        try {
            Table table = wt.getTable("mytable");
            // Do table write operations on table here
            // ...

            wt.commit(); // Changes are saved to file, when commit() is called
        } catch (Throwable t){
            wt.rollback(); // If an error occurs, always rollback
        }

        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void beginReadExample(){
        // @@Example: ex_java_shared_group_begin_read @@
        // @@Show@@
        SharedGroup group = new SharedGroup("mydatabase.realm");

        // Starts a read transaction
        ReadTransaction rt = group.beginRead();

        // Use try / catch when using transactions
        try {
            Table table = rt.getTable("mytable"); // Table must exist in shared group
            // Do table operations on table here
            // ...

        } finally{
            rt.endRead(); // Always end read transaction in finally block
        }

        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void closeExample(){
        // @@Example: ex_java_shared_group_close @@
        // @@Show@@
        SharedGroup group = new SharedGroup("mydatabase.realm");

        // Starts a write transaction
        WriteTransaction wt = group.beginWrite();

        // Use try / catch when using transactions
        try {
            Table table = wt.getTable("mytable");
            // Do table write operations on table here
            // ...

            wt.commit(); // Changes are saved to file, when commit() is called
        } catch (Throwable t){
            wt.rollback(); // If an error occurs, always rollback
        }

        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void hasChangedExample(){
        // @@Example: ex_java_shared_group_has_changed @@
        // @@Show@@
        SharedGroup group = new SharedGroup("mydatabase.realm");

        boolean hasChanged = group.hasChanged();
        // @@EndShow@@
        // @@EndExample@@
    }
}
