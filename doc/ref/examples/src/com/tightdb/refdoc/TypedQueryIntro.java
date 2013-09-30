// @@Example: ex_java_typed_query_intro @@
package com.tightdb.refdoc;

import com.tightdb.*;

public class TypedQueryIntro {

    @DefineTable
    class Employee {
        String firstName;
        String lastName;
        int salary;
    }

    public static void main(String[] args) {

        EmployeeTable employees = new EmployeeTable();

        // Add data to the table
        employees.add("John", "Lee", 10000);
        employees.add("Jane", "Lee", 15000);
        employees.add("John", "Anderson", 20000);
        employees.add("Erik", "Lee", 30000);
        employees.add("Henry", "Anderson", 10000);

        // View definition, will hold the result of our queries
        EmployeeView view;

        // Find all employees with a first name of John.
        view = employees.firstName.equal("John").findAll();

        // Find the average salary of all employees with the last name Anderson.
        double avgSalary = employees.lastName.equal("Anderson").salary.average();
        System.out.println(avgSalary);

        // Find the total salary of people named Jane and Erik.
        double salary = employees.where().
                        group().lastName.equal("Jane").or().lastName.equal("Erik")
                        .endGroup().salary.sum();

        // Find all employees with a last name of Lee and a salary less than 25000.
        view = employees.lastName.equal("Lee").salary.lessThan(25000).findAll();
    }
}
//@@EndExample@@
