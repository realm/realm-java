package com.tightdb.lib;

import static com.tightdb.test.EmployeesFixture.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;

public abstract class AbstractViewTest extends AbstractTest {
	protected EmployeeView employees;

	@BeforeMethod
	public void init() {
		EmployeeTable employeesTable = new EmployeeTable();

		addEmployee(employeesTable, EMPLOYEE[0]);
		addEmployee(employeesTable, EMPLOYEE[2]);
		insertEmployee(employeesTable, 1, EMPLOYEE[1]);

		employees = employeesTable.where().findAll();
	}

	@AfterMethod
	public void clear() {
		employees.clear();
	}

}
