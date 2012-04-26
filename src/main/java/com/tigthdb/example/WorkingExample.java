package com.tigthdb.example;

import com.tigthdb.example.generated.Person;
import com.tigthdb.example.generated.PersonTable;
import com.tigthdb.lib.Table;

public class WorkingExample {

	// not finished
	@Table
	class personTable {
		String firstName;
		String lastName;
		int salary;
	}
	
	public static void main(String[] args) {
		PersonTable persons = new PersonTable();

		Person john = persons.add("John", "Doe", 23000);
		Person nikolche = persons.insert(1, "Nikolche", "Mihajlovski", 28000);

		System.out.println("first record: " + john);
		System.out.println("second record: " + nikolche);
		System.out.println("some column: " + john.firstName);

		display(persons);

		nikolche.lastName.set("MIHAJLOVSKI");
		persons.remove(0);

		display(persons);

		persons.clear();
		
		display(persons);
	}

	private static void display(PersonTable persons) {
		System.out.println(String.format("Displaying table %s:", persons.getName()));
		if (!persons.isEmpty()) {
			for (int i = 0; i < persons.size(); i++) {
				Person p = persons.at(i);
				System.out.println(" - " + p.firstName.get() + " " + p.getLastName() + " " + p.getSalary());
			}
		} else {
			System.out.println(" - the table is empty");
		}
	}

}
