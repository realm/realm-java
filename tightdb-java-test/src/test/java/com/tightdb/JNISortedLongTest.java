package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

@SuppressWarnings("unused")
public class JNISortedLongTest {
	Table table;
	
	void init() {
		table = new Table();
		table.addColumn(ColumnType.ColumnTypeInt, "number");
		table.addColumn(ColumnType.ColumnTypeString, "name");

		table.add(1, "A");
		table.add(10, "B");
		table.add(20, "C");
		table.add(30, "B");
		table.add(40, "D");
		table.add(50, "D");
		table.add(60, "D");
		assertEquals(7, table.size());
	}

	@Test(enabled=false)	// TODO: enable
	public void shouldTestSortedInt() {
		init();
		long pos;
		boolean found;
		
		// Find first insert position
		pos = table.findSortedLong(0, 0);
		found = (table.getLong(0, pos) == 0);
		assertEquals(0, pos);
		assertEquals(false, found);
	
		// find middle match
		pos = table.findSortedLong(0, 40);
		found = (table.getLong(0, pos) == 40);
		assertEquals(4, pos);
		assertEquals(true, found);

		// find mindle insert position
		pos = table.findSortedLong(0, 41);
		found = (table.getLong(0, pos) == 41);
		assertEquals(5, pos);
		assertEquals(false, found);

		// find last insert position
		pos = table.findSortedLong(0, 100);
		found = (pos < table.size() && (table.getLong(0, pos) == 100) );
		assertEquals(7, pos);
		assertEquals(false, found);

		// find last match 
		pos = table.findSortedLong(0, 60);
		found = (table.getLong(0, pos) == 60);
		assertEquals(6, pos);
		assertEquals(true, found);
		
	}
}	
