package com.tightdb.example;

import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.PhoneTable;
import com.tightdb.lib.TightDB;

public class NestedTablesExample {

	public static void main(String[] args) {
		EmployeeTable employees = new EmployeeTable();

		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		john.getPhones().add("mobile", "111");
		john.getPhones().add("home", "222");

		johny.getPhones().add("mobile", "333");
		
		nikolche.getPhones().add("mobile", "444");
		nikolche.getPhones().add("work", "555");
		
		TightDB.print(employees);

		for (PhoneTable phoneTable : employees.phones.getAll()) {
			TightDB.print(phoneTable);
		}

	}

}
