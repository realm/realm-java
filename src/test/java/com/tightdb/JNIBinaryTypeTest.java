package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

public class JNIBinaryTypeTest {

	@Test
	public void shouldStoreValuesOfMixedType() throws Exception {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeBinary, "bin");
		table.updateFromSpec(tableSpec);

		table.insertBinary(0, 0, new byte[] { 1, 2, 3 });
		table.insertDone();

		checkMixedCell(table, 0, 0, ColumnType.ColumnTypeBinary, new byte[] { 1, 2, 3 });
	}

	private void checkMixedCell(TableBase table, long col, long row, ColumnType columnType, byte[] value) throws IllegalAccessException {
		byte[] bin = table.getBinaryByteArray(col, row);
		assertEquals(value, bin);
		
		ByteBuffer binBuf = table.getBinaryByteBuffer(col, row);
		assertEquals(ByteBuffer.wrap(value), binBuf);
		
	}

}
