package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.lib.TightDB;

public class JNIBinaryTypeTest {

	protected TableBase table;
	protected byte [] testArray = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
	
	@BeforeMethod
	public void init() {
		TightDB.loadLibrary();
		//util.setDebugLevel(0); //Set to 1 to see more JNI debug messages
		
		table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeBinary, "bin");
		table.updateFromSpec(tableSpec);	
	}
	
	@Test
	public void shouldStoreValuesOfBinaryType_ByteArray() throws Exception {
		table.add(testArray);
		checkBinaryCell(table, 0, 0, ColumnType.ColumnTypeBinary, testArray);
	}
	
	@Test
	public void shouldStoreValuesOfBinaryType_ByteBuffer_Direct() throws Exception {
		ByteBuffer bufDirect = ByteBuffer.allocateDirect(testArray.length);
		bufDirect.put(testArray);
		table.add(bufDirect);		
		checkBinaryCell(table, 0, 0, ColumnType.ColumnTypeBinary, testArray);
	}
	
	// TODO: handle wrap ByteBuffers
	@Test (enabled = false)
	public void shouldStoreValuesOfBinaryType_ByteBuffer_wrap() throws Exception {
		// This way of using ByteBuffer fails. It's not a "DirectBuffer"
		ByteBuffer bufWrap = ByteBuffer.wrap(testArray);
		table.add(bufWrap);

		checkBinaryCell(table, 0, 0, ColumnType.ColumnTypeBinary, testArray);
	}

	private void checkBinaryCell(TableBase table, long col, long row, ColumnType columnType, byte[] value) throws IllegalAccessException {
		byte[] bin = table.getBinaryByteArray(col, row);
		assertEquals(value, bin);
		
		ByteBuffer binBuf = table.getBinaryByteBuffer(col, row);
		assertEquals(ByteBuffer.wrap(value), binBuf);
	}

}
