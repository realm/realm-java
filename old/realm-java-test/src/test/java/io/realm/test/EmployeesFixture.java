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

package io.realm.test;

import java.util.Date;

public class EmployeesFixture {

    public static final PhoneData[][] PHONES = {
        { new PhoneData("home", "123") },
        { new PhoneData("mobile", "456") },
        { new PhoneData("work", "789"), new PhoneData("mobile", "012") }
    };

    public static final EmployeeData[] EMPLOYEES = { new EmployeeData("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(111111), "extra", PHONES[0]),
            new EmployeeData("Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(2222), 1234, PHONES[1]),
            new EmployeeData("Johny", "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(333343333), true, PHONES[2]) };

    public static Object[] getAll(int index) {
        Object[] values = new Object[EMPLOYEES.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = EMPLOYEES[i].get(index);
        }

        return values;
    }

}
