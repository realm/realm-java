package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.example.generated.Person;
import com.tightdb.example.generated.PersonTable;
import com.tightdb.lib.TDBUtils;

public class ManualWorkingExample {

	public static void main(String[] args) {
		PersonTable persons = new PersonTable();

		Person john = persons.add("John", "Doe", 23000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Person nikolche = persons.insert(1, "Nikolche", "Mihajlovski", 28000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		System.out.println("first record: " + john);
		System.out.println("second record: " + nikolche);
		System.out.println("some column: " + john.driver);

		TDBUtils.print(persons);
		System.out.println("- First names: " + Arrays.toString(persons.firstName.getAll()));

		System.out.println("max salary: " + persons.salary.max());
		System.out.println("min salary: " + persons.salary.min());
		System.out.println("salary sum: " + persons.salary.sum());
		
		// promote all! :P
		persons.salary.setAll(100000);
		TDBUtils.print(persons);
		
		nikolche.lastName.set("MIHAJLOVSKI");
		
		persons.remove(0);

		TDBUtils.print(persons);

		persons.clear();

		TDBUtils.print(persons);
	}

}
