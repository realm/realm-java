package com.tightdb.lib;

import org.testng.annotations.Test;

import com.tightdb.generated.Employee;
import com.tightdb.test.EmployeesFixture;

public class CursorColumnsTest extends AbstractTableTest {

	@Test
	public void shouldGetCorrectColumnValues() throws IllegalAccessException {
		Employee employee0 = employees.first();
		checkCursor(EmployeesFixture.EMPLOYEE0, employee0);

		Employee employee1 = employees.at(1);
		checkCursor(EmployeesFixture.EMPLOYEE1, employee1);

		Employee employee2 = employee1.next();
		checkCursor(EmployeesFixture.EMPLOYEE2, employee2);
	}

	@Test
	public void shouldSetAndGetCorrectColumnValues() {
		Employee employee0 = employees.first();
		checkCursor(EmployeesFixture.EMPLOYEE0, employee0);

		updateEmployee(employee0, EmployeesFixture.EMPLOYEE2); // FIXME: CRASHES!
//		checkCursor(EmployeesFixture.EMPLOYEE2, employee0);
//
//		updateEmployee(employee0, EmployeesFixture.EMPLOYEE1);
//		checkCursor(EmployeesFixture.EMPLOYEE1, employee0);
//		checkCursor(EmployeesFixture.EMPLOYEE1, employees.first());
	}

}
