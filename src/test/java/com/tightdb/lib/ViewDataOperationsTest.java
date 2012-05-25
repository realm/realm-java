package com.tightdb.lib;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;

@Test
public class ViewDataOperationsTest extends AbstractDataOperationsTest {

	EmployeeView employees;

	@Override
	protected AbstractRowset<Employee, EmployeeView, EmployeeQuery> getEmployees() {
		return employees;
	}

	@BeforeMethod
	public void init() {
		EmployeeTable employeesTable = new EmployeeTable();

		employeesTable.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		employeesTable.add(NAME2, "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		employeesTable.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);

		employees = employeesTable.where().findAll();
	}

}
