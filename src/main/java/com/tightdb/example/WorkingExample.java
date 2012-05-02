package com.tightdb.example;

import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
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
	}

	public static void main(String[] args) {
		EmployeeTable persons = new EmployeeTable();

		Employee john = persons.add("John", "Doe", 23000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee nikolche = persons.insert(1, "Nikolche", "Mihajlovski", 28000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

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

	private static void display(EmployeeTable persons) {
		System.out.println(String.format("Displaying table %s:", persons.getName()));
		if (!persons.isEmpty()) {
			for (int i = 0; i < persons.size(); i++) {
				Employee p = persons.at(i);
				System.out.println(" - " + p.firstName.get() + " " + p.getLastName() + " " + p.getSalary() + " " + p.getDriver() + " " + p.getPhoto()
						+ " " + p.getBirthdate() + " " + p.getExtra());
			}
		} else {
			System.out.println(" - the table is empty");
		}
	}

}
