package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import com.tightdb.example.generated.Employee;
import com.tightdb.example.generated.EmployeeQuery;
import com.tightdb.example.generated.EmployeeView;

public abstract class AbstractNavigationTest {

	protected abstract AbstractRowset<Employee, EmployeeView, EmployeeQuery> getTableOrView();

	@Test
	public void shouldNavigateToFirstRecord() {
		Employee first = getTableOrView().first();

		assertEquals(0, first.getPosition());
	}

	@Test
	public void shouldNavigateToLastRecord() {
		Employee last = getTableOrView().last();

		assertEquals(getTableOrView().size() - 1, last.getPosition());
	}

	@Test
	public void shouldNavigateToNextRecord() {
		Employee e = getTableOrView().at(0).next();

		assertEquals(1, e.getPosition());
	}

	@Test
	public void shouldNavigateToPreviousRecord() {
		Employee e = getTableOrView().at(1).previous();

		assertEquals(0, e.getPosition());
	}

	@Test
	public void shouldNavigateAfterSpecifiedRecords() {
		Employee e = getTableOrView().at(0).after(2);

		assertEquals(2, e.getPosition());
	}

	@Test
	public void shouldNavigateBeforeSpecifiedRecords() {
		Employee e = getTableOrView().at(2).before(2);

		assertEquals(0, e.getPosition());
	}

	@Test
	public void shouldReturnNullOnInvalidPosition() {
		assertNull(getTableOrView().at(0).previous());
		assertNull(getTableOrView().last().next());
		assertNull(getTableOrView().at(1).before(2));
		assertNull(getTableOrView().at(2).after(1000));
	}

}
