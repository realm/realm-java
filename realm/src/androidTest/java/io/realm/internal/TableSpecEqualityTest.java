package io.realm.internal;

import junit.framework.TestCase;

public class TableSpecEqualityTest extends TestCase {

    public void testShouldMatchIdenticalSimpleSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.BOOLEAN, "bar");

        assertTrue(spec1.equals(spec2));
    }

    public void testShouldntMatchSpecsWithDifferentColumnNames() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.BOOLEAN, "bar2");

        assertFalse(spec1.equals(spec2));
    }

    public void testShouldntMatchSpecsWithDifferentColumnTypes() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(ColumnType.STRING, "foo");
        spec1.addColumn(ColumnType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(ColumnType.STRING, "foo");
        spec2.addColumn(ColumnType.BINARY, "bar");

        assertFalse(spec1.equals(spec2));
    }

    public void testShouldMatchDeepRecursiveIdenticalSpecs() {
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

    public void testShouldNotMatchDeepRecursiveDifferentSpecs() {
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
