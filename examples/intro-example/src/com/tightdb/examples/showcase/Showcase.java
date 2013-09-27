package com.tightdb.examples.showcase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import com.tightdb.Group;
import com.tightdb.DefineTable;
import com.tightdb.typed.AbstractColumn;

public class Showcase {

    public static void main(String[] args) {
        showLongExample();
    }

    /******************************************************************/
    /* Example of simple TightDB operations using highlevel interface */
    /******************************************************************/

    /*
     * Define a table like below and name it in lowercase. A class will be
     * generated with first letter uppercase: Employee. Employee is a cursor to
     * rows in the EmployeeTable, which will also be generated.
     */

    @DefineTable(row="Employee")
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

    @DefineTable(row="Phone")
    class phone {
        String type;
        String number;
    }

    @SuppressWarnings("unused")
    public static void showLongExample() {
        Group group = new Group();
        EmployeeTable employees = new EmployeeTable(group);

        /****************************** BASIC OPERATIONS *****************************/

        Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra", null);
        Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true, null);
        Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234, null);

        System.out.println(employees);
        System.out.println(johny);

        System.out.println("first record: " + john);
        System.out.println("second record: " + nikolche);
        System.out.println("some column: " + john.getFirstName());

        /****************************** GETTERS AND SETTERS *****************************/

        // Get value
        System.out.println("name2: " + john.getFirstName());

        // Set value
        employees.get(2).setLastName("NewName");

        /****************************** MANIPULATION OF ALL RECORDS *****************************/

        Employee is17 = employees.salary.equal(17).findFirst();
        System.out.println("**************** Salary 17?: "+ is17);
        if (is17 == null)
            System.out.println("null - (Correct.))");

        Employee is30000 = employees.salary.equal(30000).findFirst();
        System.out.println("**************** With Salary 30000?: " + is30000);

        // using explicit OR
        System.out.println("Search example" + employees.firstName.equal("Johnny")
                .or().lastName.equal("Mihajlovski").findFirst());

        // using implicit AND
        System.out.println("Search example 2" + employees.firstName.eq("Johnny").lastName.startsWith("B").findLast());

        employees.firstName.eq("John").findLast().setSalary(30000);

        /****************************** ITERATION OF ALL RECORDS *****************************/

        // lazy iteration over the table
        for (Employee employee : employees) {
            System.out.println("iterating: " + employee);
        }

        /****************************** AGGREGATION *****************************/

        // aggregation of the salary
        System.out.println("max salary: " + employees.salary.maximum());
        System.out.println("min salary: " + employees.salary.minimum());
        System.out.println("salary sum: " + employees.salary.sum());

        /****************************** COMPLEX QUERY *****************************/

        System.out.println("Query 1" +
                employees
                .firstName.startsWith("Nik")
                .lastName.contains("vski")
                .or().firstName.eq("John")
                .findAll());

        System.out.println("Query 2a" +
                employees.firstName.startsWith("Nik")
                .group()
                    .lastName.contains("vski")
                    .or()
                    .firstName.eq("John")
                .endGroup()
                .findAll());

        System.out.println("Query 2b" +
                employees.where()
                .group()
                    .lastName.contains("vski")
                    .or()
                    .firstName.eq("John")
                .endGroup()
                .firstName.startsWith("Nik")
                .findAll());

        // lazy iteration over query
        EmployeeQuery employeesOnN = employees.firstName.startsWith("J");
        Employee employee;
        while ((employee = employeesOnN.findNext()) != null) {
            System.out.println("Employee starting with J: " + employee);
        }
        /****************************** MANIPULATION OF ALL RECORDS *****************************/

        System.out.println("- First names: " + Arrays.toString(employees.firstName.getAll()));

        employees.salary.setAll(100000);
        employees.firstName.contains("o").findAll().firstName.setAll("Bill");

        System.out.println(employees);

        /****************************** COLUMN RETRIEVAL *****************************/

        System.out.print("- Columns: ");
        for (AbstractColumn<?, ?, ?, ?> column : john.columns()) {
            System.out.print(column.getName() + "=" + column.getReadableValue() + " ");
        }
        System.out.println();

        /****************************** SUBTABLES *****************************/

        PhoneTable subtable = john.getPhones();
        subtable.add("mobile", "111");

        john.getPhones().add("mobile", "111");
        john.getPhones().add("home", "222");

        johny.getPhones().add("mobile", "333");

        nikolche.getPhones().add("mobile", "444");
        nikolche.getPhones().add("work", "555");

        for (PhoneTable phoneTable : employees.phones.getAll()) {
            System.out.println(phoneTable);
        }


        /*************************** CURSOR NAVIGATION ***************************/

        Employee p1 = employees.get(0).next();      // 2nd row
        Employee p2 = employees.last().previous();  // 2nd-last row
        Employee p3 = employees.first().after(2);   // 3rd row
        employees.last().before(2);                 // 3rd-last row

        /***************************** SAVE TO FILE ******************************/
        
        new File("employees.tightdb").delete();
        try {
            group.writeToFile("employees.tightdb");
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save the data!", e);
        }

        /****************************** DATA REMOVAL *****************************/

        employees.remove(0);

        System.out.println(employees);

        employees.clear();

        employees.firstName.eq("ff").findAll().salary.minimum();

        System.out.println(employees);

        /**************************** LOAD FROM FILE *****************************/

        Group group2 = new Group("employees.tightdb");
        EmployeeTable employees2 = new EmployeeTable(group2);
        System.out.println(employees2);
        group2.close();
    }
}
