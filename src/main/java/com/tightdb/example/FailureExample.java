package com.tightdb.example;

import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.PhoneTable;

public class FailureExample {

	public static void main(String[] args) {

		EmployeeTable employees = new EmployeeTable();
		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");

		// This works:
		PhoneTable tbl = john.phones.get();
		PhoneTable tbl2 = john.phones.get();
		PhoneTable tbl3 = john.phones.get();
		
		
		// and this works:
		john.phones.get();
		john.phones.get();
				
		john.phones.get();
		john.phones.get();

		// Enable below to compare Tightdb performance against a Java ArrayList
		Performance.TestTightdb(250000);
		
//		System.out.println(a);
//		System.out.println(b);
	}

}
