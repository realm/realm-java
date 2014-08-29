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



    }

    @Test
    public void testLinkList() {

        Group group = new Group();

        Table table1 = group.getTable("table1");
        table1.addColumn(ColumnType.INTEGER, "int");
        table1.addColumn(ColumnType.STRING, "string");
        table1.add(1, "c");
        table1.add(2, "b");
        table1.add(3, "a");


        Table table2 = group.getTable("table2");

        table2.addColumnLink(ColumnType.LINK_LIST, "LinkList", table1);

        table2.insertLinkList(0,0);

        LinkView links = table2.getRow(0).getLinkList(0);

        assertEquals(links.isEmpty(), true);
        assertEquals(links.size(), 0);

        links.add(2);
        links.add(1);

        assertEquals(links.isEmpty(), false);
        assertEquals(links.size(), 2);

        assertEquals(links.get(0).getColumnName(1), "string");

        assertEquals(links.get(0).getString(1), "a");

        links.move(1, 0);

        assertEquals(links.get(0).getString(1), "b");

        links.remove(0);

        assertEquals(links.get(0).getString(1), "a");
        assertEquals(links.size(), 1);


    }

}
