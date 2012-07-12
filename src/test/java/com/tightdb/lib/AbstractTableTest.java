package com.tightdb.lib;

import static com.tightdb.test.EmployeesFixture.*;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.tightdb.example.generated.EmployeeTable;

public abstract class AbstractTableTest extends AbstractTest {

	protected EmployeeTable employees;

	@BeforeMethod
	public void init() {
		employees = new EmployeeTable();

		addEmployee(employees, EMPLOYEES[0]);
		addEmployee(employees, EMPLOYEES[2]);
		insertEmployee(employees, 1, EMPLOYEES[1]);
		assertEquals(3, employees.size());
	}

	@AfterMethod
	public void clear() {
		employees.clear();
		assertEquals(0, employees.size());
	}

}
