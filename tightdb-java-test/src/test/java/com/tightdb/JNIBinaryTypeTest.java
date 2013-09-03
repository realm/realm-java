package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.typed.TightDB;

public class JNIBinaryTypeTest {

    protected Table table;
    protected byte [] testArray = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

    @BeforeMethod
    public void init() {
        TightDB.loadLibrary();
        //util.setDebugLevel(0); //Set to 1 to see more JNI debug messages

        table = new Table();

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.BINARY, "bin");
        table.updateFromSpec(tableSpec);
    }

    @Test
    public void shouldStoreValuesOfBinaryType_ByteArray() throws Exception {
        table.add(testArray);
        checkBinaryCell(table, 0, 0, ColumnType.BINARY, testArray);
    }

    @Test
    public void shouldStoreValuesOfBinaryType_ByteBuffer_Direct() throws Exception {
        ByteBuffer bufDirect = ByteBuffer.allocateDirect(testArray.length);
        bufDirect.put(testArray);
        table.add(bufDirect);
        checkBinaryCell(table, 0, 0, ColumnType.BINARY, testArray);
    }

    // TODO: handle wrap ByteBuffers
    @Test (enabled = false)
    public void shouldStoreValuesOfBinaryType_ByteBuffer_wrap() throws Exception {
        // This way of using ByteBuffer fails. It's not a "DirectBuffer"
        ByteBuffer bufWrap = ByteBuffer.wrap(testArray);
        table.add(bufWrap);

        checkBinaryCell(table, 0, 0, ColumnType.BINARY, testArray);
    }

    private void checkBinaryCell(Table table, long col, long row, ColumnType columnType, byte[] value) throws IllegalAccessException {
        byte[] bin = table.getBinaryByteArray(col, row);
        assertEquals(value, bin);

        ByteBuffer binBuf = table.getBinaryByteBuffer(col, row);
        assertEquals(ByteBuffer.wrap(value), binBuf);
    }

    @Test
    public void test() {
        ByteBuffer buf = ByteArrayToByteBuffer(testArray);
        byte[] arr = ByteBufferToByteArray(buf);

        assertEquals(testArray, arr);
    }

    private static byte[] ByteBufferToByteArray(ByteBuffer buf) {
        buf.flip();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return bytes;
    }

    private static ByteBuffer ByteArrayToByteBuffer(byte[] bytes) {
        ByteBuffer bufDirect = ByteBuffer.allocateDirect(bytes.length);
        bufDirect.put(bytes);
        return bufDirect;
    }

}
