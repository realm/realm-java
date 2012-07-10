package com.tightdb;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

public class JNISubtableTest {
	
	@Test(enabled = true)
	public void shouldSynchronizeNestedTables() {
		Group group = new Group();
		TableBase table = group.getTable("emp");

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeString, "name");

		TableSpec subspec = tableSpec.addSubtableColumn("sub");
		subspec.addColumn(ColumnType.ColumnTypeInt, "num");

		table.updateFromSpec(tableSpec);

		table.insertString(0, 0, "Foo");
		table.insertSubTable(1, 0);
		table.insertDone();
		assertEquals(1, table.size());

		TableBase subtable1 = table.getSubTable(1, 0);
		subtable1.insertLong(0, 0, 123);
		subtable1.insertDone();
		assertEquals(1, subtable1.size());
		subtable1.close();
		
		TableBase subtable2 = table.getSubTable(1, 0);
		assertEquals(1, subtable2.size());
		assertEquals(123, subtable2.getLong(0, 0));

		table.clear();
	}
	

}
