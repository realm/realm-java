package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;
import com.tightdb.lib.NestedTable;
import com.tightdb.lib.Table;
import com.tightdb.lib.TightDB;

public class WorkingExample {

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

	public static void main(String[] args) {
		EmployeeTable persons = new EmployeeTable();

		Employee john = persons.add("John", "Doe", 23000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee nikolche = persons.insert(1, "Nikolche", "Mihajlovski", 28000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		// 2 ways to get the value
		String name1 = john.firstName.get();
		String name2 = persons.at(0).getFirstName();

		// 2 ways to set the value
		persons.at(1).lastName.set("NewName");
		persons.at(1).setLastName("NewName");

		Employee johnDoe = persons.firstName.is("John").findUnique();
		EmployeeView allRich = persons.salary.greaterThan(100000).findAll();

		// using explicit OR
		Employee johnny = persons.firstName.is("Johnny").or().salary.is(10000).findFirst();

		// using implicit AND
		Employee johnnyB = persons.firstName.is("Johnny").lastName.startsWith("B").findLast();

		persons.firstName.is("John").findLast().salary.set(30000);

		persons.remove(0);

		System.out.println("first record: " + john);
		System.out.println("second record: " + nikolche);
		System.out.println("some column: " + john.firstName);

		// aggregation of the salary
		long salarySum = persons.salary.sum();

		// lazy iteration over the table
		for (Employee employee : persons) {
			employee.salary.set(50000);
		}

		for (Employee employee : persons) {
			System.out.println("iterating: " + employee);
		}

		john.phones.get().add("mobile", "123456");
		john.phones.get().add("home", "567890");

		nikolche.phones.get().add("home", "13579");

		System.out.println("John phones count: " + john.phones.get().size());

		TightDB.print(persons);
		System.out.println("- First names: " + Arrays.toString(persons.firstName.getAll()));

		System.out.println("max salary: " + persons.salary.max());
		System.out.println("min salary: " + persons.salary.min());
		System.out.println("salary sum: " + persons.salary.sum());

		persons.salary.setAll(100000);
		TightDB.print(persons);

		nikolche.lastName.set("MIHAJLOVSKI");
		persons.remove(0);

		TightDB.print(persons);

		persons.clear();

		TightDB.print(persons);

		/***************** NOT YET IMPLEMENTED ******************/

		// from 2nd to 4th row
		EmployeeView view = persons.range(2, 4);

		// cursor navigation
		Employee p1 = persons.at(4).next(); // 5nd row
		Employee p2 = persons.last().previous(); // 2nd-last row
		Employee p3 = persons.first().after(3); // 4th row
		Employee p4 = persons.last().before(2); // 3rd-last row

	}

}
