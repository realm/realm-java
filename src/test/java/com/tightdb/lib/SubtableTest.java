package com.tightdb.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.tightdb.generated.Employee;
import com.tightdb.generated.PhoneTable;

public class SubtableTest extends AbstractTableTest {

	@Test
	public void shouldSaveSubtableChanges() {
		Employee employee = employees.at(0);
		PhoneTable phones1 = employee.getPhones();
		phones1.add("mobile", "123");
		assertEquals(1, phones1.size());

		PhoneTable phones2 = employee.getPhones();
		assertEquals(1, phones2.size());

		employees.clear();
	}

}
