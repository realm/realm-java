package io.realm.internal;

import junit.framework.TestCase;

import java.io.File;

public class JNIImplicitTransactionsTest extends TestCase {

    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists())
            f.delete();
        f = new File(filename + ".lock");
        if (f.exists())
            f.delete();
    }

    public void testImplicitTransactions() {

        deleteFile("implicit.realm");
        SharedGroup sg = new SharedGroup("implicit.realm", true);

        WriteTransaction wt = sg.beginWrite();

        if(!wt.hasTable("test")) {
            Table table = wt.getTable("test");
            table.addColumn(ColumnType.INTEGER, "integer");
            table.addEmptyRow();
        }

        wt.commit();

        ImplicitTransaction t = sg.beginImplicitTransaction();

        Table test = t.getTable("test");


        assertEquals(1, test.size());

        t.promoteToWrite();

        test.addEmptyRow();

        t.commitAndContinueAsRead();

        // Should throw as this is now a read transaction
        try {
            test.addEmptyRow();
            fail();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            assertNotNull(e);
        }

    }

}
