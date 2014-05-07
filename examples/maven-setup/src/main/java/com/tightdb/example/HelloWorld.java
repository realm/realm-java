package io.realm.example;

import java.util.Date;

import io.realm.DefineTable;
import io.realm.typed.TightDB;

public class HelloWorld {

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

    public static void main(String[] args) {
        EmployeeTable employees = new EmployeeTable();
        employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra", null);
        TightDB.print("Employees", employees);
    }

}
