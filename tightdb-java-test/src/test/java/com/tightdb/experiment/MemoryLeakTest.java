package com.tightdb.experiment;


import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableQuery;
import com.tightdb.TableView;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

// Execute this test with Memory leak detector enabled.

public class MemoryLeakTest {

    @Test
    public void testMemoryManagement() throws Throwable {

        //System.out.println("Begin mem test");

        for (int i = 0; i < 10000; i++) {

            Table table = new Table();
            table.addColumn(ColumnType.INTEGER, "myint");
            table.add(i);

            TableQuery query = table.where();

            TableView view = query.notEqualTo(0, 2).findAll();
            assertEquals(i, table.getLong(0,0) );
            view.finalize();

            query.finalize();

            table.finalize();
        }
        // System.out.println("End mem test");
    }
}
