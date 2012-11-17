package com.tightdb;

import java.util.Date;

public class InsertMixedFailureTest {

	public static void main(String[] args) {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		table.updateFromSpec(tableSpec);

		// throws Illegal Argument: nativeSetMixed() when a Date instance is inserted
		table.insertMixed(0, 0, Mixed.mixedValue(new Date()));
		table.insertDone();
		
	}
	
}
