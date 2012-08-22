package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import java.util.Iterator;

import org.testng.annotations.Test;

import com.tightdb.example.generated.Employee;
import com.tightdb.example.generated.Phone;
import com.tightdb.test.EmployeesFixture;

public class TableCursorColumnTest extends AbstractTest {

	@Test
	public void shouldProvideConvenienceMethods() {
		Employee employee = employees.last();
		
		// 2 predefined records in the "phones" sub-table should exist 
		assertEquals(2, employee.phones.size());
		assertFalse(employee.phones.isEmpty());

		// make sure the 2 predefined records in the "phones" sub-table match
		checkCursorValues(EmployeesFixture.PHONES[2][0], employee.phones.first());
		checkCursorValues(EmployeesFixture.PHONES[2][0], employee.phones.at(0));
		checkCursorValues(EmployeesFixture.PHONES[2][1], employee.phones.last());
		checkCursorValues(EmployeesFixture.PHONES[2][1], employee.phones.at(1));

		// check the iteration through the predefined records
		Iterator<Phone> it = employee.phones.iterator();
		checkIterator(it, EmployeesFixture.PHONES[2]);
		
		// clear the phones sub-table
		employee.phones.clear();
		
		// check there are no more records in the "phones" sub-table
		assertEquals(0, employee.phones.size());
		assertTrue(employee.phones.isEmpty());
	}

}
