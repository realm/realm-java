package com.tightdb;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

public class JNIMixedSubtableTest {

	@Test
	public void shouldCreateSubtableInMixedTypeColumn() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "num");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		TableSpec subspec = tableSpec.addSubtableColumn("subtable");
		subspec.addColumn(ColumnType.ColumnTypeInt, "num");
		table.updateFromSpec(tableSpec);

		// Shouln't work: no Mixed stored yet
		//Mixed m1 = table.getMixed(1, 0);
		//ColumnType mt = table.getMixedType(1,0);
		
		// You can't "getSubTable()" unless there is one. And the addEmptyRow will put in a Mixed(0) as default.
		// You now get an exception instead of crash if you try anyway
		{
			table.addEmptyRow();
			
			boolean gotException = false;
			try {
				@SuppressWarnings("unused")
				TableBase subtable = table.getSubTable(1, 0);
			} catch (IllegalArgumentException e) {
				gotException = true;
			}
			assertEquals(true, gotException);
			table.removeLast();
		}
		
		long ROW = 0;
		boolean simple = true;
		// Add empty row - the simple way
		if (simple) {
			table.addEmptyRow();
			table.setMixed(1, ROW, new Mixed(ColumnType.ColumnTypeTable));
		} else {
			// OR Add "empty" row - the "manual" way
			table.insertLong(0, ROW, 0);
			table.insertMixed(1, ROW, new Mixed(ColumnType.ColumnTypeTable)); 	// Mixed subtable
			table.insertSubTable(2, ROW);										// Normal subtable
			table.insertDone();
		}
		assertEquals(1, table.size());
		assertEquals(0, table.getSubTableSize(1, 0));
		
		// Create schema for the one Mixed cell with a subtable
		TableBase subtable = table.getSubTable(1, ROW);
		TableSpec subspecMixed = subtable.getTableSpec();
		subspecMixed.addColumn(ColumnType.ColumnTypeInt, "num");
		subtable.updateFromSpec(subspecMixed);

		// Insert value in the Mixed subtable
		subtable.insertLong(0, 0, 27);
		subtable.insertDone();
		subtable.insertLong(0, 1, 273);
		subtable.insertDone();
		assertEquals(2, subtable.size());
		assertEquals(2, table.getSubTableSize(1, ROW));
		assertEquals(27, subtable.getLong(0, ROW));
		assertEquals(273, subtable.getLong(0, ROW+1));
	}

	@SuppressWarnings("unused")
	@Test
	public void shouldCreateSubtableInMixedTypeColumn2() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "num");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		table.updateFromSpec(tableSpec);

		table.addEmptyRow();
		table.setMixed(1, 0, new Mixed(ColumnType.ColumnTypeTable));
		
		TableBase subtable = table.getSubTable(1, 0);
	}

}
