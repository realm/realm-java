package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

public class SearchExample {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        EmployeeTable Employees = new EmployeeTable();

        Employee john = Employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra", null);
        Employee johny = Employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true, null);
        Employee nikolche = Employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234, null);

        System.out.println(Employees);

        EmployeeQuery q1 = Employees.firstName.startsWith("J").lastName.endsWith("e");
        EmployeeView results = q1.findAll();

        System.out.println(results);

        System.out.println("First names: " + Arrays.toString(results.firstName.getAll()));
        System.out.println("Salary sum: " + results.salary.sum());
        System.out.println("Salary min: " + results.salary.minimum());
        System.out.println("Salary max: " + results.salary.maximum());
        System.out.println("Salary average: " + results.salary.average());

        System.out.println(results);

        results.clear();

        System.out.println(Employees);

        long count = Employees.firstName.contains("iko").clear();
        System.out.println("Removed " + count + " rows!");

        System.out.println(Employees);
    }

}
