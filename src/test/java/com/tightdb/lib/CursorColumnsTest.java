package com.tightdb.lib;

import static com.tightdb.test.EmployeesFixture.*;
import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

import com.tightdb.generated.Employee;

public class CursorColumnsTest extends AbstractTableTest {

	private static final String[] EXPECTED_COLUMNS = { "firstName", "lastName", "salary", "driver", "photo", "birthdate", "extra", "phones" };

	@Test
	public void shouldGetCorrectColumnValues() throws IllegalAccessException {
		Employee employee = employees.first();

		assertEquals(FIRST_NAMES[0], employee.firstName.get());
		assertEquals(LAST_NAMES[0], employee.lastName.get());
		assertEquals(SALARIES[0], employee.salary.get().longValue());
		assertEquals(DRIVERS[0], employee.driver.get().booleanValue());
		assertEquals(ByteBuffer.wrap(PHOTOS[0]), employee.photo.get());
		assertEquals(BIRTHDATES[0], employee.birthdate.get());
		assertEquals(EXTRAS[0], employee.extra.get().getStringValue());
		assertEquals(TightDB.mixedValue(EXTRAS[0]), employee.extra.get());

		AbstractColumn<?, ?, ?, ?>[] columns = employee.columns();
		assertEquals(EXPECTED_COLUMNS.length, columns.length);

		for (int i = 0; i < columns.length; i++) {
			AbstractColumn<?, ?, ?, ?> column = columns[i];
			assertEquals(EXPECTED_COLUMNS[i], column.getName());
		}
	}

	@Test
	public void shouldSetAndGetCorrectColumnValues() throws IllegalAccessException {
		// FIXME: implement this!
	}

}
