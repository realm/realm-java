package com.tightdb;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

public class JNIMixedTypeTest {

	@Test
	public void shouldStoreValuesOfMixedType() throws Exception {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "foo");
		table.updateFromSpec(tableSpec);

		table.insertMixed(0, 0, new Mixed("str1"));
		table.insertDone();

		checkMixedCell(table, 0, 0, ColumnType.ColumnTypeString, "str1");

		table.setMixed(0, 0, new Mixed(true));

		checkMixedCell(table, 0, 0, ColumnType.ColumnTypeBool, true);
	}

	private void checkMixedCell(TableBase table, long col, long row, ColumnType columnType, Object value) throws IllegalAccessException {
		ColumnType mixedType = table.getMixedType(col, row);
		assertEquals(columnType, mixedType);

		Mixed mixed = table.getMixed(col, row);
		assertEquals(value, mixed.getValue());
	}

}
