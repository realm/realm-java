package com.tightdb.examples.showcase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import com.tightdb.Group;
import com.tightdb.Table;
import com.tightdb.lib.AbstractColumn;
import com.tightdb.lib.TightDB;

public class Showcase {

	public static void main(String[] args) {
		showLongExample();
	}

	/******************************************************************/
	/* Example of simple Tightdb operations using highlevel interface */
	/******************************************************************/

	/*
	 * Define a table like below and name it in lowercase. A class will be
	 * generated with first letter uppercase: Employee. Employee is a cursor to
	 * rows in the EmployeeTable, which will also be generated.
	 */
	
	@Table(row="Employee")
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

	@Table(row="Phone")
	class phone {
		String type;
		String number;
	}

	public static void showLongExample() {
		Group group = new Group();
		EmployeeTable employees = new EmployeeTable(group);

		/****************************** BASIC OPERATIONS *****************************/

		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);
		
		TightDB.print("Employees", employees);
		TightDB.print("Johny", johny);

		System.out.println("first record: " + john);
		System.out.println("second record: " + nikolche);
		System.out.println("some column: " + john.firstName);

		/****************************** GETTERS AND SETTERS *****************************/

		// 2 ways to get the value
		System.out.println("name1: " + john.firstName.get());
		System.out.println("name2: " + john.getFirstName());

		// 2 ways to set the value
		employees.at(2).lastName.set("NewName");
		employees.at(2).setLastName("NewName");

		/****************************** MANIPULATION OF ALL RECORDS *****************************/

		Employee is17 = employees.salary.equal(17).findFirst();
		TightDB.print("**************** Salary 17?: ", is17);
		if (is17 == null)
			System.out.println("No - (Correct.))");

		Employee is30000 = employees.salary.equal(30000).findFirst();
		TightDB.print("**************** With Salary 30000?: ", is30000);

		// using explicit OR
		TightDB.print("Search example", employees.firstName.equal("Johnny")
				.or().lastName.equal("Mihajlovski").findFirst());

		// using implicit AND
		TightDB.print("Search example 2", employees.firstName.eq("Johnny").lastName.startsWith("B").findLast());

		employees.firstName.eq("John").findLast().salary.set(30000);

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

		TightDB.print("Query 1", 
				employees
				.firstName.startsWith("Nik")
				.lastName.contains("vski")
				.or().firstName.eq("John")
				.findAll());

		TightDB.print("Query 2a", 
				employees.firstName.startsWith("Nik")
				.group()
					.lastName.contains("vski")
					.or()
					.firstName.eq("John")
				.endGroup()
				.findAll());
		
		TightDB.print("Query 2b", 
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
			TightDB.print("Employee starting with J: ", employee);
		}
		/****************************** MANIPULATION OF ALL RECORDS *****************************/

		System.out.println("- First names: " + Arrays.toString(employees.firstName.getAll()));

		employees.salary.setAll(100000);
		employees.firstName.contains("o").findAll().firstName.setAll("Bill");

		TightDB.print(employees);

		/****************************** COLUMN RETRIEVAL *****************************/

		System.out.print("- Columns: ");
		for (AbstractColumn<?, ?, ?, ?> column : john.columns()) {
			System.out.print(column.getName() + "=" + column.getReadableValue() + " ");
		}
		System.out.println();

		/****************************** SUBTABLES *****************************/

		PhoneTable subtable = john.phones.get();
		subtable.add("mobile", "111");

		john.getPhones().add("mobile", "111");
		john.getPhones().add("home", "222");

		johny.getPhones().add("mobile", "333");

		nikolche.getPhones().add("mobile", "444");
		nikolche.getPhones().add("work", "555");

		for (PhoneTable phoneTable : employees.phones.getAll()) {
			TightDB.print(phoneTable);
		}

		// convenience methods on the column:

		for (Phone phone : nikolche.phones) {
			TightDB.print("- phone", phone);
		}

		TightDB.print("- first phone", nikolche.phones.first());

		/*************************** CURSOR NAVIGATION ***************************/

		Employee p1 = employees.at(0).next(); 		// 2nd row
		Employee p2 = employees.last().previous(); 	// 2nd-last row
		Employee p3 = employees.first().after(2); 	// 3rd row
		employees.last().before(2); 				// 3rd-last row

		/***************************** SAVE TO FILE ******************************/

		try {
			group.writeToFile("employees.tdb");
		} catch (IOException e) {
			throw new RuntimeException("Couldn't save the data!", e);
		}

		/****************************** DATA REMOVAL *****************************/

		employees.remove(0);

		TightDB.print(employees);

		employees.clear();
		
		employees.firstName.eq("ff").findAll().salary.minimum();
		
		TightDB.print(employees);

		/**************************** LOAD FROM FILE *****************************/

		Group group2 = new Group("employees.tdb");
		EmployeeTable employees2 = new EmployeeTable(group2);
		TightDB.print(employees2);
	}
}
