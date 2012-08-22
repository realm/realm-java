package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;
import java.util.Date;

import org.testng.annotations.Test;

import com.tightdb.Mixed;
import com.tightdb.example.generated.Employee;
import com.tightdb.example.generated.PhoneTable;
import com.tightdb.test.EmployeesFixture;

public class CursorColumnsTest extends AbstractTest {

	@Test
	public void shouldGetCorrectColumnValues() throws IllegalAccessException {
		Employee employee0 = employees.first();
		checkCursor(EmployeesFixture.EMPLOYEES[0], employee0);

		Employee employee1 = employees.at(1);
		checkCursor(EmployeesFixture.EMPLOYEES[1], employee1);

		Employee employee2 = employee1.next();
		checkCursor(EmployeesFixture.EMPLOYEES[2], employee2);
	}

	@Test
	public void shouldSetAndGetCorrectColumnValues() {
		Employee employee0 = employees.first();
		checkCursor(EmployeesFixture.EMPLOYEES[0], employee0);

		// FIXME: Fails with exception. You can't currently use
		// ByteBuffer.wrap(byte[]) for binary data - it creates a
		// "HeapByteBuffer"
		updateEmployee(employee0, EmployeesFixture.EMPLOYEES[2]);

		checkCursor(EmployeesFixture.EMPLOYEES[2], employee0);

		updateEmployee(employee0, EmployeesFixture.EMPLOYEES[1]);
		checkCursor(EmployeesFixture.EMPLOYEES[1], employee0);
		checkCursor(EmployeesFixture.EMPLOYEES[1], employees.first());
	}

	@Test
	public void shouldSetAndGetMixedValues() throws Exception {
		Employee employee = employees.first();

		employee.extra.set(true);
		assertEquals(true, employee.extra.get().getBooleanValue());

		byte[] arr = { 1, 3, 5 };
		employee.extra.set(arr);
		// FIXME: shouldn't be BINARY_TYPE_BYTE_ARRAY an expected type here?
		assertEquals(Mixed.BINARY_TYPE_BYTE_BUFFER, employee.extra.get().getBinaryType());
		assertEquals(ByteBuffer.wrap(arr), employee.extra.get().getBinaryValue());

		ByteBuffer buf = ByteBuffer.allocateDirect(3);
		byte[] arr2 = { 10, 20, 30 };
		buf.put(arr2);
		employee.extra.set(buf);
		assertEquals(Mixed.BINARY_TYPE_BYTE_BUFFER, employee.extra.get().getBinaryType());
		assertEquals(ByteBuffer.wrap(arr2), employee.extra.get().getBinaryValue());

		Date date = new Date(6547);
		employee.extra.set(date);
		assertEquals(date, employee.extra.get().getDateValue());

		long num = 135L;
		employee.extra.set(num);
		assertEquals(num, employee.extra.get().getLongValue());

		Mixed mixed = Mixed.mixedValue("mixed");
		employee.extra.set(mixed);
		assertEquals(mixed, employee.extra.get());

		employee.extra.set("abc");
		assertEquals("abc", employee.extra.get().getStringValue());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldntSetTableValue() {
		// the "set" operation is not supported yet for sub-table columns
		employees.first().phones.set(employees.last().phones.get());
	}

	public void shouldProvideReadableValue() {
		Employee employee = employees.first();
		
		assertNotNull(employee.firstName.getReadableValue());
		assertNotNull(employee.lastName.getReadableValue());
		assertNotNull(employee.salary.getReadableValue());
		assertNotNull(employee.driver.getReadableValue());
		assertNotNull(employee.photo.getReadableValue());
		assertNotNull(employee.birthdate.getReadableValue());
		assertNotNull(employee.extra.getReadableValue());
		assertNotNull(employee.phones.getReadableValue());
	}
	
}
