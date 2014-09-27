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

import junit.framework.Test;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import io.realm.internal.test.ColumnTypeData;

public class JNITableSpecTest extends TestCase {

    TableSpec spec, spec2 = new TableSpec();
    Table table = new Table();
    ColumnTypeData columnTypeData = new ColumnTypeData(null);

    public static Collection<Object[]> parameters() {

        return Arrays.asList(
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.INTEGER)},
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.FLOAT)},
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.DOUBLE)},
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.BOOLEAN)},
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.STRING)},
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.BINARY)},
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.MIXED)},
                new Object[]{new TableSpec(), new TableSpec(), new Table(), new ColumnTypeData(ColumnType.TABLE)}
        );
    }

    public JNITableSpecTest(TableSpec spec, TableSpec spec2,
                            Table table, ColumnTypeData columnTypeData) {
        this.spec = spec;
        this.spec2 = spec2;
        this.table = table;
        this.columnTypeData = columnTypeData;
    }

    public void testShouldDefineOneColumnTable() {
        spec.addColumn(columnTypeData.type, "foo");
        assertEquals(0, spec.getColumnIndex("foo"));
        assertEquals(-1, spec.getColumnIndex("xx"));

        spec2.addColumn(columnTypeData.type, "foo");
        checkSpecIdentity(spec, spec2);

        Table table = new Table();
        table.updateFromSpec(spec);
    }

    public void checkSpecIdentity(TableSpec spec, TableSpec spec2) {
        assertEquals(spec, spec2);
        assertEquals(spec.hashCode(), spec2.hashCode());
    }

    public void testShouldDefineTwoColumnsTable() {
        TableSpec subSpec = spec.addSubtableColumn("bar");
        subSpec.addColumn(columnTypeData.type, "subbar");
        assertEquals(0, spec.getColumnIndex("foo"));
        assertEquals(1, spec.getColumnIndex("bar"));
        assertEquals(0, subSpec.getColumnIndex("subbar"));
        assertEquals(-1, subSpec.getColumnIndex("xx"));

        TableSpec subSpec2 = spec2.addSubtableColumn("bar");
        subSpec2.addColumn(columnTypeData.type, "subbar");

        checkSpecIdentity(spec, spec2);
        Table table = new Table();
        table.updateFromSpec(spec);
    }

    public void testShouldHandleColumnsDynamically() {
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
        table.add(66, 66.6f, -66.6, false, "66", date, new byte[]{6});
        table.addColumn(ColumnType.MIXED, "7");
        table.add(77, 77.7f, -77.7, true, "77", date, new byte[]{7, 7}, "mix");
        table.addColumn(ColumnType.TABLE, "8");
        table.add(88, 88.8f, -88.8, false, "88", date, new byte[]{8, 8, 8}, "mixed", null);

        table.addEmptyRows(10);
        assertEquals(9 + 10, table.size());
        checkColumnsTest(table);
        renameColumnsTest(table);
        removeColumnsTest(table);
    }

    public void checkColumnsTest(Table table) {
        // Check columns
        long columns = 9;
        assertEquals(columns, table.getColumnCount());
        for (long i = 0; i < columns; i++) {
            String name = "" + i;
            assertEquals(name, table.getColumnName(i));
            assertEquals(i, table.getColumnIndex(name));
        }
    }

    public void renameColumnsTest(Table table) {
        // Test renameColumn():
        long columns = 9;
        for (long i = 0; i < columns; i++)
            table.renameColumn(i, "New " + i);
        for (long i = 0; i < columns; i++)
            assertEquals("New " + i, table.getColumnName(i));
    }

    public void removeColumnsTest(Table table) {
        // Test removeColumn():
        long columns = 9;
        table.removeColumn(1);
        assertEquals(columns - 1, table.getColumnCount());
        assertEquals("New 0", table.getColumnName(0));
        for (long i = 1; i < columns - 1; i++)
            assertEquals("New " + (i + 1), table.getColumnName(i));
        // remove first
        table.removeColumn(0);
        assertEquals(columns - 2, table.getColumnCount());
        for (long i = 0; i < columns - 2; i++)
            assertEquals("New " + (i + 2), table.getColumnName(i));
        // remove last
        table.removeColumn(columns - 3);
        assertEquals(columns - 3, table.getColumnCount());
        for (long i = 0; i < columns - 3; i++)
            assertEquals("New " + (i + 2), table.getColumnName(i));
        // remove all but "New 4"
        table.removeColumn(0);
        table.removeColumn(0);
        assertEquals(columns - 5, table.getColumnCount());
        for (long i = 0; i < columns - 6; i++)
            table.removeColumn(1);
        assertEquals(1, table.getColumnCount());
        assertEquals("New 4", table.getColumnName(0));
        assertEquals("44", table.getString(0, 4));
    }

    public void testShouldThrowOnUpdateFromTableSpecOnSubtable() {

        // Table definition
        Table persons = new Table();

        persons.addColumn(ColumnType.STRING, "name");
        persons.addColumn(ColumnType.STRING, "email");
        persons.addColumn(ColumnType.TABLE, "addresses");

        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(ColumnType.STRING, "street");
        addresses.addColumn(ColumnType.INTEGER, "zipcode");
        addresses.addColumn(ColumnType.TABLE, "phone_numbers");

        persons.add(new Object[]{"Mr X", "xx@xxxx.com", new Object[][]{{"X Street", 1234, null}}});

        Table address = persons.getSubtable(2, 0);

        spec.addColumn(ColumnType.INTEGER, "foo");

        try {
            address.updateFromSpec(spec);
            fail("Address is subtable. Not allowed to update from spec");
        } catch (UnsupportedOperationException e) {
        }
    }

    public static Test suite() {
        return new JNITestSuite(JNITableSpecTest.class, parameters());
    }
}

