package com.tightdb.lib;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;

public class QueryTest {

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

	@After
	public void clear() {
		employees.clear();
	}
	
	@Test
	public void shouldMatchOnSimpleNumberCriteria() {
		assertEquals(1, employees.salary.is(30000).findAll().size());
		
		assertEquals(2, employees.salary.lessThan(30000).findAll().size());
		assertEquals(3, employees.salary.lessOrEqual(30000).findAll().size());
		
		assertEquals(2, employees.salary.greaterThan(10000).findAll().size());
		assertEquals(3, employees.salary.greaterOrEqual(10000).findAll().size());
	}

	@Test
	public void shouldMatchOnSimpleStringCriteria() {
		assertEquals(1, employees.firstName.is("John").findAll().size());
		assertEquals(2, employees.firstName.startsWith("J").findAll().size());
		assertEquals(1, employees.firstName.endWith("hny").findAll().size());
	}

	@Test
	public void shouldMatchOnCombinedAndOrCriteria() {
		EmployeeView nikoOrJohn = employees.firstName.startsWith("Nik").lastName.contains("vski").or().firstName.is("John").findAll();

		assertEquals(2, nikoOrJohn.size());
	}

	@Test
	public void shouldMatchOnCriteriaEndingWithGroup() {
		EmployeeView niko = employees.firstName.startsWith("Nik").group().lastName.contains("vski").or().firstName.is("John").endGroup().findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchOnCriteriaBeginingWithGroup() {
		EmployeeView niko = employees.where().group().lastName.contains("vski").or().firstName.is("John").endGroup().firstName.startsWith("Nik")
				.findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchOnCriteriaHavingGroupInMiddle() {
		EmployeeView niko = employees.where()
		.firstName.startsWith("Nik")
		.group()
			.lastName.contains("vski")
			.or()
			.firstName.is("John")
		.endGroup()
		.salary.is(30000)
		.findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchMultipleQueriesWithoutInterference() {
		EmployeeView niko1 = employees.firstName.startsWith("Nik").group().lastName.contains("vski").or().firstName.is("John").endGroup().findAll();
		EmployeeView niko2 = employees.where().group().lastName.contains("vski").or().firstName.is("John").endGroup().firstName.startsWith("Nik")
				.findAll();

		assertEquals(1, niko1.size());
		assertEquals(1, niko2.size());
	}

}
