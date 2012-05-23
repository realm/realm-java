package com.tightdb.example;

import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;

public class FailureExample {

	public static void main(String[] args) {

		EmployeeTable employees = new EmployeeTable();
		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");

		john.phones.get();
		john.phones.get(); // if this line is commented out, the failure disappears

		// Enable below to compare Tightdb performance against a Java ArrayList
		Performance.TestTightdb(250000);
	}

}
