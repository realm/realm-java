package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.AfterMethod;
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
        table.addColumn(ColumnType.BINARY, "bin");
    }

    @AfterMethod
    public void close() {
        //table.close();
        table = null;
    }


}
