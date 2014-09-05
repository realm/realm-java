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

public class NestedTablesExample {

    public static void main(String[] args) {
        EmployeeTable employees = new EmployeeTable();

        Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra", null);
        Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true, null);
        Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234, null);

        PhoneTable p = john.getPhones();
        p.add("mobile", "111");
        john.getPhones().add("home", "222");

        johny.getPhones().add("mobile", "333");

        nikolche.getPhones().add("mobile", "444");
        nikolche.getPhones().add("work", "555");

        System.out.println(employees);
        for (PhoneTable phoneTable : employees.phones.getAll()) {
            System.out.println(phoneTable);
        }

    }

}
