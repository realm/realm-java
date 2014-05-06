// @@Example: ex_java_typed_query_intro @@
package com.realm.refdoc;

import com.realm.*;

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
