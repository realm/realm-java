package io.realm.internal;

import junit.framework.TestCase;

public class TableIndexAndDistinctTest extends TestCase {
    Table table;

    void init() {
        table = new Table();
        table.addColumn(ColumnType.INTEGER, "number");
        table.addColumn(ColumnType.STRING, "name");

        long i = 0;
        table.add(0, "A");
        table.add(1, "B");
        table.add(2, "C");
        table.add(3, "B");
        table.add(4, "D");
        table.add(5, "D");
        table.add(6, "D");
        assertEquals(7, table.size());
    }

    public void testShouldTestDistinct() {
        init();

        // Must set index before using distinct()
        table.addSearchIndex(1);
        assertEquals(true, table.hasSearchIndex(1));

        TableView view = table.getDistinctView(1);
        assertEquals(4, view.size());
        assertEquals(0, view.getLong(0, 0));
        assertEquals(1, view.getLong(0, 1));
        assertEquals(2, view.getLong(0, 2));
        assertEquals(4, view.getLong(0, 3));
    }

// TODO: parametric test
/*    *//**
     * Should throw exception if trying to get distinct on columns where index has not been set
     * @param index
     *//*

    @Test(expectedExceptions = UnsupportedOperationException.class, dataProvider = "columnIndex")
    public void shouldTestDistinctErrorWhenNoIndex(Long index) {

        //Get a table with all available column types
        Table t = TestHelper.getTableWithAllColumnTypes();

        TableView view = table.getDistinctView(1);
    }*/

    public void testShouldTestDistinctErrorWhenIndexOutOfBounds() {
        init();

        try {
            TableView view = table.getDistinctView(3);
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertNotNull(e);
        }
    }

    /**
     * Check that Index can be set on multiple columns, with the String
     * @param
     */
    public void testShouldTestSettingIndexOnMultipleColumns() {

        //Create a table only with String type columns
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "col1");
        t.addColumn(ColumnType.STRING, "col2");
        t.addColumn(ColumnType.STRING, "col3");
        t.addColumn(ColumnType.STRING, "col4");
        t.addColumn(ColumnType.STRING, "col5");
        t.add("row1", "row2", "row3", "row4", "row5");
        t.add("row1", "row2", "row3", "row4", "row5");
        t.add("row1", "row2", "row3", "row4", "row5");
        t.add("row1", "row2", "row3", "row4", "row5");
        t.add("row1", "row2", "row3", "row4", "row5");

        for (long c=0;c<t.getColumnCount();c++){
            t.addSearchIndex(c);
            assertEquals(true, t.hasSearchIndex(c));
        }
    }


// TODO: parametric test
/*    *//**
     * Checks that all other column types than String throws exception.
     * @param o
     *//*

    @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "columnIndex")
    public void shouldTestIndexOnWrongColumnType(Long index) {

        //Get a table with all available column types
        Table t = TestHelper.getTableWithAllColumnTypes();

        //If column type is String, then throw the excepted exception
        if (t.getColumnType(index).equals(ColumnType.STRING)){
            throw new IllegalArgumentException();
        }

        t.addSearchIndex(index);
    }*/

    public void testShouldCheckIndexIsOkOnColumn() {
        init();
        table.addSearchIndex(1);
    }

    public void testShouldThrowDistinctErrorWhenWrongColumnType() {
        init();
        table.addSearchIndex(1);
        try {
            TableView view = table.getDistinctView(0);
            fail();
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }
    }

    /**
     * Is used to run a test multiple times,
     * that corresponds to the number of columns in the Table generated
     * in TestHelper.getTableWithAllColumnTypes
     * @return
     */

// TODO: parametric test
/*    @DataProvider(name = "columnIndex")
    public Iterator<Object[]> mixedValuesProvider() {
        Long[] values = {
               0L,1L,2L,3L,4L,5L,6L,7L,8L
        };

        List<?> mixedValues = Arrays.asList(values);
        return DataProviderUtil.allCombinations(mixedValues);
    }*/

    public void testUnsetIndex() {
        init();
        table.addSearchIndex(1);
        assertEquals(true, table.hasSearchIndex(1));

        table.removeSearchIndex(1);
        assertEquals(false, table.hasSearchIndex(1));
    }

    public void testUnsetIndexNoop() {
        init();
        assertEquals(false, table.hasSearchIndex(1));

        // remove index from non-indexed column is a no-op
        table.removeSearchIndex(1);
        assertEquals(false, table.hasSearchIndex(1));
    }
}

