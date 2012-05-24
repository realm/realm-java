package com.tightdb.lib;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.generated.EmployeeView;

public abstract class AbstractDataOperationsTest {

	protected static final String NAME0 = "John";
	protected static final String NAME1 = "Nikolche";
	protected static final String NAME2 = "Johny";

	protected abstract AbstractRowset<Employee, EmployeeView, EmployeeQuery> getEmployees();

	@After
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

		getEmployees().at(1).setExtra(TightDB.mixedValue("new_value"));
		assertEquals("new_value", getEmployees().at(1).getExtra().getValue());
		assertEquals("new_value", getEmployees().at(1).getExtra().getStringValue());
	}

}
