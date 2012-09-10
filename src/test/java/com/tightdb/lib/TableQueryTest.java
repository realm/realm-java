package com.tightdb.lib;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeView;

public class TableQueryTest extends AbstractTest {

	@Test
	public void shouldMatchOnSimpleNumberCriteria() {
		assertEquals(1, employees.salary.equal(30000).findAll().size());
		assertEquals(1, employees.salary.eq(30000).findAll().size());

		assertEquals(2, employees.salary.notEqual(30000).findAll().size());
		assertEquals(2, employees.salary.neq(30000).findAll().size());

		assertEquals(2, employees.salary.lessThan(30000).findAll().size());
		assertEquals(2, employees.salary.lt(30000).findAll().size());

		assertEquals(3, employees.salary.lessThanOrEqual(30000).findAll()
				.size());
		assertEquals(3, employees.salary.lte(30000).findAll().size());

		assertEquals(3, employees.salary.greaterThan(5000).findAll().size());
		assertEquals(3, employees.salary.gt(5000).findAll().size());

		assertEquals(3, employees.salary.greaterThanOrEqual(10000).findAll()
				.size());
		assertEquals(3, employees.salary.gte(10000).findAll().size());

		assertEquals(2, employees.salary.between(5000, 15000).findAll().size());
	}

	@Test
	public void shouldCalculateStatistics() {
		TestEmployeeQuery results = employees.firstName.eq("John").or().firstName
				.eq("Nikolche");
		assertEquals(2, results.count());

		assertEquals(20000.0, results.salary.average());
		assertEquals(10000.0, results.salary.average(0, 100, 1)); // first
		assertEquals(30000.0, results.salary.average(1, 2, 100)); // second
		assertEquals(20000.0, results.salary.average(0, 2, 100)); // both

		assertEquals(10000, results.salary.minimum());
		assertEquals(10000, results.salary.minimum(0, 100, 1)); // first
		assertEquals(30000, results.salary.minimum(1, 2, 100)); // second
		assertEquals(10000, results.salary.minimum(0, 2, 100)); // both

		assertEquals(30000, results.salary.maximum());
		assertEquals(10000, results.salary.maximum(0, 100, 1)); // first
		assertEquals(30000, results.salary.maximum(1, 2, 100)); // second
		assertEquals(30000, results.salary.maximum(0, 2, 100)); // both

		assertEquals(40000, results.salary.sum());
		assertEquals(10000, results.salary.sum(0, 100, 1)); // first
		assertEquals(30000, results.salary.sum(1, 2, 100)); // second
		assertEquals(40000, results.salary.sum(0, 2, 100)); // both
	}

	@Test(enabled=false)
	public void shouldMatchOnSimpleStringCriteria() {
		assertEquals(1, employees.firstName.eq("John").findAll().size());
		assertEquals(1, employees.firstName.equal("John").findAll().size());
		assertEquals(1, employees.firstName.eq("john", false).findAll().size());
		assertEquals(1, employees.firstName.equal("john", false).findAll()
				.size());

		assertEquals(2, employees.firstName.neq("John").findAll().size());
		assertEquals(2, employees.firstName.notEqual("John").findAll().size());
		assertEquals(2, employees.firstName.neq("John").findAll().size());
		assertEquals(2, employees.firstName.notEqual("John").findAll().size());

		assertEquals(2, employees.firstName.startsWith("J").findAll().size());
		assertEquals(2, employees.firstName.startsWith("j", false).findAll()
				.size());

		assertEquals(1, employees.firstName.endsWith("hny").findAll().size());
		assertEquals(1, employees.firstName.endsWith("hnY", false).findAll()
				.size());

		assertEquals(2, employees.firstName.contains("ohn").findAll().size());
		assertEquals(2, employees.firstName.contains("ohN", false).findAll()
				.size());
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
		TestEmployeeView nikoOrJohn = employees.firstName.startsWith("Nik").lastName
				.contains("vski").or().firstName.eq("John").findAll();

		assertEquals(2, nikoOrJohn.size());
	}

	@Test
	public void shouldMatchOnCriteriaEndingWithGroup() {
		TestEmployeeView niko = employees.where().firstName.startsWith("Nik").salary
				.eq(30000).group().lastName.contains("vski").or().firstName
				.eq("John").endGroup().findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchOnCriteriaBeginingWithGroup() {
		TestEmployeeView niko = employees.where().group().lastName.contains(
				"vski").or().firstName.eq("John").endGroup().firstName
				.startsWith("Nik").salary.eq(30000).findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchOnCriteriaHavingGroupInMiddle() {
		TestEmployeeView niko = employees.where().firstName.startsWith("Nik")
				.group().lastName.contains("vski").or().firstName.eq("John")
				.endGroup().salary.eq(30000).findAll();

		assertEquals(1, niko.size());
	}

	@Test
	public void shouldMatchMultipleQueriesWithoutInterference() {
		TestEmployeeView niko1 = employees.firstName.startsWith("Nik").group().lastName
				.contains("vski").or().firstName.eq("John").endGroup()
				.findAll();
		TestEmployeeView niko2 = employees.where().group().lastName.contains(
				"vski").or().firstName.eq("John").endGroup().firstName
				.startsWith("Nik").findAll();

		assertEquals(1, niko1.size());
		assertEquals(1, niko2.size());
	}

	@Test(enabled = false)
	// FIXME: enable the test (fix the bug)
	public void shouldRemoveAllMatchingRows() {
		// Remove all
		TestEmployeeQuery q = employees.where().salary.lessThan(100000000);
		// EmployeeQuery q = employees.where().firstName.neq("xy");

		assertEquals(3, q.count());

		long n = q.remove();
		assertEquals(3, n);
		assertEquals(0, employees.size());
	}

	@Test(enabled = true)
	public void shouldRemoveSomeMatchingRows() {
		// Remove some
		TestEmployeeQuery q = employees.where().salary.lessThan(100000000);

		assertEquals(1, q.count(1, 2, 1));

		long n = q.remove(1, 2, 1);
		assertEquals(1, n);
		assertEquals(2, employees.size());
	}

	@Test(enabled = true)
	public void shouldntRemoveNonMatchingRows() {
		// Remove some
		TestEmployeeQuery q = employees.salary.lessThan(10000);

		assertEquals(0, q.count());

		long n = q.remove();
		assertEquals(0, n);
		assertEquals(3, employees.size());
	}

}
