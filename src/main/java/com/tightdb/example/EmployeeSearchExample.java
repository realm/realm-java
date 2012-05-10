package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;
import com.tightdb.lib.TDBUtils;

public class EmployeeSearchExample {

	public static void main(String[] args) {
		EmployeeTable employees = new EmployeeTable();

		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		TDBUtils.print(employees);

		// .salary.is(11) doesn't work
		EmployeeQuery q1 = employees.firstName.startsWith("J").lastName.endWith("e");
		System.out.println(q1);

		EmployeeView results = q1.findAll();
		System.out.println(results);

		TDBUtils.print(results);

		System.out.println("First names: " + Arrays.toString(results.firstName.getAll()));
		System.out.println("Salary sum: " + results.salary.sum());
		System.out.println("Salary min: " + results.salary.min());
		System.out.println("Salary max: " + results.salary.max());

		TDBUtils.print(results);

		results.clear();

		TDBUtils.print(employees);

		long count = employees.firstName.contains("iko").clear();
		System.out.println("Removed " + count + " rows!");

		TDBUtils.print(employees);
	}

}
