package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class JNISortedLongTest {
    Table table;
    TableView view;

    void init() {
        table = new Table();
        table.addColumn(ColumnType.INTEGER, "number");
        table.addColumn(ColumnType.STRING, "name");

        table.add(1, "A");
        table.add(10, "B");
        table.add(20, "C");
        table.add(30, "B");
        table.add(40, "D");
        table.add(50, "D");
        table.add(60, "D");
        table.add(60, "D");

        assertEquals(8, table.size());

        view = table.where().findAll();

        assertEquals(view.size(), table.size());

    }

    @Test
    public void shouldTestSortedIntTable() {
        init();

        // before first entry
        assertEquals(0, table.lowerBoundLong(0, 0));
        assertEquals(0, table.upperBoundLong(0, 0));

        // find middle match
        assertEquals(4, table.lowerBoundLong(0, 40));
        assertEquals(5, table.upperBoundLong(0, 40));

        // find mindle (nonexisting)
        assertEquals(5, table.lowerBoundLong(0, 41));
        assertEquals(5, table.upperBoundLong(0, 41));

        // beyond last entry
        assertEquals(8, table.lowerBoundLong(0, 100));
        assertEquals(8, table.upperBoundLong(0, 100));

        // find last match (duplicated)
        assertEquals(6, table.lowerBoundLong(0, 60));
        assertEquals(8, table.upperBoundLong(0, 60));

    }

}
