package com.tightdb.example;

import java.io.IOException;
import java.util.Date;

import com.tightdb.Group;
import com.tightdb.generated.EmployeeTable;

public class FailureExample {

	public static void main(String[] args) {
		Group group = new Group();
		EmployeeTable employees = new EmployeeTable(group);

		employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");

		employees.at(0).phones.get();

		try {
			group.writeToFile("employees.tdb");
		} catch (IOException e) {
			throw new RuntimeException("Couldn't save the data!", e);
		}

		employees.clear();

	}

}
