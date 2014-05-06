package com.realm;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

public class TableSpecEqualityTest {

    @Test
    public void shouldMatchIdenticalSimpleSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.BOOLEAN, "bar");

        assertTrue(spec1.equals(spec2));
    }

    @Test
    public void shouldntMatchSpecsWithDifferentColumnNames() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.BOOLEAN, "bar2");

        assertFalse(spec1.equals(spec2));
    }

    @Test
    public void shouldntMatchSpecsWithDifferentColumnTypes() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.BINARY, "bar");

        assertFalse(spec1.equals(spec2));
    }

    @Test
    public void shouldMatchDeepRecursiveIdenticalSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.TABLE, "bar");
        spec1.getSubtableSpec(1).addColumn(ColumnType.INTEGER, "x");
        spec1.getSubtableSpec(1).addColumn(ColumnType.TABLE, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.BOOLEAN, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.TABLE, "bar");
        spec2.getSubtableSpec(1).addColumn(ColumnType.INTEGER, "x");
        spec2.getSubtableSpec(1).addColumn(ColumnType.TABLE, "sub");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.BOOLEAN, "b");

        assertTrue(spec1.equals(spec2));
    }

    @Test
    public void shouldntMatchDeepRecursiveDifferentSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.TABLE, "bar");
        spec1.getSubtableSpec(1).addColumn(ColumnType.INTEGER, "x");
        spec1.getSubtableSpec(1).addColumn(ColumnType.TABLE, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.BOOLEAN, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.TABLE, "bar");
        spec2.getSubtableSpec(1).addColumn(ColumnType.INTEGER, "x");
        spec2.getSubtableSpec(1).addColumn(ColumnType.TABLE, "sub2");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.BOOLEAN, "b");

        assertFalse(spec1.equals(spec2));
    }

}
