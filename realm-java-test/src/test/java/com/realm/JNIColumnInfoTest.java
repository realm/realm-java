package com.realm;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JNIColumnInfoTest {

    Table table;

    @BeforeMethod
    void init() {
        table = new Table();
        table.addColumn(ColumnType.STRING, "firstName");
        table.addColumn(ColumnType.STRING, "lastName");
    }

    @Test
    public void shouldGetColumnInformation() {

        assertEquals(2, table.getColumnCount());

        assertEquals("lastName", table.getColumnName(1));

        assertEquals(1, table.getColumnIndex("lastName"));

        assertEquals(ColumnType.STRING, table.getColumnType(1));

    }

    @Test
    public void validateColumnInfo() {

        TableView view = table.where().findAll();

        assertEquals(2, view.getColumnCount());

        assertEquals("lastName", view.getColumnName(1));

        assertEquals(1, view.getColumnIndex("lastName"));

        assertEquals(ColumnType.STRING, view.getColumnType(1));

    }

}
