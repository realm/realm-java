package com.tightdb.lib;

import java.util.Date;

import org.testng.annotations.Test;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;

@Test
public class ViewNavigationTest extends AbstractNavigationTest {

	private EmployeeView view;

	public ViewNavigationTest() {
		EmployeeTable employees = new EmployeeTable();

		employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		employees.add("Johny", "B. Good", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);
		
		view = employees.firstName.startsWith("").findAll();
	}

	@Override
	protected AbstractRowset<Employee, EmployeeView, EmployeeQuery> getTableOrView() {
		return view;
	}

}
