package com.tightdb;

import org.testng.annotations.Test;

public class JNIGroupTest {

    @Test
    public void createTableFromGroup() {
        Group group = new Group();

        Table table1 = group.createTable("myTable");
        assert(table1 != null);

    }

    @Test(expectedExceptions=RuntimeException.class)
    public void createTableWithExistingNameFromGroupShouldThrow() {
        Group group = new Group();

        Table table1 = group.createTable("myTable");
        assert(table1 != null);

        Table table2 = group.createTable("myTable");
        assert(table2 != null);
    }

    @Test
    public void getTableFromGroup() {

        Group group = new Group();
        group.createTable("myTable");
        Table table1 = group.getTable("myTable");
        assert(table1 != null);

    }

    @Test
    public void getNonExistingTableFromGroup() {

        Group group = new Group();
        Table table1 = group.getTable("myTable");
        assert(table1 == null);

    }


}
