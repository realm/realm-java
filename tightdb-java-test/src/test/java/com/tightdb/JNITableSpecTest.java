package com.tightdb;

import static org.testng.AssertJUnit.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tightdb.test.DataProviderUtil;

public class JNITableSpecTest {

    @Test(dataProvider = "oneColumn")
    public void shouldDefineOneColumnTable(ColumnType columnType) {
        TableSpec spec = new TableSpec();
        spec.addColumn(columnType, "foo");
        assertEquals(0, spec.getColumnIndex("foo"));
        assertEquals(-1, spec.getColumnIndex("xx"));

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(columnType, "foo");
        checkSpecIdentity(spec, spec2);

        Table table = new Table();
        table.updateFromSpec(spec);
    }

    @Test(dataProvider = "twoColumns")
    public void shouldDefineTwoColumnsTable(ColumnType columnType, ColumnType columnType2) {
        TableSpec spec = new TableSpec();
        spec.addColumn(columnType, "foo");
        TableSpec subspec = spec.addSubtableColumn("bar");
        subspec.addColumn(columnType2, "subbar");
        assertEquals(0, spec.getColumnIndex("foo"));
        assertEquals(1, spec.getColumnIndex("bar"));
        assertEquals(0, subspec.getColumnIndex("subbar"));
        assertEquals(-1, subspec.getColumnIndex("xx"));

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(columnType, "foo");
        TableSpec subspec2 = spec2.addSubtableColumn("bar");
        subspec2.addColumn(columnType2, "subbar");

        checkSpecIdentity(spec, spec2);

        Table table = new Table();
        table.updateFromSpec(spec);
    }

    @DataProvider(name = "oneColumn")
    public Iterator<Object[]> oneColumn() {
        return DataProviderUtil.allCombinations(columnTypes());
    }

    @DataProvider(name = "twoColumns")
    public Iterator<Object[]> twoColumns() {
        return DataProviderUtil.allCombinations(columnTypes(), columnTypes());
    }

    private void checkSpecIdentity(TableSpec spec, TableSpec spec2) {
        assertEquals(spec, spec2);
        assertEquals(spec.hashCode(), spec2.hashCode());
    }

    private List<?> columnTypes() {
        return Arrays.asList(ColumnType.values());
    }

    @Test()
    public void shouldHandleColumnsDynamically() {
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "0");
        assertEquals(1, table.getColumnCount());
        assertEquals(0, table.getColumnIndex("0"));
        assertEquals("0", table.getColumnName(0));
        assertEquals(ColumnType.INTEGER, table.getColumnType(0));
        table.add(23);

        table.addColumn(ColumnType.FLOAT, "1");
        table.add(11, 11.1f);
        table.addColumn(ColumnType.DOUBLE, "2");
        table.add(22, 22.2f, -22.2);
        table.addColumn(ColumnType.BOOLEAN, "3");
        table.add(33, 33.3f, -33.3, true);
        table.addColumn(ColumnType.STRING, "4");
        table.add(44, 44.4f, -44.4, true, "44");
        table.addColumn(ColumnType.DATE, "5");
        Date date = new Date();
        table.add(55, 55.5f, -55.5, false, "55", date);
        table.addColumn(ColumnType.BINARY, "6");
        table.add(66, 66.6f, -66.6, false, "66", date, new byte[] {6});
        table.addColumn(ColumnType.MIXED, "7");
        table.add(77, 77.7f, -77.7, true, "77", date, new byte[] {7, 7}, "mix");
        table.addColumn(ColumnType.TABLE, "8");
        table.add(88, 88.8f, -88.8, false, "88", date, new byte[] {8, 8, 8}, "mixed", null);

        table.addEmptyRows(10);
        assertEquals(9+10, table.size());

        // Check columns
        long columns = 9;
        assertEquals(columns, table.getColumnCount());
        for (long i=0; i<columns; i++) {
            String name = "" + i;
            assertEquals(name, table.getColumnName(i));
            assertEquals(i, table.getColumnIndex(name));
        }

        // Test renameColumn():

        for (long i=0; i<columns; i++)
            table.renameColumn(i, "New " + i);
        for (long i=0; i<columns; i++)
            assertEquals("New " + i, table.getColumnName(i));

        // Test removeColumn():

        table.removeColumn(1);
        assertEquals(columns-1, table.getColumnCount());
        assertEquals("New 0", table.getColumnName(0));
        for (long i=1; i<columns-1; i++)
            assertEquals("New " + (i + 1), table.getColumnName(i));
        // remove first
        table.removeColumn(0);
        assertEquals(columns-2, table.getColumnCount());
        for (long i=0; i<columns-2; i++)
            assertEquals("New " + (i + 2), table.getColumnName(i));
        // remove last
        table.removeColumn(columns-3);
        assertEquals(columns-3, table.getColumnCount());
        for (long i=0; i<columns-3; i++)
            assertEquals("New " + (i + 2), table.getColumnName(i));
        // remove all but "New 4"
        table.removeColumn(0);
        table.removeColumn(0);
        assertEquals(columns-5, table.getColumnCount());
        for (long i=0; i<columns-6; i++)
            table.removeColumn(1);
        assertEquals(1, table.getColumnCount());
        assertEquals("New 4", table.getColumnName(0));
        assertEquals("44", table.getString(0,4));
    }
}
