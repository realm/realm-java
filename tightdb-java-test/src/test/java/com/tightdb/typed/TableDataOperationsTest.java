package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestEmployeeView;
import com.tightdb.test.TestPhoneTable;
import com.tightdb.typed.AbstractTableOrView;

@Test
public class TableDataOperationsTest extends AbstractDataOperationsTest {

	private TestEmployeeTable employees;
	private Object[][] phones;
	
	@Override
	protected AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getEmployees() {
		return employees;
	}

	@BeforeMethod
	public void init() {
		employees = new TestEmployeeTable();

		employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra", null);
		employees.add(NAME2, "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(), true, null);
		employees.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234, null);

		phones = new Object[][] { { "home", "123-123" }, { "mobile", "456-456" } };
		employees.add(NAME3, "Bond", 150000, true, new byte[] { 0 }, new Date(), "x", phones);
	}

	private void setAndTestValue(long val) {
		employees.get(1).setSalary(val);
		assertEquals(val, employees.get(1).getSalary());
	}

	@Test
	public void shouldStoreValues() {
		setAndTestValue(Integer.MAX_VALUE);
		setAndTestValue(Integer.MIN_VALUE);

		setAndTestValue(Long.MAX_VALUE);
		setAndTestValue(Long.MIN_VALUE);
	}

	@Test
	public void shouldConstructSubtableInline() {
		TestPhoneTable phones = employees.last().getPhones();
		assertEquals(2, phones.size());
		
		assertEquals("home", phones.get(0).type.get());
		assertEquals("123-123", phones.get(0).number.get());
		
		assertEquals("mobile", phones.get(1).getType());
		assertEquals("456-456", phones.get(1).getNumber());
	}


	@Test(enabled=true)
	public void shouldDeleteAllButLast() {		
		employees.moveLastOver(2);
		employees.moveLastOver(1);
		employees.moveLastOver(0);
		assertEquals("Bond", employees.get(0).getLastName());
		TestPhoneTable phones2 = employees.last().getPhones();
		assertEquals(2, phones2.size());
		assertEquals(1, employees.size());

		try {
			employees.moveLastOver(0);
			// should not allow the last to be removed
			assert(false);
		} catch (Exception e) {
		}
		
	}
}
