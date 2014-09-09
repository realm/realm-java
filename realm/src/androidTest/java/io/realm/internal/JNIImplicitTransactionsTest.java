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
        SharedGroup sg = new SharedGroup(testFile, true);

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
            assertNotNull(e);
        }

    }

}
