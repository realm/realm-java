package io.realm.internal;

import android.test.AndroidTestCase;
import android.util.Log;

import junit.framework.AssertionFailedError;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import io.realm.Realm;

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
        SharedGroup sg = new SharedGroup(testFile, true, SharedGroup.Durability.FULL, null); // TODO: try with encryption

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

    public void testVersionIdInBg() throws Throwable {
        deleteFile();
        SharedGroup sg = new SharedGroup(testFile, true, SharedGroup.Durability.FULL, null); // TODO: try with encryption

//        ReadTransaction readTransaction = sg.beginRead();
//        sg.promoteToWrite();
//        Table table = readTransaction.getTable("test");
//        table.addColumn(ColumnType.INTEGER, "integer");
//        table.addEmptyRow();
//        sg.commitAndContinueAsRead();

        // Create a table
        WriteTransaction wt = sg.beginWrite();
        if (!wt.hasTable("test")) {
            Table table = wt.getTable("test");
            table.addColumn(ColumnType.INTEGER, "integer");
            table.addEmptyRow();
        }
//        wt.commit();
        sg.commitAndContinueAsRead();

//        sg.advanceRead();

        final SharedGroup.VersionID versionID = sg.getVersion();
        Log.d("REALM", ">>>>>>>>>>>>>>>>>>>>>>> testVersionIdInBg versionID=" + versionID.toString());
        final Throwable[] threadAssertionError = new Throwable[1];
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);

        new Thread() {
            @Override
            public void run() {
                SharedGroup sg = new SharedGroup(testFile, true, SharedGroup.Durability.FULL, null);
                sg.beginImplicitTransaction();
                SharedGroup.VersionID versionIdBg = sg.getVersion();
                Log.d("REALM", ">>>>>>>>>>>>>>>>>>>>>>> background versionID=" + versionID.toString());
                try {
                    assertEquals(0, versionID.compareTo(versionIdBg));
                } catch (AssertionFailedError e) {
                    threadAssertionError[0] = e;
                } finally {
                    sg.close();
                    signalCallbackFinished.countDown();
                }
            }
        }.start();
        sg.close();
        signalCallbackFinished.await();

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }

    }

    public void testCannotUseClosedImplicitTransaction() {
        deleteFile();
        SharedGroup sg = new SharedGroup(testFile, true, SharedGroup.Durability.FULL, null);
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
