package com.tightdb;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

public class TableSpecEqualityTest {

    @Test
    public void shouldMatchIdenticalSimpleSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.ColumnTypeString, "foo");
        spec1.addColumn(ColumnType.ColumnTypeBool, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.ColumnTypeString, "foo");
        spec2.addColumn(ColumnType.ColumnTypeBool, "bar");

        assertTrue(spec1.equals(spec2));
    }

    @Test
    public void shouldntMatchSpecsWithDifferentColumnNames() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.ColumnTypeString, "foo");
        spec1.addColumn(ColumnType.ColumnTypeBool, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.ColumnTypeString, "foo");
        spec2.addColumn(ColumnType.ColumnTypeBool, "bar2");

        assertFalse(spec1.equals(spec2));
    }
    
    @Test
    public void shouldntMatchSpecsWithDifferentColumnTypes() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.ColumnTypeString, "foo");
        spec1.addColumn(ColumnType.ColumnTypeBool, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.ColumnTypeString, "foo");
        spec2.addColumn(ColumnType.ColumnTypeBinary, "bar");

        assertFalse(spec1.equals(spec2));
    }

    @Test
    public void shouldMatchDeepRecursiveIdenticalSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.ColumnTypeString, "foo");
        spec1.addColumn(ColumnType.ColumnTypeTable, "bar");
        spec1.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeInt, "x");
        spec1.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeTable, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.ColumnTypeBool, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.ColumnTypeString, "foo");
        spec2.addColumn(ColumnType.ColumnTypeTable, "bar");
        spec2.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeInt, "x");
        spec2.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeTable, "sub");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.ColumnTypeBool, "b");

        assertTrue(spec1.equals(spec2));
    }

    @Test
    public void shouldntMatchDeepRecursiveDifferentSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.ColumnTypeString, "foo");
        spec1.addColumn(ColumnType.ColumnTypeTable, "bar");
        spec1.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeInt, "x");
        spec1.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeTable, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.ColumnTypeBool, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.ColumnTypeString, "foo");
        spec2.addColumn(ColumnType.ColumnTypeTable, "bar");
        spec2.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeInt, "x");
        spec2.getSubtableSpec(1).addColumn(ColumnType.ColumnTypeTable, "sub2");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(ColumnType.ColumnTypeBool, "b");

        assertFalse(spec1.equals(spec2));
    }
    
}
