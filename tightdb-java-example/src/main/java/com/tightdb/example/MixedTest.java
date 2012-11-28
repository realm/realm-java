package com.tightdb.example;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Mixed;
import com.tightdb.TableBase;
import com.tightdb.TableSpec;

public class MixedTest {

	@Test
	public void should() {
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
			gotException = true;
		}
		assertEquals(true, gotException);
		
		table.addEmptyRow();
		table.setMixed(1, 0, new Mixed(ColumnType.ColumnTypeTable));
		Mixed m = table.getMixed(1, 0);
		
		ColumnType mt = table.getMixedType(1,0);
		System.out.println("m = " + m + " type: " + mt);
		
	}

}
