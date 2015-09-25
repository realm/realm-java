package io.realm.internal;

import junit.framework.TestCase;

import io.realm.RealmFieldType;

public class JNIBinaryTypeTest extends TestCase {

    protected Table table;
    protected byte [] testArray = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

    @Override
    public void setUp() {
        RealmCore.loadLibrary();
        //util.setDebugLevel(0); //Set to 1 to see more JNI debug messages

        table = new Table();
        table.addColumn(RealmFieldType.BINARY, "bin");
    }

    @Override
    public void tearDown() {
        //table.close();
        table = null;
    }


}
