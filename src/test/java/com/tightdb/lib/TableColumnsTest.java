package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import java.util.Date;

import org.testng.annotations.Test;

import com.tightdb.example.generated.Employee;
import com.tightdb.example.generated.EmployeeView;
import com.tightdb.test.EmployeesFixture;

public class TableColumnsTest extends AbstractTableTest {

	@Test
	public void shouldFindFirstRecordByColumnValue() throws IllegalAccessException {
		Employee record = null;
		
		record = employees.firstName.findFirst(EmployeesFixture.EMPLOYEE[1].firstName);
		assertEquals(1, record.getPosition());

		record = employees.salary.findFirst(EmployeesFixture.EMPLOYEE[0].salary);
		assertEquals(0, record.getPosition());

		record = employees.salary.findFirst(12345);
		assertNull(record);

		record = employees.driver.findFirst(EmployeesFixture.EMPLOYEE[0].driver);
		assertEquals(0, record.getPosition());

		record = employees.driver.findFirst(EmployeesFixture.EMPLOYEE[1].driver);
		assertEquals(1, record.getPosition());

		record = employees.birthdate.findFirst(EmployeesFixture.EMPLOYEE[1].birthdate);
		assertEquals(1, record.getPosition());

		record = employees.birthdate.findFirst(EmployeesFixture.EMPLOYEE[2].birthdate);
		assertEquals(2, record.getPosition());

		record = employees.birthdate.findFirst(new Date(12345));
		assertNull(record);

	}

	@Test
	public void shouldFindAllRecordsByColumnValue() throws IllegalAccessException {
		EmployeeView view = null;
		view = employees.firstName.findAll(EmployeesFixture.EMPLOYEE[1].firstName);
		assertEquals(1, view.size());

		view = employees.salary.findAll(EmployeesFixture.EMPLOYEE[0].salary);
		assertEquals(2, view.size());

		view = employees.salary.findAll(12345);
		assertEquals(0, view.size());
		
		view = employees.driver.findAll(false);
		assertEquals(1, view.size());
		
		view = employees.driver.findAll(true);
		assertEquals(2, view.size());
		
		view = employees.birthdate.findAll(EmployeesFixture.EMPLOYEE[2].birthdate);
		assertEquals(1, view.size());
		
		view = employees.birthdate.findAll(EmployeesFixture.EMPLOYEE[1].birthdate);
		assertEquals(1, view.size());
		
		view = employees.birthdate.findAll(new Date(0));
		assertEquals(0, view.size());
	}

	@Test
	public void shouldAggregateColumnValue() {
		assertEquals(EmployeesFixture.EMPLOYEE[0].salary, employees.salary.minimum());
		assertEquals(EmployeesFixture.EMPLOYEE[1].salary, employees.salary.maximum());
		long sum = EmployeesFixture.EMPLOYEE[0].salary + EmployeesFixture.EMPLOYEE[1].salary +
				EmployeesFixture.EMPLOYEE[2].salary;
		assertEquals(sum, employees.salary.sum());
	}

	@Test
	public void shouldAddValueToWholeColumn() {
		employees.salary.addLong(123);
		for (int i=0; i<EmployeesFixture.EMPLOYEE.length; ++i)
			assertEquals(EmployeesFixture.EMPLOYEE[i].salary+123, employees.at(i).getSalary());
	}

	
}
