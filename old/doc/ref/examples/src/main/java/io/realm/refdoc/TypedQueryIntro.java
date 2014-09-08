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

// @@Example: ex_java_typed_query_intro @@
package io.realm.refdoc;

import io.realm.*;

public class TypedQueryIntro {

    @DefineTable
    class Employee {
        String firstName;
        String lastName;
        int salary;
    }

    public static void main(String[] args) {
        // @@Show@@

        // Create a table with some data
        EmployeeTable employees = new EmployeeTable();
        employees.add("John", "Lee", 10000);
        employees.add("Jane", "Lee", 15000);
        employees.add("John", "Anderson", 20000);
        employees.add("Erik", "Lee", 30000);
        employees.add("Henry", "Anderson", 10000);

        // Find all employees with a first name of John.
        EmployeeView view = employees.firstName.equalTo("John").findAll();

        // Find the average salary of all employees with the last name Anderson.
        double avgSalary = employees.lastName.equalTo("Anderson").salary.average();
        System.out.println(avgSalary);

        // Find the total salary of all people named Jane and Erik.
        double salary = employees.where()
                        .group()
                           .lastName.equalTo("Jane")
                           .or()
                           .lastName.equalTo("Erik")
                        .endGroup()
                        .salary.sum();

        // Find all employees with a last name of Lee and a salary less than 25000.
        EmployeeView view2 = employees.lastName.equalTo("Lee").salary.lessThan(25000).findAll();

        // @@EndShow@@
    }
}
//@@EndExample@@
