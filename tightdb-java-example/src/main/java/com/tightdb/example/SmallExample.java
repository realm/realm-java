package com.tightdb.example;

import java.util.Date;

import com.tightdb.*;
import com.tightdb.typed.TightDB;

public class SmallExample {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        EmployeeTable employees = new EmployeeTable();

        /****************************** BASIC OPERATIONS *****************************/

        Employee john = employees.add("John", "Doe", 10000, true,  new byte[] {1,2,3}, new Date(), "extra", null);
        Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] {2,3,4}, new Date(), true, null);
        Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234, null);

        TightDB.print("Employees", employees);
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
