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

/*package com.tightdb.example;

import java.util.ArrayDeque;
import java.util.Date;

import com.tightdb.*;

public class ReplicationExample {

    @DefineTable(row="Employee")
    class employee {
        String firstName;
        String lastName;
        int salary;
        boolean driver;
        byte[] photo;
        Date birthdate;
        Object extra;
        phone phones;
    }

    @DefineTable(row="Phone")
    class phone {
        String type;
        String number;
    }

    public static void main(String[] args)
    {
        String databaseFile = SharedGroupWithReplication.getDefaultDatabaseFileName();

        ArrayDeque<String> positionalArgs = new ArrayDeque<String>();
        boolean error = false;
        for (int i=0; i<args.length; ++i) {
            String arg = args[i];
            if (arg.length() < 2 || !arg.substring(0,2).equals("--")) {
                positionalArgs.addLast(arg);
                continue;
            }

            if (arg.equals("--database-file")) {
                if (i+1 < args.length) {
                    databaseFile = args[++i];
                    continue;
                }
            }
            error = true;
            break;
        }
        if (error || positionalArgs.size() != 0) {
            System.err.println(
                "ERROR: Bad command line.\n\n" +
                "Synopsis: java com.tightdb.example.ReplicationExample\n\n" +
                "Options:\n" +
                "  --database-file STRING   (default: \""+databaseFile+"\")");
            System.exit(1);
        }

        SharedGroup db = new SharedGroupWithReplication(databaseFile);
        try {
            WriteTransaction transact = db.beginWrite();
            try {
                EmployeeTable employees = new EmployeeTable(transact);
                employees.add("John", "Doe", 10000, true,
                              new byte[] { 1, 2, 3 }, new Date(), "extra", null);
                System.out.println(employees.size());
                transact.commit();
            } catch (Throwable e) {
                transact.rollback();
                throw new RuntimeException(e);
            }
        } finally {
            db.close();
        }
    }
}*/
