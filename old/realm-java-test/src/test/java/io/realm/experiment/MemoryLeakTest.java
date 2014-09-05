package io.realm.experiment;


import io.realm.TableQuery;
import io.realm.TableView;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import io.realm.ColumnType;
import io.realm.Table;

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
            AssertJUnit.assertEquals(i, table.getLong(0,0) );
            view.close();

            query.close();

            table.close();
        }
        // System.out.println("End mem test");
    }
}
