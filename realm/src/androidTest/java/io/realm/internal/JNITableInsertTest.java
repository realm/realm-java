/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.internal;

import android.test.MoreAsserts;

import junit.framework.Test;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.realm.internal.test.TestHelper;

public class JNITableInsertTest extends TestCase {

    static List<Object> value = new ArrayList<Object>();
    Object tv = new Object();
    Object ct = new Object();

    public static Collection<Object[]> parameters() {
        value.add(0, true);
        value.add(1, "abc");
        value.add(2, 123L);
        value.add(3, 987.123f);
        value.add(4, 1234567.898d);
        value.add(5, new Date(645342));
        value.add(6, new byte[] {1, 2, 3, 4, 5});
        return Arrays.asList(
                new Object[] {value},
                new Object[] {value}
        );
    }

    public JNITableInsertTest(ArrayList value) {
        this.value = value;
    }

    public void verifyRow(Table tbl, long rowIndex, Object[] values) {
        assertTrue((Boolean) (values[0]) == tbl.getBoolean(0, rowIndex));
        assertEquals(((Number) values[1]).longValue(), tbl.getLong(1, rowIndex));
        assertEquals((String) values[2], tbl.getString(2, rowIndex));
        if (values[3] instanceof byte[])
            MoreAsserts.assertEquals((byte[]) values[3], tbl.getBinaryByteArray(3, rowIndex));
        assertEquals(((Date) values[4]).getTime() / 1000, tbl.getDate(4, rowIndex).getTime() / 1000);

        //      Mixed mix1 = Mixed.mixedValue(values[5]);
        //      Mixed mix2 =  tbl.getMixed(5, rowIndex);
        // TODO:        assertTrue(mix1.equals(mix2));

        Table subtable = tbl.getSubtable(6, rowIndex);
        Object[] subValues = (Object[]) values[6];
        for (long i = 0; i < subtable.size(); i++) {
            Object[] val = (Object[]) subValues[(int) i];
            assertTrue(((Number) val[0]).longValue() == subtable.getLong(0, i));
            assertEquals(((String) val[1]), subtable.getString(1, i));
        }
        assertTrue(tbl.isValid());
    }

    public void testShouldInsertAddAndSetRows() {
        Table table = new Table();
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.BOOLEAN, "bool");
        tableSpec.addColumn(ColumnType.INTEGER, "number");
        tableSpec.addColumn(ColumnType.STRING, "string");
        tableSpec.addColumn(ColumnType.BINARY, "Bin");
        tableSpec.addColumn(ColumnType.DATE, "date");
        tableSpec.addColumn(ColumnType.MIXED, "mix");
        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(ColumnType.INTEGER, "sub-num");
        subspec.addColumn(ColumnType.STRING, "sub-str");
        table.updateFromSpec(tableSpec);

        byte[] buf = new byte[23];
        Mixed mixedSubtable = new Mixed(ColumnType.TABLE);
        Date date = new Date();
        long mixed = 123;

        // Check subtable
        Object[][] subTblData = new Object[][] {{234, "row0"},
                {345, "row1"},
                {456, "row2"}};
        Object[] rowData0 = new Object[] {false, (short) 2, "hi", buf, date, mixed, subTblData};
        long index = table.add(rowData0);
        assertEquals(0, index);
        verifyRow(table, 0, rowData0);

        Object[] rowData1 = new Object[] {false, 7, "hi1", new byte[] {0, 2, 3}, date, "mix1", null};
        Object[] rowData2 = new Object[] {true, 12345567789L, "hello", new byte[] {0}, date, buf, null};
        Object[] rowData3 = new Object[] {false, (byte) 17, "hi3", buf, date, mixedSubtable, null};
        // TODO: support insert of mixed subtable

        table.addAt(1, rowData1);
        index = table.add(rowData2);
        assertEquals(2, index);
        table.addAt(0, rowData3);

        verifyRow(table, 0, rowData3);
        verifyRow(table, 1, rowData0);
        verifyRow(table, 2, rowData1);
        verifyRow(table, 3, rowData2);

        // Same test - but a one-liner...
        table.add(false, (short) 2, "hi", buf, date, mixed, new Object[][] {{234, "row0"},
                {345, "row1"},
                {456, "row2"}});
        verifyRow(table, 4, rowData0);

