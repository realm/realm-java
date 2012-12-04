package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

@SuppressWarnings("unused")
public class JNIDistinctTest {
	TableBase table;
	
	void init() {
		table = new TableBase();
		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "number");
		tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
		table.updateFromSpec(tableSpec);

		long i = 0;
		table.insertLong(0, i, i); table.insertString(1, i++, "A"); table.insertDone();
		table.insertLong(0, i, i); table.insertString(1, i++, "B"); table.insertDone();
		table.insertLong(0, i, i); table.insertString(1, i++, "C"); table.insertDone();
		table.insertLong(0, i, i); table.insertString(1, i++, "B"); table.insertDone();
		table.insertLong(0, i, i); table.insertString(1, i++, "D"); table.insertDone();
		table.insertLong(0, i, i); table.insertString(1, i++, "D"); table.insertDone();
		table.insertLong(0, i, i); table.insertString(1, i++, "D"); table.insertDone();
		assertEquals(7, table.size());
	}

	@Test
	public void shouldTestDistinct() {
		init();
		
		// Must set index before using distinct()
		table.setIndex(1);
		assertEquals(true, table.hasIndex(1));
		
		TableViewBase view = table.distinct(1);
		assertEquals(4, view.size());
		assertEquals(0, view.getLong(0, 0));
		assertEquals(1, view.getLong(0, 1));
		assertEquals(2, view.getLong(0, 2));
		assertEquals(4, view.getLong(0, 3));
	}
	
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldTestDistinctErrorWhenNoIndex() {
		init();
		TableViewBase view = table.distinct(1);	
	}

	@Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
	public void shouldTestDistinctErrorWhenIndexOutOfBounds() {
		init();
		
		TableViewBase view = table.distinct(3);	
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldTestDistinctErrorWhenWrongColumnType() {
		init();
		table.setIndex(0);
		TableViewBase view = table.distinct(0);	
	}

}