package com.tightdb.example;

import com.tightdb.ColumnType;
import com.tightdb.Mixed;
import com.tightdb.TableBase;
import com.tightdb.TableSpec;

public class MixedTest {

	public static void main(String[] args) {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "num");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		TableSpec subspec = tableSpec.addSubtableColumn("subtable");
		subspec.addColumn(ColumnType.ColumnTypeInt, "num");
		table.updateFromSpec(tableSpec);
		
		boolean gotException = false;
		try {
			// Shouldn't work: no Mixed stored yet
			Mixed m = table.getMixed(1, 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println( "Got exception - as expected!");
		}
		
		table.addEmptyRow();
		table.setMixed(1, 0, new Mixed(ColumnType.ColumnTypeTable));
		Mixed m = table.getMixed(1, 0);
		
		ColumnType mt = table.getMixedType(1,0);
		System.out.println("m = " + m + " type: " + mt);
		
	}

}
