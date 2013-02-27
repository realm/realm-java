package com.tightdb.typed;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.Group;
import com.tightdb.test.TestEmployeeTable;

@SuppressWarnings("unused")
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

/* TODO: Enable when implemented "free" method for the data
	@Test(enabled = true)
	public void groupByteBufferCanClose() {
		Group group = new Group();
		ByteBuffer data = group.writeToByteBuffer();
		group.close();

		Group group2 = new Group(data);
		group2.close();
	}
*/
	@Test(enabled = true)
	public void groupMemCanClose() {
		Group group = new Group();
		byte[] data = group.writeToMem();
		group.close();

		Group group2 = new Group(data);
		group2.close();
	}

	@Test(enabled = true)
	public void shouldCreateTablesInGroup() {
		//util.setDebugLevel(2);
		Group group = new Group();

		TestEmployeeTable employees = new TestEmployeeTable(group);
		employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 },
				new Date(), "extra", null);
		employees.add(NAME2, "B. Good", 20000, true, new byte[] { 1, 2, 3 },
				new Date(), true, null);
		employees.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4,
				5 }, new Date(), 1234, null);

		byte[] data = group.writeToMem();
		
		// check table info retrieval
		assertEquals(1, group.size());
		assertEquals(TestEmployeeTable.class.getSimpleName(),
				group.getTableName(0));
		assertTrue(group.hasTable(TestEmployeeTable.class.getSimpleName()));
		assertFalse(group.hasTable("xxxxxx"));

		// check table retrieval
		assertEquals(employees.size(),
				group.getTable(TestEmployeeTable.class.getSimpleName()).size());
		employees.clear();
		group.close();

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

	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void groupByteBufferChecksForNull() {
		ByteBuffer data = null;
		Group group = new Group(data);	
		// Expect to throw exception
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void groupByteBufferChecksForDatabaseFormat() {
		ByteBuffer data = ByteBuffer.allocateDirect(5);
		Group group = new Group(data);	
		// Expect to throw exception
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void groupByteArrayChecksForDatabaseFormat() {
		byte[] data = {1,2,3,4,5};
		Group group = new Group(data);	
		// Expect to throw exception
	}
}
