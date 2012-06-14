package com.tightdb.lib;

import org.testng.annotations.Test;

import com.tightdb.generated.Employee;
import com.tightdb.generated.PhoneTable;

public class MixedSubtableTest extends AbstractTableTest {

	@Test
	public void shouldStoreSubtableInMixedTypeColumn() {
		Employee employee = employees.at(0);
		PhoneTable ss = employee.extra.createSubtable(PhoneTable.class);
		System.out.println("res: " + ss);
	}

}
