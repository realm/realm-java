package com.tightdb.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;

public class TableTest {

	protected static final String NAME0 = "John";
	protected static final String NAME1 = "Nikolche";
	protected static final String NAME2 = "Johny";

	protected EmployeeTable employees2;
	private EmployeeView employees;

	@Before
	public void init() {
		employees2 = new EmployeeTable();

		employees2.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		employees2.add(NAME2, "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		employees2.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);
		
		employees = employees2.where().findAll();
	}

	@After
	public void clear() {
		employees.clear();
	}

	@Test
	public void shouldRetrieveRowsByIndex() {
		assertEquals(NAME0, employees.at(0).getFirstName());
		assertEquals(NAME1, employees.at(1).getFirstName());
		assertEquals(NAME2, employees.at(2).getFirstName());
	}

	@Test
	public void shouldHaveTwoWaysToReadCellValues() {
		assertEquals(NAME0, employees.at(0).getFirstName());
		assertEquals(NAME0, employees.at(0).firstName.get());
	}

	@Test
	public void shouldHaveTwoWaysToWriteCellValues() {
		employees.at(0).setFirstName("FOO");
		assertEquals("FOO", employees.at(0).getFirstName());

		employees.at(0).firstName.set("BAR");
		assertEquals("BAR", employees.at(0).getFirstName());
	}

	@Test
	public void shouldAllowMixedValues() throws IllegalAccessException {
		assertEquals("extra", employees.at(0).getExtra().getValue());
		assertEquals("extra", employees.at(0).getExtra().getStringValue());

		assertEquals(1234L, employees.at(1).getExtra().getValue());
		assertEquals(1234L, employees.at(1).getExtra().getLongValue());

		assertEquals(true, employees.at(2).getExtra().getValue());
		assertEquals(true, employees.at(2).getExtra().getBooleanValue());

		employees.at(1).setExtra(TightDB.mixedValue("new_value"));
		assertEquals("new_value", employees.at(1).getExtra().getValue());
		assertEquals("new_value", employees.at(1).getExtra().getStringValue());
	}

	@Test
	public void shouldFindFirstRecordByColumnValue() throws IllegalAccessException {
		Employee record1 = employees.firstName.findFirst(NAME1);
		assertEquals(1, record1.getPosition());

		Employee record2 = employees.salary.findFirst(10000);
		assertEquals(0, record2.getPosition());

		Employee record3 = employees.salary.findFirst(12345);
		assertNull(record3);
	}

	@Test
	@Ignore // FIXME: crashes!
	public void shouldFindAllRecordsByColumnValue() throws IllegalAccessException {
		EmployeeView view1 = employees.firstName.findAll(NAME1);
		assertEquals(1, view1.size());

		EmployeeView view2 = employees.salary.findAll(10000);
		assertEquals(2, view2.size());

		EmployeeView view3 = employees.salary.findAll(12345);
		assertEquals(0, view3.size());
	}

	@Test
	public void shouldAggregateColumnValuee() {
		assertEquals(10000, employees.salary.minimum());
		assertEquals(30000, employees.salary.maximum());
		assertEquals(50000, employees.salary.sum());
	}

}
