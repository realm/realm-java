package com.tightdb.refdoc;

public class TypedQueryExamples {

    public static void main(String[] args) {
        findFirstExample();
        findFromExample();
        findAllExample();
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
        long janePosition = jane.getPosition();
        EmployeeRow res2 = employees.where().salary.lessThan(15000).findFrom(janePosition+1);
        Assert(res2.getFirstName().equals("Henry"));
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
        EmployeeView view = employees.where().salary.greaterThan(15000).findAll();
        Assert(view.size() == 2);

        // Show all matching employees
        for (EmployeeRow employee : view) {
            System.out.println(employee.getFirstName() + " earns " + employee.getSalary());
        }
    //@@EndExample@@
    }


    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
}
