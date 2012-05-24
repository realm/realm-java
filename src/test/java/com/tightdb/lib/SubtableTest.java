package com.tightdb.lib;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.PhoneTable;

public class SubtableTest {

	protected static final String NAME0 = "John";
	protected static final String NAME1 = "Nikolche";
	protected static final String NAME2 = "Johny";

	protected EmployeeTable employees;

	@Before
	public void init() {
		employees = new EmployeeTable();

		employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		employees.add(NAME2, "B. Good", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		employees.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);
	}

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
