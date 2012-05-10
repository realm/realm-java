package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.lib.NestedTable;
import com.tightdb.lib.TDBUtils;
import com.tightdb.lib.Table;

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

		System.out.println("first record: " + john);
		System.out.println("second record: " + nikolche);
		System.out.println("some column: " + john.firstName);

		john.phones.get().add("mobile", "123456");
		john.phones.get().add("home", "567890");

		nikolche.phones.get().add("home", "13579");

		System.out.println(john.phones.get().size());

		TDBUtils.print(persons);
		System.out.println("- First names: " + Arrays.toString(persons.firstName.getAll()));
		
		System.out.println("max salary: " + persons.salary.max());
		System.out.println("min salary: " + persons.salary.min());
		System.out.println("salary sum: " + persons.salary.sum());
		
		persons.salary.setAll(100000);
		TDBUtils.print(persons);
		
		nikolche.lastName.set("MIHAJLOVSKI");
		persons.remove(0);

		TDBUtils.print(persons);

		persons.clear();

		TDBUtils.print(persons);
	}

}
