package io.realm;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.AssertJUnit.assertEquals;

public class JNIImplicitTransactionsTest {

    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists())
            f.delete();
        f = new File(filename + ".lock");
        if (f.exists())
            f.delete();
    }

    @Test(expectedExceptions=IllegalStateException.class)
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
        test.addEmptyRow();

    }

}
