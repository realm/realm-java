package com.tightdb.lib;

import java.util.Date;

import org.testng.annotations.Test;

import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestEmployeeView;

@Test
public class ViewNavigationTest extends AbstractNavigationTest {

	private TestEmployeeView view;

	public ViewNavigationTest() {
		TestEmployeeTable employees = new TestEmployeeTable();

		employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 },
				new Date(), "extra", null);
		employees.add("Johny", "B. Good", 20000, true, new byte[] { 1, 2, 3 },
				new Date(), true, null);
		employees.insert(1, "Nikolche", "Mihajlovski", 30000, false,
				new byte[] { 4, 5 }, new Date(), 1234, null);

		view = employees.firstName.startsWith("").findAll();
	}

	@Override
	protected AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getTableOrView() {
		return view;
	}

}
