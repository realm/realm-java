package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;
import com.tightdb.generated.PhoneTable;
import com.tightdb.lib.AbstractColumn;
import com.tightdb.lib.NestedTable;
import com.tightdb.lib.Table;
import com.tightdb.lib.TightDB;

public class Example {

	public static void main(String[] args) {
		int rowArg = 250000;
		if (args.length > 0) {
		    try {
		        rowArg = Integer.parseInt(args[0]);
		    } catch (NumberFormatException e) {
		        System.err.println("Argument" + " must be an integer");
		        System.exit(1);
		    }
		}
		
		Performance.TestTightdb(250000);
		Performance.TestJavaArray(rowArg);
		
		//showExample();
	}
	
	@Table
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

	@NestedTable
	class phone {
		String type;
		String number;
	}

	public static void showExample() {
		EmployeeTable employees = new EmployeeTable();
		
		/****************************** BASIC OPERATIONS *****************************/

		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		TightDB.print("Employees", employees);

		TightDB.print("Johny", johny);

		System.out.println("first record: " + john);
		System.out.println("second record: " + nikolche);
		System.out.println("some column: " + john.firstName);

		/****************************** GETTERS AND SETTERS *****************************/

		// 2 ways to get the value
		System.out.println("name1: " + john.firstName.get());
		System.out.println("name2: " + employees.at(0).getFirstName());

		// 2 ways to set the value
		employees.at(2).lastName.set("NewName");
		employees.at(2).setLastName("NewName");

		Employee niko = employees.firstName.startsWith("Nik").findUnique();
		System.out.println("Unique Niko: " + niko);

		/****************************** MANIPULATION OF ALL RECORDS *****************************/

		Employee ny = employees.salary.is(17).findFirst();
		TightDB.print("**************** Findes 17?: ", ny);
		if (ny==null)
			System.out.println("NOPE!!)");
					
		Employee ny2 = employees.salary.is(30000).findFirst();
		TightDB.print("**************** Findes 30000?: ", ny2);
		
		// using explicit OR
		TightDB.print("Search example", employees.firstName.is("Johnny").or().lastName.is("Mihajlovski").findFirst());

		// using implicit AND
		TightDB.print("Search example 2", employees.firstName.is("Johnny").lastName.startsWith("B").findLast());

		employees.firstName.is("John").findLast().salary.set(30000);

		/****************************** ITERATION OF ALL RECORDS *****************************/

		// lazy iteration over the table
		for (Employee employee : employees) {
			System.out.println("iterating: " + employee);
		}

		john.phones.get().add("mobile", "123456");
		john.phones.get().add("home", "567890");

		nikolche.phones.get().add("home", "13579");

		System.out.println("John phones count: " + john.phones.get().size());

		TightDB.print("Employees", employees);

		/****************************** AGGREGATION *****************************/

		// aggregation of the salary
		System.out.println("max salary: " + employees.salary.max());
		System.out.println("min salary: " + employees.salary.min());
		System.out.println("salary sum: " + employees.salary.sum());

		/****************************** COMPLEX QUERY *****************************/

		TightDB.print("Query 1", employees.firstName.startsWith("Nik").lastName.contains("vski").or().firstName.is("John").findAll());

		TightDB.print("Query 2a", employees.firstName.startsWith("Nik").
									startGroup().lastName.contains("vski").or().firstName.is("John").endGroup()
									.findAll());

		TightDB.print("Query 2b",
				employees.query().startGroup().lastName.contains("vski").or().firstName.is("John").endGroup().firstName.startsWith("Nik").findAll());

		// lazy iteration over query (use find_next() )
//		for (Employee employee : employees.firstName.startsWith("N")) {
						
//		}
		/****************************** MANIPULATION OF ALL RECORDS *****************************/

		System.out.println("- First names: " + Arrays.toString(employees.firstName.getAll()));

		employees.salary.setAll(100000);

		TightDB.print(employees);

		/****************************** COLUMN RETRIEVAL *****************************/

		System.out.print("- Columns:");
		for (AbstractColumn<?, ?, ?> column : john.columns()) {
			System.out.print(column.getName() + " ");
		}
		System.out.println();

		/****************************** NESTED TABLES *****************************/

		john.getPhones().add("mobile", "111");
		john.getPhones().add("home", "222");

		johny.getPhones().add("mobile", "333");

		nikolche.getPhones().add("mobile", "444");
		nikolche.getPhones().add("work", "555");

		TightDB.print(employees);

		for (PhoneTable phoneTable : employees.phones.getAll()) {
			TightDB.print(phoneTable);
		}

		/****************************** DATA REMOVAL *****************************/

		employees.remove(0);

		TightDB.print(employees);

		employees.clear();

		TightDB.print(employees);

		/****************************** NOT IMPLEMENTED YET *****************************/

		try {
			// from 2nd to 4th row
			EmployeeView view = employees.range(2, 4);

			// cursor navigation
			Employee p1 = employees.at(4).next(); // 5nd row
			Employee p2 = employees.last().previous(); // 2nd-last row
			Employee p3 = employees.first().after(3); // 4th row
			Employee p4 = employees.last().before(2); // 3rd-last row
		} catch (Exception e) {
		}
	}
}
