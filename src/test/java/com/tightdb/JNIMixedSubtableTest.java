package com.tightdb;

import org.testng.annotations.Test;

public class JNIMixedSubtableTest {

	@Test
	public void shouldCreateSubtableInMixedTypeColumn() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "num");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		table.updateFromSpec(tableSpec);

		table.insertSubTable(1, 0);
	}

	@Test
	public void shouldCreateSubtableInMixedTypeColumn2() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "num");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		table.updateFromSpec(tableSpec);

		table.addEmptyRow();

		TableBase subtable = table.getSubTable(1, 0);
	}

}
