package io.realm;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.AssertJUnit.assertEquals;

public class JNIImplicitTransactions {

    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists())
            f.delete();
        f = new File(filename + ".lock");
        if (f.exists())
            f.delete();
    }

    @Test
    public void testImplicitTransactions() {

        deleteFile("implicit.realm");
        SharedGroup sg = new SharedGroup("implicit.realm", true);

        WriteTransaction wt = sg.beginWrite();

        Table table = wt.getTable("test");
        table.addColumn(ColumnType.INTEGER, "integer");
        table.addEmptyRow();

        wt.commit();

        ReadTransaction rt = sg.beginRead();

        Table test = rt.getTable("test");


        assertEquals(1, test.size());

        sg.promoteToWrite();

        test.addEmptyRow();

        sg.commitAndContinueAsRead();

        assertEquals(2, test.size());

        test.addEmptyRow();

        System.out.println(test.size());






    }

}
