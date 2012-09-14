package com.tightdb.experiment;

import com.tightdb.ColumnType;
import com.tightdb.TableBase;
import com.tightdb.TableSpec;

public class ClearTableFailureTest {

	public static void main(String[] args) {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeDate, "dd");
		table.updateFromSpec(tableSpec);
		
		table.clear();

	}

}
