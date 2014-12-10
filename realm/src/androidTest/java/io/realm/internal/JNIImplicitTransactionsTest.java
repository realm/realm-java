package io.realm.internal;

import android.test.AndroidTestCase;

import java.io.File;
import java.util.Arrays;

public class JNIImplicitTransactionsTest extends AndroidTestCase {

    String testFile;

    @Override
    protected void setUp() throws Exception {
        testFile = new File(this.getContext().getFilesDir(), "implicit.realm").toString();
    }

    private void deleteFile() {
        for (String fileToDelete : Arrays.asList(testFile, testFile + ".lock")) {
            File f = new File(fileToDelete);
            if (f.exists()) {
                boolean result = f.delete();
                if (!result) {
                    fail();
                }
            }
        }
    }

    public void testImplicitTransactions() {
        deleteFile();
        SharedGroup sg = new SharedGroup(testFile, true, null); // TODO: try with encryption

        // Create a table
        WriteTransaction wt = sg.beginWrite();
        if (!wt.hasTable("test")) {
            Table table = wt.getTable("test");
            table.addColumn(ColumnType.INTEGER, "integer");
            table.addEmptyRow();
        }
        wt.commit();

        // Add a row in a write transaction and continue with read transaction
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
            assertNotNull(e);
        }
    }

    public void testCannotUseClosedImplicitTransaction() {
        SharedGroup sg = new SharedGroup(testFile, true, null);
        WriteTransaction wt = sg.beginWrite();
        if (!wt.hasTable("test")) {
            Table table = wt.getTable("test");
            table.addColumn(ColumnType.INTEGER, "integer");
            table.addEmptyRow();
        }
        wt.commit();
        ImplicitTransaction t = sg.beginImplicitTransaction();

        sg.close();
        try {
            t.advanceRead();
        } catch (IllegalStateException e) {
            return;
        }

        fail("It should not be possible to advanceRead on a transaction which SharedGroup is closed");
    }
}
