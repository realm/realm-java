package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.nio.ByteBuffer;
import java.util.Date;

import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Mixed;
import com.tightdb.test.EmployeesFixture;
import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeView;
import com.tightdb.typed.AbstractTableOrView;

public class CursorColumnsTest extends AbstractTest {

	@Test
	public void shouldGetCorrectColumnValues() throws IllegalAccessException {
		TestEmployeeRow employee0 = employees.first();
		checkCursor(EmployeesFixture.EMPLOYEES[0], employee0);

		TestEmployeeRow employee1 = employees.at(1);
		checkCursor(EmployeesFixture.EMPLOYEES[1], employee1);

		TestEmployeeRow employee2 = employee1.next();
		checkCursor(EmployeesFixture.EMPLOYEES[2], employee2);
	}

	@Test
	public void shouldSetAndGetCorrectColumnValues() {
		checkSetAndGetCorrectColumnValues(employees);
		checkSetAndGetCorrectColumnValues(employeesView);
	}

	private void checkSetAndGetCorrectColumnValues(
			AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> empls) {
		TestEmployeeRow employee0 = empls.first();
		checkCursor(EmployeesFixture.EMPLOYEES[0], employee0);

		updateEmployee(employee0, EmployeesFixture.EMPLOYEES[2]);
		checkCursor(EmployeesFixture.EMPLOYEES[2], employee0);

		updateEmployee(employee0, EmployeesFixture.EMPLOYEES[1]);
		checkCursor(EmployeesFixture.EMPLOYEES[1], employee0);
		checkCursor(EmployeesFixture.EMPLOYEES[1], empls.first());
	}

	@Test
	public void shouldSetAndGetMixedValues() throws Exception {
		checkSetAndGetMixedValues(employees);
		checkSetAndGetMixedValues(employeesView);
	}

	private void checkSetAndGetMixedValues(
			AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> empls)
			throws Exception {
		TestEmployeeRow employee = empls.first();

		employee.extra.set(true);
		assertEquals(true, employee.extra.get().getBooleanValue());
		assertEquals(ColumnType.ColumnTypeBool, employee.extra.getType());

		byte[] arr = { 1, 3, 5 };
		employee.extra.set(arr);
		// FIXME: shouldn't be BINARY_TYPE_BYTE_ARRAY an expected type here?
		assertEquals(Mixed.BINARY_TYPE_BYTE_BUFFER, employee.extra.get()
				.getBinaryType());
		assertEquals(ByteBuffer.wrap(arr), employee.extra.get()
				.getBinaryValue());
		assertEquals(ColumnType.ColumnTypeBinary, employee.extra.getType());

		ByteBuffer buf = ByteBuffer.allocateDirect(3);
		byte[] arr2 = { 10, 20, 30 };
		buf.put(arr2);
		employee.extra.set(buf);
		assertEquals(Mixed.BINARY_TYPE_BYTE_BUFFER, employee.extra.get()
				.getBinaryType());
		assertEquals(ByteBuffer.wrap(arr2), employee.extra.get()
				.getBinaryValue());
		assertEquals(ColumnType.ColumnTypeBinary, employee.extra.getType());

		Date date = new Date(6547);
		employee.extra.set(date);
		assertEquals(date, employee.extra.get().getDateValue());
		assertEquals(ColumnType.ColumnTypeDate, employee.extra.getType());

		long num = 135L;
		employee.extra.set(num);
		assertEquals(num, employee.extra.get().getLongValue());
		assertEquals(ColumnType.ColumnTypeInt, employee.extra.getType());

		Mixed mixed = Mixed.mixedValue("mixed");
		employee.extra.set(mixed);
		assertEquals(mixed, employee.extra.get());
		assertEquals(ColumnType.ColumnTypeString, employee.extra.getType());

		employee.extra.set("abc");
		assertEquals("abc", employee.extra.get().getStringValue());
		assertEquals(ColumnType.ColumnTypeString, employee.extra.getType());
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldntSetTableValue() {
		// the "set" operation is not supported yet for sub-table columns
		employees.first().phones.set(employees.last().phones.get());
	}

	public void shouldProvideReadableValue() {
		TestEmployeeRow employee = employees.first();

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
