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
     // @@Example: ex_java_typed_query_intro @@

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
      //@@EndExample@@
    }

    public static void findFirstExample() {
    // @@Example: ex_java_typed_query_find_first @@
        // Create a new table
        EmployeeTable employees = new EmployeeTable();
        employees.add("John", "Lee", 10000);
        employees.add("Jane", "Lee", 15000);
        employees.add("John", "Anderson", 20000);
        employees.add("Erik", "Lee", 30000);
        employees.add("Henry", "Anderson", 10000);

        // Search for a (non-existent) employee
        EmployeeRow res1 = employees.where().firstName.equalTo("Susan").findFirst();
        Assert(res1 == null); // not found

        // Search for an existing employee
        EmployeeRow res2 = employees.where().firstName.equalTo("Erik").findFirst();
        Assert(res2.getSalary() == 30000);
    //@@EndExample@@
    }

    public static void findFromExample() {
    // @@Example: ex_java_typed_query_find_from @@
        // Create a new table
        EmployeeTable employees = new EmployeeTable();
        employees.add("John", "Lee", 10000);
        employees.add("Jane", "Lee", 15000);
        employees.add("John", "Anderson", 20000);
        employees.add("Erik", "Lee", 30000);
        employees.add("Henry", "Anderson", 10000);

        // Search for a employee named Jane
        EmployeeRow jane = employees.where().firstName.equalTo("Jane").findFirst();

        // Find first employee after Jane with a salary below 15000
        long jane_pos = jane.getPosition();
        EmployeeRow res2 = employees.where().salary.lessThan(15000).findFrom(jane_pos+1);
        Assert(res2.getfirstName() == "Henry");
    //@@EndExample@@
    }

    public static void findAllExample() {
    // @@Example: ex_java_typed_query_find_all @@
        // Create a new table
        EmployeeTable employees = new EmployeeTable();
        employees.add("John", "Lee", 10000);
        employees.add("Jane", "Lee", 15000);
        employees.add("John", "Anderson", 20000);
        employees.add("Erik", "Lee", 30000);
        employees.add("Henry", "Anderson", 10000);

        // Search for a all employees with a given salary
        EmployeeView view = employees.where().salary.greaterThan("15000").findAll();
        Assert(view.size() == 2);

        // Show all matching employees
        for (EmployeeRow employee : view) {
            System.out.println(employee.getFirstName() + " earns " + employee.getSalary());
        }
    //@@EndExample@@
    }
}
