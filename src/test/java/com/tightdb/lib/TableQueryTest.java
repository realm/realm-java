package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import com.tightdb.example.generated.EmployeeQuery;
import com.tightdb.example.generated.EmployeeView;

public class TableQueryTest extends AbstractTableTest {

	@Test
	public void shouldMatchOnSimpleNumberCriteria() {
		assertEquals(1, employees.salary.equal(30000).findAll().size());
		assertEquals(1, employees.salary.eq(30000).findAll().size());

		assertEquals(2, employees.salary.notQqual(30000).findAll().size());
		assertEquals(2, employees.salary.neq(30000).findAll().size());
		
		assertEquals(2, employees.salary.lessThan(30000).findAll().size());
		assertEquals(2, employees.salary.lt(30000).findAll().size());
		
		assertEquals(3, employees.salary.lessThanOrEqual(30000).findAll().size());
		assertEquals(3, employees.salary.lte(30000).findAll().size());

		assertEquals(3, employees.salary.greaterThan(5000).findAll().size());
		assertEquals(3, employees.salary.gt(5000).findAll().size());
		
		assertEquals(3, employees.salary.greaterThanOrEqual(10000).findAll().size());
		assertEquals(3, employees.salary.gte(10000).findAll().size());
		
		assertEquals(2, employees.salary.between(5000, 15000).findAll().size());
	}

	@Test
	public void shouldMatchOnSimpleStringCriteria() {
		assertEquals(1, employees.firstName.eq("John").findAll().size());
		assertEquals(1, employees.firstName.equal("John").findAll().size());
		assertEquals(1, employees.firstName.eq("john", false).findAll().size());
		assertEquals(1, employees.firstName.equal("john", false).findAll().size());
		
		assertEquals(2, employees.firstName.neq("John").findAll().size());
		assertEquals(2, employees.firstName.notEqual("John").findAll().size());
		assertEquals(2, employees.firstName.neq("John").findAll().size());
		assertEquals(2, employees.firstName.notEqual("John").findAll().size());
		
		assertEquals(2, employees.firstName.startsWith("J").findAll().size());
		assertEquals(2, employees.firstName.startsWith("j", false).findAll().size());
		
		assertEquals(1, employees.firstName.endsWith("hny").findAll().size());
		assertEquals(1, employees.firstName.endsWith("hnY", false).findAll().size());
		
		assertEquals(2, employees.firstName.contains("ohn").findAll().size());
		assertEquals(2, employees.firstName.contains("ohN", false).findAll().size());
	}

	@Test
	public void shouldMatchOnSimpleBooleanCriteria() {
		assertEquals(2, employees.driver.eq(true).findAll().size());
		assertEquals(2, employees.driver.equal(true).findAll().size());

		assertEquals(1, employees.driver.neq(true).findAll().size());
		assertEquals(1, employees.driver.notEqual(true).findAll().size());
	}

	@Test
	public void shouldMatchOnCombinedAndOrCriteria() {
		EmployeeView nikoOrJohn = employees.firstName.startsWith("Nik").lastName.contains("vski")
				.or().firstName.eq("John").findAll();

		assertEquals(2, nikoOrJohn.size());
	}

	@Test
	public void shouldMatchOnCriteriaEndingWithGroup() {
		EmployeeView niko = employees.where()
		.firstName.startsWith("Nik")
		.salary.eq(30000)
		.group()
			.lastName.contains("vski")
			.or()
			.firstName.eq("John")
		.endGroup()
		.findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchOnCriteriaBeginingWithGroup() {
		EmployeeView niko = employees.where()
		.group()
			.lastName.contains("vski")
			.or()
			.firstName.eq("John")
		.endGroup()
		.firstName.startsWith("Nik")
		.salary.eq(30000)
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
				.firstName.eq("John")
			.endGroup()
			.salary.eq(30000)
			.findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchMultipleQueriesWithoutInterference() {
		EmployeeView niko1 = employees.firstName.startsWith("Nik").group().lastName.contains("vski")
				.or().firstName.eq("John").endGroup().findAll();
		EmployeeView niko2 = employees.where().group().lastName.contains("vski")
				.or().firstName.eq("John").endGroup().firstName.startsWith("Nik")
				.findAll();

		assertEquals(1, niko1.size());
		assertEquals(1, niko2.size());
	}

	@Test (enabled = false) // FIXME: enable the test (fix the bug) 
	public void shouldRemoveAllMatchingRows() {
		// Remove all
		EmployeeQuery q = employees.where().salary.lessThan(100000000);
		// EmployeeQuery q = employees.where().firstName.neq("xy");

		assertEquals(3, q.count());
		
		long n = q.remove();
		assertEquals(3, n);
		assertEquals(0, employees.size());
	}
	
	@Test (enabled = true)
	public void shouldRemoveSomeMatchingRows() {
		// Remove some
		EmployeeQuery q = employees.where().salary.lessThan(100000000);
		
		assertEquals(1, q.count(1, 2, 1));
		
		long n = q.remove(1, 2, 1);
		assertEquals(1, n);
		assertEquals(2, employees.size());
	}
	
	@Test (enabled = true)
	public void shouldntRemoveNonMatchingRows() {
		// Remove some
		EmployeeQuery q = employees.salary.lessThan(10000);
		
		assertEquals(0, q.count());
		
		long n = q.remove();
		assertEquals(0, n);
		assertEquals(3, employees.size());
	}
	
}