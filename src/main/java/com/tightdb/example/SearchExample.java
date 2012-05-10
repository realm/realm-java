package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.example.generated.Person;
import com.tightdb.example.generated.PersonQuery;
import com.tightdb.example.generated.PersonTable;
import com.tightdb.example.generated.PersonView;
import com.tightdb.lib.TDBUtils;

public class SearchExample {

	public static void main(String[] args) {
		PersonTable persons = new PersonTable();

		Person john = persons.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Person johny = persons.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		Person nikolche = persons.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		TDBUtils.print(persons);

		// .salary.is(11) doesn't work
		PersonQuery q1 = persons.firstName.startsWith("J").lastName.endWith("e");
		System.out.println(q1);
		
		PersonView results = q1.findAll();
		System.out.println(results);
		
		TDBUtils.print(results);
		
		System.out.println("First names: " + Arrays.toString(results.firstName.getAll()));
		System.out.println("Salary sum: " + results.salary.sum());
		System.out.println("Salary min: " + results.salary.min());
		System.out.println("Salary max: " + results.salary.max());
		
		TDBUtils.print(results);
		
		results.clear();
		
		TDBUtils.print(persons);
		
		long count = persons.firstName.contains("iko").clear();
		System.out.println("Removed " + count + " rows!");
		
		TDBUtils.print(persons);
	}

}
