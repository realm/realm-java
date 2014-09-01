package io.realm;

import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;

public class JNIRowTest {

    @Test
    public void testRow() {

        Table table = new Table();

        table.addColumn(ColumnType.STRING, "string");
        table.addColumn(ColumnType.INTEGER, "integer");
        table.addColumn(ColumnType.FLOAT, "float");
        table.addColumn(ColumnType.DOUBLE, "double");
        table.addColumn(ColumnType.BOOLEAN, "boolean");
        table.addColumn(ColumnType.DATE, "date");
        table.addColumn(ColumnType.BINARY, "binary");


        byte[] data = new byte[2];

        table.add("abc", 3, new Float(1.2), 1.3, true, new Date(0), data);


        Row row = table.getRow(0);

        assertEquals("abc", row.getString(0));
        assertEquals(3, row.getLong(1));
        assertEquals(new Float(1.2), row.getFloat(2));
        assertEquals(1.3, row.getDouble(3));
        assertEquals(true, row.getBoolean(4));
        assertEquals(new Date(0), row.getDate(5));
        assertEquals(data, row.getBinaryByteArray(6));


        row.setString(0, "a");
        row.setLong(1, 1);
        row.setFloat(2, new Float(8.8));
        row.setDouble(3, 9.9);
        row.setBoolean(4, false);
        row.setDate(5, new Date(10000));

        byte[] newData = new byte[3];
        row.setBinaryByteArray(6, newData);

        assertEquals("a", row.getString(0));
        assertEquals(1, row.getLong(1));
        assertEquals(new Float(8.8), row.getFloat(2));
        assertEquals(9.9, row.getDouble(3));
        assertEquals(false, row.getBoolean(4));
        assertEquals(new Date(10000), row.getDate(5));
        assertEquals(newData, row.getBinaryByteArray(6));


    }

    @Test
    public void testMixed() {
        Table table = new Table();

        table.addColumn(ColumnType.MIXED, "mixed");

        table.addEmptyRows(2);

        Row row = table.getRow(0);
        row.setMixed(0, new Mixed(1.5));

        assertEquals(1.5, row.getMixed(0).getDoubleValue());

        Row row2 = table.getRow(1);
        row2.setMixed(0, new Mixed("test"));

        assertEquals("test", row2.getMixed(0).getStringValue());


    }

}