        // Test set()
        Date date2 = new Date(123);
        Object[] newRowData = new Object[] {true, 321, "new", new byte[] {5},
                date2, "hey", new Object[][] {{432, "new"}}};
        table.set(2, newRowData);
        verifyRow(table, 0, rowData3);
        verifyRow(table, 1, rowData0);
        verifyRow(table, 2, newRowData);
        verifyRow(table, 3, rowData2);

    }

    public void testAddAtMethod() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "col1");
        t.addColumn(ColumnType.INTEGER, "col2");

        t.add("s1", 1);
        t.add("s2", 2);

        t.addAt(1, "s22", 22);

        assertEquals(t.getString(0, 1), "s22");
    }

    public void testShouldFailInsert() {
        Table table = new Table();
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.BOOLEAN, "bool");
        tableSpec.addColumn(ColumnType.INTEGER, "number");
        tableSpec.addColumn(ColumnType.STRING, "string");
        tableSpec.addColumn(ColumnType.BINARY, "Bin");
        tableSpec.addColumn(ColumnType.DATE, "date");
        tableSpec.addColumn(ColumnType.MIXED, "mix");
        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(ColumnType.INTEGER, "sub-num");
        table.updateFromSpec(tableSpec);

        // Wrong number of parameters
        byte[] buf = new byte[23];
        try {
            table.addAt(0, false);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // wrong row index
        long mix = 123;
        try {
            table.addAt(1, false, 1, "hi", buf, new Date(), mix, null);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // wrong row index
        table.addAt(0, false, 1, "hi", buf, new Date(), 123, null);
        table.addAt(1, false, 1, "hi", buf, new Date(), 123, null);
        try {
            table.addAt(3, false, 1, "hi", buf, new Date(), mix, null);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (999 instead of bool)
        try {
            table.addAt(0, 999, 1, "hi", buf, new Date(), mix, null);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (bool instead of 1)
        try {
            table.addAt(0, true, false, "hi", buf, new Date(), mix, null);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (999 instead of string)
        try {
            table.addAt(0, false, 1, 999, buf, new Date(), mix, null);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (999 instead of Binary)
        try {
            table.addAt(0, false, 1, "hi", 999, new Date(), mix, null);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (999 instead of Date)
        try {
            table.addAt(0, false, 1, "hi", buf, 999, mix, null);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (999 instead of subtable)
        try {
            table.addAt(0, false, 1, "hi", buf, new Date(), mix, 999);
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (String instead of subtable-Int)
        try {
            table.addAt(0, false, 1, "hi", buf, new Date(), mix, new Object[][] {{"err", 2, 3}});
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }

        // Wrong type of parameter (String instead of subtable-Int)
        try {
            table.addAt(0, false, 1, "hi", buf, new Date(), mix, new Object[] {1, 2, 3});
            fail("expected exception.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIncrementInColumnTest() {

        Table table = new Table();
        table.addColumn(ColumnType.STRING, "col0");
        table.addColumn(ColumnType.INTEGER, "col1");

        table.add("row0", 0);
        table.add("row1", 10);
        table.add("row2", 20);
        table.add("row3", 30);
        table.add("row4", 40);

        table.adjust(1, 3); //Adding 3 to all rows in col1

        assertEquals(3, table.getLong(1, 0));
        assertEquals(13, table.getLong(1, 1));
        assertEquals(23, table.getLong(1, 2));
        assertEquals(33, table.getLong(1, 3));
        assertEquals(43, table.getLong(1, 4));
    }

    public void testAdjustColumnValuesOnUnsupportedColumnTypeTest() {

        Table table = TestHelper.getTableWithAllColumnTypes();

        for (long c = 0; c < table.getColumnCount(); c++) {

            if (!table.getColumnType(c).equals(ColumnType.INTEGER)) { // Do not check if it is a Long column
                try {
                    table.adjust(c, 10);
                    assertTrue(false); //We should never get here, as an exception is thrown above
                } catch (IllegalArgumentException e) {
                    assertTrue(true); // All other column types than long will throw exception
                }
            }
        }
    }

    public void testShouldThrowExceptionWhenColumnNameIsTooLong() {

        Table table = new Table();
        try {
            table.addColumn(ColumnType.STRING, "THIS STRING HAS 64 CHARACTERS, "
                    + "LONGER THAN THE MAX 63 CHARACTERS");
            fail("Too long name");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testWhenColumnNameIsExcactly63CharLong() {

        Table table = new Table();
        table.addColumn(ColumnType.STRING, "THIS STRING HAS 63 CHARACTERS PERFECT FOR THE MAX 63 CHARACTERS");
    }

    public void testGenericAddOnTable() {
        for (int i = 0; i < value.size(); i++) {
            for (int j = 0; j < value.size(); j++) {

                Table t = new Table();

                //If the objects matches no exception will be thrown
                if (value.get(i).getClass().equals(value.get(j).getClass())) {
                    assertTrue(true);

                } else {
                    //Add column
                    t.addColumn(TestHelper.getColumnType(value.get(j)), value.get(j).getClass().getSimpleName());
                    //Add value
                    try {
                        t.add(value.get(i));
                        fail("No matching type");
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
    }

    public static Test suite() {
        return new JNITestSuite(JNITableInsertTest.class, parameters());

    }
}

