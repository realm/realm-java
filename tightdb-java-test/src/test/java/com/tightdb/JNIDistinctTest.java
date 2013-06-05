package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

@SuppressWarnings("unused")
public class JNIDistinctTest {
    Table table;
    
    void init() {
        table = new Table();
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.ColumnTypeInt, "number");
        tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
        table.updateFromSpec(tableSpec);

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

    @Test
    public void shouldTestDistinct() {
        init();
        
        // Must set index before using distinct()
        table.setIndex(1);
        assertEquals(true, table.hasIndex(1));
        
        TableView view = table.distinct(1);
        assertEquals(4, view.size());
        assertEquals(0, view.getLong(0, 0));
        assertEquals(1, view.getLong(0, 1));
        assertEquals(2, view.getLong(0, 2));
        assertEquals(4, view.getLong(0, 3));
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldTestDistinctErrorWhenNoIndex() {
        init();
        TableView view = table.distinct(1); 
    }

    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void shouldTestDistinctErrorWhenIndexOutOfBounds() {
        init();
        
        TableView view = table.distinct(3); 
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldTestIndexOnWrongColumnType() {
        init();
        table.setIndex(0);
    }

    @Test()
    public void shouldCheckIndexIsOkOnColumn() {
        init();
        table.setIndex(1);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldTestDistinctErrorWhenWrongColumnType() {
        init();
        table.setIndex(1);
        TableView view = table.distinct(0); 
    }

}