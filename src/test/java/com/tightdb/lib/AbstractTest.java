package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import com.tightdb.Mixed;
import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.test.EmployeeData;

public abstract class AbstractTest {

	private static final String[] EXPECTED_COLUMNS = { "firstName", "lastName", "salary", "driver", "photo", "birthdate", "extra", "phones" };

	protected void addEmployee(EmployeeTable employees, EmployeeData emp) {
		employees.add(emp.firstName, emp.lastName, emp.salary, emp.driver, emp.photo, emp.birthdate, emp.extra);
	}

	protected void insertEmployee(EmployeeTable employees, long pos, EmployeeData emp) {
		employees.insert(pos, emp.firstName, emp.lastName, emp.salary, emp.driver, emp.photo, emp.birthdate, emp.extra);
	}
	
	protected void updateEmployee(Employee employee, EmployeeData data) {
		employee.firstName.set(data.firstName);
		employee.lastName.set(data.lastName);
		employee.salary.set(data.salary);
		employee.driver.set(data.driver);
		employee.photo.set(ByteBuffer.wrap(data.photo));
		employee.birthdate.set(data.birthdate);
		employee.extra.set(Mixed.mixedValue(data.extra));
	}
	
	protected void checkCursorValues(EmployeeData expected, Employee employee) {
		try {
			assertEquals(expected.firstName, employee.firstName.get());
			assertEquals(expected.lastName, employee.lastName.get());
			assertEquals(expected.salary, employee.salary.get().longValue());
			assertEquals(expected.driver, employee.driver.get().booleanValue());
			assertEquals(ByteBuffer.wrap(expected.photo), employee.photo.get());
			assertEquals(expected.birthdate, employee.birthdate.get());
			assertEquals(Mixed.mixedValue(expected.extra), employee.extra.get());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void checkCursorColumns(Employee employee) {
		try {
			AbstractColumn<?, ?, ?, ?>[] columns = employee.columns();
			assertEquals(EXPECTED_COLUMNS.length, columns.length);

			for (int i = 0; i < columns.length; i++) {
				AbstractColumn<?, ?, ?, ?> column = columns[i];
				assertEquals(EXPECTED_COLUMNS[i], column.getName());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void checkCursor(EmployeeData expected, Employee employee) {
		checkCursorValues(expected, employee);
		checkCursorColumns(employee);
	}

}
