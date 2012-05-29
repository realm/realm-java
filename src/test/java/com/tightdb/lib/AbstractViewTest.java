package com.tightdb.lib;

import static org.testng.AssertJUnit.*;
import java.util.Date;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;

public abstract class AbstractViewTest {

	protected static final String NAME0 = "John";
	protected static final String NAME1 = "Nikolche";
	protected static final String NAME2 = "Johny";

	protected EmployeeView employees;

	@BeforeMethod
	public void init() {
		EmployeeTable employeesTable = new EmployeeTable();

		employeesTable.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		employeesTable.add(NAME2, "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		employeesTable.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);

		employees = employeesTable.where().findAll();
	}

	@AfterMethod
	public void clear() {
		employees.clear();
	}

}
