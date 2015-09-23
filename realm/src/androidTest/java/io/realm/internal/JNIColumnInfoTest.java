package io.realm.internal;

import junit.framework.TestCase;

import io.realm.RealmFieldType;

public class JNIColumnInfoTest extends TestCase {

    Table table;

    @Override
    public void setUp() {
        table = new Table();
        table.addColumn(RealmFieldType.STRING, "firstName");
        table.addColumn(RealmFieldType.STRING, "lastName");
    }

    public void testShouldGetColumnInformation() {

        assertEquals(2, table.getColumnCount());

        assertEquals("lastName", table.getColumnName(1));

        assertEquals(1, table.getColumnIndex("lastName"));

        assertEquals(RealmFieldType.STRING, table.getColumnType(1));

    }

    public void testValidateColumnInfo() {

        TableView view = table.where().findAll();

        assertEquals(2, view.getColumnCount());

        assertEquals("lastName", view.getColumnName(1));

        assertEquals(1, view.getColumnIndex("lastName"));

        assertEquals(RealmFieldType.STRING, view.getColumnType(1));

    }

}
