package com.tightdb.lib;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.tightdb.Mixed;
import com.tightdb.example.Employee;
import com.tightdb.example.EmployeeQuery;
import com.tightdb.example.EmployeeView;

public abstract class AbstractDataOperationsTest {

	protected static final String NAME0 = "John";
	protected static final String NAME1 = "Nikolche";
	protected static final String NAME2 = "Johny";

	protected abstract AbstractRowset<Employee, EmployeeView, EmployeeQuery> getEmployees();

	@AfterMethod
	public void clear() {
		getEmployees().clear();
	}

	@Test
	public void shouldRetrieveRowsByIndex() {
		assertEquals(NAME0, getEmployees().at(0).getFirstName());
		assertEquals(NAME1, getEmployees().at(1).getFirstName());
		assertEquals(NAME2, getEmployees().at(2).getFirstName());
	}

	@Test
	public void shouldHaveTwoWaysToReadCellValues() {
		assertEquals(NAME0, getEmployees().at(0).getFirstName());
		assertEquals(NAME0, getEmployees().at(0).firstName.get());
	}

	@Test
	public void shouldHaveTwoWaysToWriteCellValues() {
		getEmployees().at(0).setFirstName("FOO");
		assertEquals("FOO", getEmployees().at(0).getFirstName());

		getEmployees().at(0).firstName.set("BAR");
		assertEquals("BAR", getEmployees().at(0).getFirstName());
	}

	@Test
	public void shouldAllowMixedValues() throws IllegalAccessException {
		assertEquals("extra", getEmployees().at(0).getExtra().getValue());
		assertEquals("extra", getEmployees().at(0).getExtra().getStringValue());

		assertEquals(1234L, getEmployees().at(1).getExtra().getValue());
		assertEquals(1234L, getEmployees().at(1).getExtra().getLongValue());

		assertEquals(true, getEmployees().at(2).getExtra().getValue());
		assertEquals(true, getEmployees().at(2).getExtra().getBooleanValue());

		getEmployees().at(1).setExtra(Mixed.mixedValue("new_value"));
		assertEquals("new_value", getEmployees().at(1).getExtra().getValue());
		assertEquals("new_value", getEmployees().at(1).getExtra().getStringValue());
	}

	@Test
	public void shouldRemoveFirstRow() throws IllegalAccessException {
		// Remove first row
		getEmployees().remove(0);
		assertEquals(NAME1, getEmployees().at(0).getFirstName());
		assertEquals(NAME2, getEmployees().at(1).getFirstName());
		assertEquals(2, getEmployees().size());
	}

	@Test
	public void shouldRemoveMiddleRow() throws IllegalAccessException {
		// Remove middle row
		getEmployees().remove(1);
		assertEquals(NAME0, getEmployees().at(0).getFirstName());
		assertEquals(NAME2, getEmployees().at(1).getFirstName());
		assertEquals(2, getEmployees().size());
	}

	@Test
	public void shouldRemoveLastRow() throws IllegalAccessException {
		// Remove last row
		getEmployees().remove(2);
		assertEquals(NAME0, getEmployees().at(0).getFirstName());
		assertEquals(NAME1, getEmployees().at(1).getFirstName());
		assertEquals(2, getEmployees().size());
	}

	@Test
	public void shouldPrintData() {
		assertNotNull(getEmployees().toString());
		TightDB.print(getEmployees());
		TightDB.print("Employees", getEmployees());
		
		assertNotNull(getEmployees().first().toString());
		TightDB.print("First employee", getEmployees().first());
		
		assertNotNull(getEmployees().first().birthdate.toString());
		assertNotNull(getEmployees().first().phones.toString());
	}
	
}
