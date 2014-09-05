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

package io.tightdb.example;

import java.util.Date;

import io.realm.DefineTable;

public class SmallExample {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        EmployeeTable employees = new EmployeeTable();

        /****************************** BASIC OPERATIONS *****************************/

        Employee john = employees.add("John", "Doe", 10000, true,  new byte[] {1,2,3}, new Date(), "extra", null);
        Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] {2,3,4}, new Date(), true, null);
        Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234, null);

        System.out.println(employees.toString());
    }


    @DefineTable(row = "Employee")
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

    @DefineTable(row = "Phone")
    class phone {
        String type;
        String number;
    }

}
