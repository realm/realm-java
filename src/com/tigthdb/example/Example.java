package com.tigthdb.example;

import java.util.List;
import java.util.Map;

import com.tigthdb.example.generated.Person;
import com.tigthdb.example.generated.PersonTable;
import com.tigthdb.lib.Table;

public class Example {

	public static void main(String[] args) {

		@Table
		class phoneTable {
			String type;
			String number;
		}

		@Table
		class personTable {
			String firstName;
			String lastName;
			int salary;
			phoneTable phones;
		}

		PersonTable persons = new PersonTable();

		Person john = persons.add("John", "Doe", 23000);
		john.phones.add("home", "123456");
		john.phones.add("mobile", "333444");

		persons.insert(0, "Nikolche", "Mihajlovski", 28000);

		String name = persons.at(0).firstName.get();
		persons.at(1).lastName.set("NewName");

		persons.remove(0);

		Person johnDoe = persons.firstName.is("John").findUnique();

		List<Person> allRich = persons.salary.greaterThan(100000).findAll();

		// using explicit OR
		Person johnny = persons.firstName.is("Johnny").or().salary.is(10000).findFirst();

		// using implicit AND
		Person johnnyB = persons.firstName.is("Johnny").lastName.startsWith("B").findUnique();

		persons.firstName.is("John").findLast().salary.set(30000);

		List<Person> nikolches = persons.firstName.is("Nikolche").findAll();

		// projection and aggregation of the salary
		int salarySum = persons.salary.sum();

		// lazy iteration through the table - now simpler
		for (Person person : persons) {
			person.salary.set(50000);
		}
		
		// using lazy list of results - as moving a cursor through a view
		for (Person person : persons.salary.greaterThan(123).findAll()) {
			
			System.out.println(person);
		}
		// TODO: view.salary.max();

		// Various combinations:
		
		int sum = persons.firstName.is("X").or().salary.is(5).salary.sum();
		persons.firstName.is("Y").salary.is(6).lastName.set("Z");
		persons.salary.greaterThan(1234).remove();

		// TODO: 
		/*
		for (String phone : persons.phones.type.is("mobile").findAll().phone.all()) {
			System.out.println(phone);
		}
		*/
	}

}
