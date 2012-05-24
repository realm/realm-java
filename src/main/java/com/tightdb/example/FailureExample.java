package com.tightdb.example;

import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.PhoneTable;
import com.tightdb.lib.TightDB;

import com.tightdb.TableBase;

public class FailureExample {

	public static void main(String[] args) {
		
		EmployeeTable employees = new EmployeeTable();
		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");

		// This works:
		PhoneTable tbl = john.phones.get();
		tbl.add("test", "123");
		TightDB.print(tbl);
		
		PhoneTable tbl2 = john.phones.get();
		TightDB.print(tbl2);
		
		//john.phones.get().add("home", "222");
		//john.getPhones().add("home2", "333");
				
				
	}

}
