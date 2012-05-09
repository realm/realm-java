package com.tightdb.example;

import java.util.Date;

import com.tightdb.example.generated.Person;
import com.tightdb.example.generated.PersonQuery;
import com.tightdb.example.generated.PersonTable;
import com.tightdb.example.generated.PersonView;
import com.tightdb.lib.TDBUtils;

public class SearchExample {

	public static void main(String[] args) {
		PersonTable persons = new PersonTable();

		Person john = persons.add("John", "Doe", 23000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Person john2 = persons.add("Johny", "Goe", 23000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Person nikolche = persons.insert(1, "Nikolche", "Mihajlovski", 28000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		TDBUtils.printTable(persons);

		// .salary.is(11) doesn't work
		PersonQuery q1 = persons.firstName.startsWith("J").lastName.endWith("e");
		System.out.println(q1);
		
		PersonView results = q1.findAll();
		System.out.println(results);
	}

}
