package com.tightdb;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

public class JNISubtableTest {

    @Test()
    public void shouldSynchronizeNestedTables() {
        Group group = new Group();
        Table table = group.getTable("emp");

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.ColumnTypeString, "name");

        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(ColumnType.ColumnTypeInt, "num");

        table.updateFromSpec(tableSpec);

        table.add("Foo", null);
        assertEquals(1, table.size());

        Table subtable1 = table.getSubTable(1, 0);
        subtable1.add(123);
        assertEquals(1, subtable1.size());
        subtable1.close();

        Table subtable2 = table.getSubTable(1, 0);
        assertEquals(1, subtable2.size());
        assertEquals(123, subtable2.getLong(0, 0));

        table.clear();
    }

    @Test()
    public void shouldInsertNestedTablesNested() {
        Group group = new Group();
        Table table = group.getTable("emp");

        // Define table
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.ColumnTypeString, "name");

        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(ColumnType.ColumnTypeInt, "num");

        tableSpec.addColumn(ColumnType.ColumnTypeInt, "Int");
        table.updateFromSpec(tableSpec);

        // Insert values
        table.add("Foo", null, 123456);
        table.getSubTable(1, 0).add(123);
        assertEquals(1, table.getSubTable(1, 0).size());
        assertEquals(123, table.getSubTable(1, 0).getLong(0,0));

        assertEquals(1, table.size());
    }


}
