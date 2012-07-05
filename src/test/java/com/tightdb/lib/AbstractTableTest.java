package com.tightdb.lib;

import static com.tightdb.test.EmployeesFixture.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.tightdb.generated.EmployeeTable;

public abstract class AbstractTableTest extends AbstractTest {

	protected EmployeeTable employees;

	@BeforeMethod
	public void init() {
		employees = new EmployeeTable();

		addEmployee(employees, EMPLOYEE[0]);
		addEmployee(employees, EMPLOYEE[2]);
		insertEmployee(employees, 1, EMPLOYEE[1]);
	}

	@AfterMethod
	public void clear() {
		employees.clear();
	}

}
