package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import java.util.Date;

import org.testng.annotations.Test;

import com.tightdb.example.generated.Employee;
import com.tightdb.example.generated.EmployeeView;
import com.tightdb.test.EmployeesFixture;

public class ViewColumnsTest extends AbstractViewTest {

	@Test
	public void shouldFindFirstRecordByColumnValue() throws IllegalAccessException {
		Employee record = null;
		record = employees.firstName.findFirst(EmployeesFixture.EMPLOYEES[1].firstName);
		assertEquals(1, record.getPosition());

		record = employees.salary.findFirst(EmployeesFixture.EMPLOYEES[0].salary);
		assertEquals(0, record.getPosition());

		record = employees.salary.findFirst(12345);
		assertNull(record);

		record = employees.driver.findFirst(EmployeesFixture.EMPLOYEES[0].driver);
		assertEquals(0, record.getPosition());

		record = employees.driver.findFirst(EmployeesFixture.EMPLOYEES[1].driver);
		assertEquals(1, record.getPosition());

		record = employees.birthdate.findFirst(EmployeesFixture.EMPLOYEES[1].birthdate);
		assertEquals(1, record.getPosition());

		record = employees.birthdate.findFirst(EmployeesFixture.EMPLOYEES[2].birthdate);
		assertEquals(2, record.getPosition());

		record = employees.birthdate.findFirst(new Date(12345));
		assertNull(record);
	}

	@Test
	public void shouldFindAllRecordsByColumnValue() throws IllegalAccessException {
		EmployeeView view = null;
		
		view = employees.firstName.findAll(EmployeesFixture.EMPLOYEES[1].firstName);
		assertEquals(1, view.size());

		view = employees.salary.findAll(EmployeesFixture.EMPLOYEES[0].salary);
		assertEquals(2, view.size());

		view = employees.salary.findAll(12345);
		assertEquals(0, view.size());
		
		view = employees.driver.findAll(false);
		assertEquals(1, view.size());
		
		view = employees.driver.findAll(true);
		assertEquals(2, view.size());
		
		view = employees.birthdate.findAll(EmployeesFixture.EMPLOYEES[2].birthdate);
		assertEquals(1, view.size());
		
		view = employees.birthdate.findAll(EmployeesFixture.EMPLOYEES[1].birthdate);
		assertEquals(1, view.size());
		
		view = employees.birthdate.findAll(new Date(0));
		assertEquals(0, view.size());
	}

	@Test
	public void shouldAggregateColumnValue() {
		assertEquals(EmployeesFixture.EMPLOYEES[0].salary, employees.salary.minimum());
		assertEquals(EmployeesFixture.EMPLOYEES[1].salary, employees.salary.maximum());
		long sum = EmployeesFixture.EMPLOYEES[0].salary + EmployeesFixture.EMPLOYEES[1].salary +
				EmployeesFixture.EMPLOYEES[2].salary;
		assertEquals(sum, employees.salary.sum());
	}

	@Test
	public void shouldAddValueToWholeColumn() {
		employees.salary.addLong(123);
		for (int i=0; i<EmployeesFixture.EMPLOYEES.length; ++i)
			assertEquals(EmployeesFixture.EMPLOYEES[i].salary+123, employees.at(i).getSalary());
	}


}
