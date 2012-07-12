package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;
import java.util.Iterator;

import com.tightdb.Mixed;
import com.tightdb.example.generated.Employee;
import com.tightdb.example.generated.EmployeeTable;
import com.tightdb.example.generated.Phone;
import com.tightdb.test.EmployeeData;
import com.tightdb.test.PhoneData;

public abstract class AbstractTest {

	private static final String[] EXPECTED_COLUMNS = { "firstName", "lastName", "salary", "driver", "photo", "birthdate", "extra", "phones" };

	protected void addEmployee(EmployeeTable employees, EmployeeData emp) {
		Employee e = employees.add(emp.firstName, emp.lastName, emp.salary, emp.driver, emp.photo, emp.birthdate, emp.extra);
		addPhones(emp, e);
	}

	protected void insertEmployee(EmployeeTable employees, long pos, EmployeeData emp) {
		Employee e = employees.insert(pos, emp.firstName, emp.lastName, emp.salary, emp.driver, emp.photo, emp.birthdate, emp.extra);
		addPhones(emp, e);
	}

	private void addPhones(EmployeeData emp, Employee e) {
		for (PhoneData phone : emp.phones) {
			e.phones.get().add(phone.type, phone.number);
		}
	}

	protected void updateEmployee(Employee employee, EmployeeData data) {
		employee.firstName.set(data.firstName);
		employee.lastName.set(data.lastName);
		employee.salary.set(data.salary);
		employee.driver.set(data.driver);
		// FIXME: NOTE: This is just a hack. photo.set should take a byte[] as
		// parameter.
		// using wrap() doesn't create a Direct allocated buffer as expected.
		ByteBuffer buf = ByteBuffer.allocateDirect(data.photo.length);
		buf.put(data.photo);
		employee.photo.set(buf);
		// employee.photo.set(ByteBuffer.wrap(data.photo));
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

	protected void checkCursorValues(PhoneData expected, Phone phone) {
		assertEquals(expected.type, phone.type.get());
		assertEquals(expected.number, phone.number.get());
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

	protected void checkIterator(Iterator<Employee> it, EmployeeData[] employeeData) {
		for (int i = 0; i < employeeData.length; i++) {
			checkIteratorOnRemove(it);
			assertTrue(it.hasNext());
			checkCursorValues(employeeData[i], it.next());
		}
		checkIteratorOnRemove(it);
	}
	
	private void checkIteratorOnRemove(Iterator<?> it) {
		try {
			it.remove();
		} catch (UnsupportedOperationException e) {
			return;
		}
		fail("Expected unsopported 'remove' operation!");
	}

	protected void checkIterator(Iterator<Phone> it, PhoneData[] phoneData) {
		for (int i = 0; i < phoneData.length; i++) {
			checkIteratorOnRemove(it);
			assertTrue(it.hasNext());
			checkCursorValues(phoneData[i], it.next());
		}
		checkIteratorOnRemove(it);
	}

}
