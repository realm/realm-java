package com.tightdb.lib;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.tightdb.example.Employee;
import com.tightdb.example.Phone;
import com.tightdb.example.PhoneQuery;
import com.tightdb.example.PhoneTable;
import com.tightdb.example.PhoneView;
import com.tightdb.test.EmployeesFixture;

public class SubtableTest extends AbstractTest {

	@Test
	public void shouldSaveSubtableChanges() {
		Employee employee = employees.at(0);
		
		// check the basic operations
		PhoneTable phones1 = employee.getPhones();
		assertEquals(1, phones1.size());
		
		phones1.add("mobile", "111");
		assertEquals(2, phones1.size());

		PhoneTable phones2 = employee.getPhones();
		assertEquals(2, phones2.size());

		phones2.add("mobile", "222");
		assertEquals(3, phones2.size());

		phones2.insert(1, "home", "333");
		assertEquals(4, phones2.size());

		PhoneTable phones3 = employee.getPhones();
		assertEquals(2, phones3.type.eq("mobile").count());
		assertEquals(2, phones3.type.eq("home").count());

		assertEquals(1, phones3.number.eq("111").count());
		assertEquals(1, phones3.number.eq("123").count());
		assertEquals(0, phones3.number.eq("xxx").count());

		// check the search operations
		PhoneQuery phoneQuery = phones3.where().number.eq("111").number.neq("wrong").type.eq("mobile").type.neq("wrong");
		assertEquals(1, phoneQuery.count());

		PhoneView all = phoneQuery.findAll();
		assertEquals(1, all.size());
		checkPhone(all.at(0), "mobile", "111");

		checkPhone(phoneQuery.findFirst(), "mobile", "111");
		checkPhone(phoneQuery.findLast(), "mobile", "111");
		checkPhone(phoneQuery.findNext(), "mobile", "111");
		assertEquals(null, phoneQuery.findNext());

		// make sure the other sub-tables and independent and were not changed
		assertEquals(EmployeesFixture.PHONES[1].length, employees.at(1).getPhones().size());
		assertEquals(EmployeesFixture.PHONES[2].length, employees.at(2).getPhones().size());
		
		// check the clear operation on the query
		phoneQuery.clear();
		assertEquals(3, phones1.size());
		
		// check the clear operation
		phones3.clear();
		assertEquals(0, phones1.size());
		assertEquals(0, phones2.size());
		assertEquals(0, phones3.size());

		employees.clear();
	}

	private void checkPhone(Phone phone, String type, String number) {
		assertEquals(type, phone.getType());
		assertEquals(number, phone.getNumber());
		assertEquals(type, phone.type.get());
		assertEquals(number, phone.number.get());
	}

	@Test
	public void shouldInvalidateWhenParentTableIsCleared() {
		Employee employee = employees.at(0);
		PhoneTable phones = employee.getPhones();
		assertTrue(phones.isValid());
		
		employees.clear();
		assertFalse(phones.isValid());
	}
	
	@Test
	public void shouldInvalidateOnRemovedRecordParentTable() {
		Employee employee = employees.at(0);
		PhoneTable phones = employee.getPhones();
		assertTrue(phones.isValid());
		
		employees.remove(2);
		assertFalse(phones.isValid());
	}
	
}
