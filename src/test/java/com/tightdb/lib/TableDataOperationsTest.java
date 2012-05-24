package com.tightdb.lib;

import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;

public class TableDataOperationsTest extends AbstractDataOperationsTest {

	private EmployeeTable employees;

	@Override
	protected AbstractRowset<Employee, EmployeeView, EmployeeQuery> getEmployees() {
		if (employees == null) {
			employees = new EmployeeTable();

			employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
			employees.add(NAME2, "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(), true);
			employees.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234);
		}

		return employees;
	}

}
