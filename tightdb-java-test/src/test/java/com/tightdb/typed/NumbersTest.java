package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestNumbersTable;

public class NumbersTest {

	private TestNumbersTable numbers;

	@BeforeMethod
	public void init() {
		numbers = new TestNumbersTable();

		numbers.add(10000, 10000.0f, 10000.0);
		numbers.add(10000, 10000.0f, 10000.0);
		numbers.insert(1, 30000, 30000.0f, 30000.0);

		assertEquals(3, numbers.size());
	}

	@Test
	public void shouldMatchFloats() {
		assertEquals(1, numbers.floatNum.equal(30000.0f).findAll().size());
		assertEquals(1, numbers.floatNum.eq(30000.0f).findAll().size());

		assertEquals(2, numbers.floatNum.notEqual(30000.0f).findAll().size());
		assertEquals(2, numbers.floatNum.neq(30000.0f).findAll().size());

		assertEquals(2, numbers.floatNum.lessThan(30000.0f).findAll().size());
		assertEquals(2, numbers.floatNum.lt(30000.0f).findAll().size());

		assertEquals(3, numbers.floatNum.lessThanOrEqual(30000.0f).findAll().size());
		assertEquals(3, numbers.floatNum.lte(30000.0f).findAll().size());

		assertEquals(3, numbers.floatNum.greaterThan(5000).findAll().size());
		assertEquals(3, numbers.floatNum.gt(5000).findAll().size());

		assertEquals(3, numbers.floatNum.greaterThanOrEqual(10000.0f).findAll().size());
		assertEquals(3, numbers.floatNum.gte(10000.0f).findAll().size());

		assertEquals(2, numbers.floatNum.between(5000, 15000).findAll().size());
	}

	@Test
	public void shouldMatchDoubles() {
		assertEquals(1, numbers.doubleNum.equal(30000.0).findAll().size());
		assertEquals(1, numbers.doubleNum.eq(30000.0).findAll().size());

		assertEquals(2, numbers.doubleNum.notEqual(30000.0).findAll().size());
		assertEquals(2, numbers.doubleNum.neq(30000.0).findAll().size());

		assertEquals(2, numbers.doubleNum.lessThan(30000.0).findAll().size());
		assertEquals(2, numbers.doubleNum.lt(30000.0).findAll().size());

		assertEquals(3, numbers.doubleNum.lessThanOrEqual(30000.0).findAll().size());
		assertEquals(3, numbers.doubleNum.lte(30000.0).findAll().size());

		assertEquals(3, numbers.doubleNum.greaterThan(5000).findAll().size());
		assertEquals(3, numbers.doubleNum.gt(5000).findAll().size());

		assertEquals(3, numbers.doubleNum.greaterThanOrEqual(10000.0).findAll().size());
		assertEquals(3, numbers.doubleNum.gte(10000.0).findAll().size());

		assertEquals(2, numbers.doubleNum.between(5000, 15000).findAll().size());
	}

	@Test
	public void shouldAggregateFloats() {
		assertEquals(10000.0f, numbers.floatNum.minimum());
		assertEquals(10000.0f, numbers.floatNum.minimum(0, 1)); // first
		assertEquals(30000.0f, numbers.floatNum.minimum(1, 2)); // second
		assertEquals(10000.0f, numbers.floatNum.minimum(0, 2)); // 1st & 2nd

		assertEquals(30000.0f, numbers.floatNum.maximum());
		assertEquals(10000.0f, numbers.floatNum.maximum(0, 1)); // first
		assertEquals(30000.0f, numbers.floatNum.maximum(1, 2)); // second
		assertEquals(30000.0f, numbers.floatNum.maximum(0, 2)); // 1st & 2nd

		assertEquals(50000.0, numbers.floatNum.sum());
		assertEquals(10000.0, numbers.floatNum.sum(0, 1)); // first
		assertEquals(30000.0, numbers.floatNum.sum(1, 2)); // second
		assertEquals(40000.0, numbers.floatNum.sum(0, 2)); // 1st & 2nd

		assertEquals(16666.66, numbers.floatNum.average(), 0.01);
		assertEquals(30000.0, numbers.floatNum.average(1, 2)); // second
		assertEquals(20000.0, numbers.floatNum.average(0, 2)); // 1st & 2nd
		assertEquals(10000.0, numbers.floatNum.average(0, 1)); // first
	}

	@Test
	public void shouldAggregateDoubles() {
		assertEquals(10000.0, numbers.doubleNum.minimum());
		assertEquals(10000.0, numbers.doubleNum.minimum(0, 1)); // first
		assertEquals(30000.0, numbers.doubleNum.minimum(1, 2)); // second
		assertEquals(10000.0, numbers.doubleNum.minimum(0, 2)); // 1st & 2nd

		assertEquals(30000.0, numbers.doubleNum.maximum());
		assertEquals(10000.0, numbers.doubleNum.maximum(0, 1)); // first
		assertEquals(30000.0, numbers.doubleNum.maximum(1, 2)); // second
		assertEquals(30000.0, numbers.doubleNum.maximum(0, 2)); // 1st & 2nd

		assertEquals(50000.0, numbers.doubleNum.sum());
		assertEquals(10000.0, numbers.doubleNum.sum(0, 1)); // first
		assertEquals(30000.0, numbers.doubleNum.sum(1, 2)); // second
		assertEquals(40000.0, numbers.doubleNum.sum(0, 2)); // 1st & 2nd

		assertEquals(16666.66, numbers.doubleNum.average(), 0.01);
		assertEquals(30000.0, numbers.doubleNum.average(1, 2)); // second
		assertEquals(20000.0, numbers.doubleNum.average(0, 2)); // 1st & 2nd
		assertEquals(10000.0, numbers.doubleNum.average(0, 1)); // first
	}

}
