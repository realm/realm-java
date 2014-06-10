package io.realm;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JNILinkTest {

    @Test
    public void testLinkColumns() {

        Group group = new Group();

        Table table1 = group.getTable("table1");


        Table table2 = group.getTable("table2");
        table2.addColumn(ColumnType.INTEGER, "int");
        table2.addColumn(ColumnType.STRING, "string");

        table2.add(1, "c");
        table2.add(2, "b");
        table2.add(3, "a");

        table1.addColumnLink(ColumnType.LINK, "Link", table2);


        table1.addEmptyRow();
        table1.setLink(0, 0, 1);

        Table target = table1.getLinkTarget(0);

        System.gc();


        assertEquals(target.getColumnCount(), 2);


        String test = target.getString(1, table1.getLink(0, 0));

        assertEquals(test, "b");

        group.close();


    }

}
