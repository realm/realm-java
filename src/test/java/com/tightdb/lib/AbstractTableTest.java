package com.tightdb.lib;

import static com.tightdb.test.EmployeesFixture.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.tightdb.generated.EmployeeTable;

public abstract class AbstractTableTest {

	protected EmployeeTable employees;

	@BeforeMethod
	public void init() {
		employees = new EmployeeTable();

		employees.add(FIRST_NAMES[0], LAST_NAMES[0], SALARIES[0], DRIVERS[0], PHOTOS[0], BIRTHDATES[0], EXTRAS[0]);
		employees.add(FIRST_NAMES[2], LAST_NAMES[2], SALARIES[2], DRIVERS[2], PHOTOS[2], BIRTHDATES[2], EXTRAS[2]);
		employees.insert(1, FIRST_NAMES[1], LAST_NAMES[1], SALARIES[1], DRIVERS[1], PHOTOS[1], BIRTHDATES[1], EXTRAS[1]);
	}

	@AfterMethod
	public void clear() {
		employees.clear();
	}

}
