package com.tightdb.lib;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.tightdb.example.EmployeeTable;
import com.tightdb.example.PhoneTable;
import com.tightdb.test.TestEmployeeRow;

public class MixedSubtableTest extends AbstractTest {

	@Test
	public void shouldStoreSubtableInMixedTypeColumn() {
		TestEmployeeRow employee = employees.at(0);
		PhoneTable phones = employee.extra.createSubtable(PhoneTable.class);

		phones.add("mobile", "123");
		assertEquals(1, phones.size());

		PhoneTable phones2 = employee.extra.getSubtable(PhoneTable.class);
		assertEquals(1, phones2.size());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldFailOnOnWrongSubtableRetrievalFromMixedTypeColumn() {
		TestEmployeeRow employee = employees.at(0);
		PhoneTable phones = employee.extra.createSubtable(PhoneTable.class);

		phones.add("mobile", "123");
		assertEquals(1, phones.size());

		// should fail - since we try to get the wrong subtable class
		employee.extra.getSubtable(EmployeeTable.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldFailOnOnSubtableRetrtievalFromIncorrectType() {
		TestEmployeeRow employee = employees.at(0);
		employee.extra.set(123);

		// should fail
		employee.extra.getSubtable(PhoneTable.class);
	}

}
