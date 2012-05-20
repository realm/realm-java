package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;
import com.tightdb.lib.TightDB;

public class SearchExample {

	public static void main(String[] args) {
		EmployeeTable Employees = new EmployeeTable();

		Employee john = Employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee johny = Employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		Employee nikolche = Employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		TightDB.print(Employees);

		// .salary.is(11) doesn't work
		EmployeeQuery q1 = Employees.firstName.startsWith("J").lastName.endWith("e");
		System.out.println(q1);
		
		EmployeeView results = q1.findAll();
		System.out.println(results);
		
		TightDB.print(results);
		
		System.out.println("First names: " + Arrays.toString(results.firstName.getAll()));
		System.out.println("Salary sum: " + results.salary.sum());
		System.out.println("Salary min: " + results.salary.minimum());
		System.out.println("Salary max: " + results.salary.maximum());
		
		TightDB.print(results);
		
		results.clear();
		
		TightDB.print(Employees);
		
		long count = Employees.firstName.contains("iko").clear();
		System.out.println("Removed " + count + " rows!");
		
		TightDB.print(Employees);
	}

}
