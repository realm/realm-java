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

import java.io.File;
import java.io.FileNotFoundException;
import io.realm.*;

public class ReadTransactionExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        endReadExample();
    }

    public static void endReadExample(){
        // @@Example: ex_java_shared_group_end_read @@
        {
            // Delete file to start from scratch
            (new File("mydatabase.realm")).delete();
            (new File("mydatabase.realm.lock")).delete();
            // Create table, add columns and add row with data
            SharedGroup group = new SharedGroup("mydatabase.realm");
            WriteTransaction wt = group.beginWrite();

            try{
                Table users = wt.getTable("myTable");
                users.addColumn(ColumnType.STRING, "username");
                users.addColumn(ColumnType.INTEGER, "level");
                users.add("tarzan", 45);
                wt.commit();
            } catch (Throwable t){
                wt.rollback();
            }

            // Remember to close the shared group
            group.close();
        }

        // @@Show@@
        // Open database file in a shared group
        SharedGroup group = new SharedGroup("mydatabase.realm");

        // Start read transaction
        ReadTransaction rt = group.beginRead();

        // Always do try / finally when using read transactions
        try {
            if (rt.hasTable("myTable")) {
                Table table = rt.getTable("myTable");
                // More table read operations here....
            }
        } finally {
            rt.endRead(); // End read transaction in finally
        }

        // Remember to close the shared group
        group.close();
        // @@EndShow@@
        // @@EndExample@@
    }
}
