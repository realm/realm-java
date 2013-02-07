package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestPhoneTable;

public class MixedSubtableTest extends AbstractTest {

	@Test
	public void shouldStoreSubtableInMixedTypeColumn() {
		TestEmployeeRow employee = employees.at(0);
		TestPhoneTable phones = employee.extra.createSubtable(TestPhoneTable.class);

		phones.add("mobile", "123");
		assertEquals(1, phones.size());

		TestPhoneTable phones2 = employee.extra.getSubtable(TestPhoneTable.class);
		assertEquals(1, phones2.size());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldFailOnOnWrongSubtableRetrievalFromMixedTypeColumn() {
		TestEmployeeRow employee = employees.at(0);
		TestPhoneTable phones = employee.extra.createSubtable(TestPhoneTable.class);

		phones.add("mobile", "123");
		assertEquals(1, phones.size());

		// should fail - since we try to get the wrong subtable class
		employee.extra.getSubtable(TestEmployeeTable.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldFailOnOnSubtableRetrtievalFromIncorrectType() {
		TestEmployeeRow employee = employees.at(0);
		employee.extra.set(123);

		// should fail
		employee.extra.getSubtable(TestPhoneTable.class);
	}

}
