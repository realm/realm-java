package com.tightdb.lib;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.testng.annotations.Test;

import com.tightdb.Group;
import com.tightdb.test.TestEmployeeTable;

public class GroupTest {

	protected static final String NAME0 = "John";
	protected static final String NAME1 = "Nikolche";
	protected static final String NAME2 = "Johny";

	@Test(enabled = true)
	public void groupFileCanClose() throws NullPointerException, IOException {
		Group group = new Group();
		group.writeToFile("testfile.tdb");
		group.close();

		Group group2 = new Group("testfile.tdb");
		group2.close();
	}

	@Test(enabled = true)
	public void groupByteBufferCanClose() {
		Group group = new Group();
		ByteBuffer data = group.writeToByteBuffer();
		group.close();

		Group group2 = new Group(data);
		group2.close();
	}

	@Test(enabled = false)
	public void groupMemCanClose() {
		Group group = new Group();
		byte[] data = group.writeToMem();
		group.close();

		Group group2 = new Group(data);
		group2.close();

		// data is deleted by group.close()!
		// FIXME:
		System.out.println("Data len:" + data.length);
	}

	@Test(enabled = true)
	public void shouldCreateTablesInGroup() {
		Group group = new Group();

		TestEmployeeTable employees = new TestEmployeeTable(group);
		employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 },
				new Date(), "extra");
		employees.add(NAME2, "B. Good", 20000, true, new byte[] { 1, 2, 3 },
				new Date(), true);
		employees.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4,
				5 }, new Date(), 1234);

		byte[] data = group.writeToMem();
		// data is currently disposed after group.close() !
		
		// check table info retrieval
		assertEquals(1, group.getTableCount());
		assertEquals(TestEmployeeTable.class.getCanonicalName(),
				group.getTableName(0));
		assertTrue(group.hasTable(TestEmployeeTable.class.getCanonicalName()));
		assertFalse(group.hasTable("xxxxxx"));

		// check table retrieval
		assertEquals(employees.size(),
				group.getTable(TestEmployeeTable.class.getSimpleName()).size());
		employees.clear();

		// Make new group based on same data.
		Group group2 = new Group(data);
		TestEmployeeTable employees2 = new TestEmployeeTable(group2);
		assertEquals(3, employees2.size());
		assertEquals(NAME0, employees2.at(0).getFirstName());
		assertEquals(NAME1, employees2.at(1).getFirstName());
		assertEquals(NAME2, employees2.at(2).getFirstName());
		employees2.clear();
		group2.close();

		// Make new empty group
		Group group3 = new Group();
		TestEmployeeTable employees3 = new TestEmployeeTable(group3);
		assertEquals(0, employees3.size());
		employees3.clear();
		group3.close();

		group.close();
		System.out.println("Done");
	}
}
