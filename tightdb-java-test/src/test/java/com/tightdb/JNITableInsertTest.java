package com.tightdb;

import java.nio.ByteBuffer;
import java.util.Date;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


public class JNITableInsertTest {

	@Test()
	public void ShouldInsertMixed() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		table.updateFromSpec(tableSpec);

		table.insertMixed(0, 0, new Mixed(new Date()));
		table.insertDone();
		
	}
	
	@Test()
	public void ShouldInsert() {
		TableBase table = new TableBase();
		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeBool, "bool");
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "number");
		tableSpec.addColumn(ColumnType.ColumnTypeString, "string");
		tableSpec.addColumn(ColumnType.ColumnTypeBinary, "Bin");
		tableSpec.addColumn(ColumnType.ColumnTypeDate, "date");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		TableSpec subspec = tableSpec.addSubtableColumn("sub");
		subspec.addColumn(ColumnType.ColumnTypeInt, "sub-num");
		table.updateFromSpec(tableSpec);
		
		ByteBuffer buf = ByteBuffer.allocateDirect(23);
		Mixed mixedSubTable = new Mixed(ColumnType.ColumnTypeTable);
		table.insertRow(0, false, 2, "hi", new byte[] {0,2,3}, new Date(), new Mixed("mix1"), null);
		table.insertRow(1, true, 12345567789L, "hello", new byte[] {0}, new Date(123), new Mixed(new byte[] {23}), null);
		table.insertRow(2, false, 2, "hi", buf, new Date(), new Mixed(123), null);
		assertEquals(2, table.getLong(1,0));
		try {
			table.insertRow(0, false);
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			// expected
		} 
		
	}
	
}
