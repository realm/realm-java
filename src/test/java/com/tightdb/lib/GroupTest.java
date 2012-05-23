package com.tightdb.lib;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.tightdb.Group;
import com.tightdb.generated.EmployeeTable;

public class GroupTest {

	protected static final String NAME0 = "John";
	protected static final String NAME1 = "Nikolche";
	protected static final String NAME2 = "Johny";

	@Test
	public void shouldCreateTablesInGroup() {
		Group group = new Group();

		EmployeeTable employees = new EmployeeTable(group);

		employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		employees.add(NAME2, "B. Good", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		employees.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);

		EmployeeTable employees2 = new EmployeeTable(group);

		assertEquals(NAME0, employees2.at(0).getFirstName());
		assertEquals(NAME1, employees2.at(1).getFirstName());
		assertEquals(NAME2, employees2.at(2).getFirstName());
	}

}
