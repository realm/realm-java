package com.tightdb.lib;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestEmployeeView;

@Test
public class ViewDataOperationsTest extends AbstractDataOperationsTest {

	TestEmployeeView employees;

	@Override
	protected AbstractRowset<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getEmployees() {
		return employees;
	}

	@BeforeMethod
	public void init() {
		TestEmployeeTable employeesTable = new TestEmployeeTable();

		employeesTable.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 },
				new Date(), "extra");
		employeesTable.add(NAME2, "B. Good", 10000, true,
				new byte[] { 1, 2, 3 }, new Date(), true);
		employeesTable.insert(1, NAME1, "Mihajlovski", 30000, false,
				new byte[] { 4, 5 }, new Date(), 1234);

		employees = employeesTable.where().findAll();
	}

}
