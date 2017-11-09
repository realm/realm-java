/*
 * Copyright 2015 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class JNITableTest {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @SuppressWarnings("FieldCanBeLocal")
    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (sharedRealm != null)  {
            sharedRealm.close();
        }
    }

    @Test
    public void tableToString() {
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                t.addColumn(RealmFieldType.STRING, "stringCol");
                t.addColumn(RealmFieldType.INTEGER, "intCol");
                t.addColumn(RealmFieldType.BOOLEAN, "boolCol");

                TestHelper.addRowWithValues(t, "s1", 1, true);
                TestHelper.addRowWithValues(t, "s2", 2, false);
            }
        });

        String expected = "The Table temp contains 3 columns: stringCol, intCol, boolCol. And 2 rows.";
        assertEquals(expected, t.toString());
    }

    @Test
    public void rowOperationsOnZeroRow() {
        Table t = TestHelper.createTable(sharedRealm, "temp");

        sharedRealm.beginTransaction();
        // Removes rows without columns.
        try { t.moveLastOver(0);  fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        try { t.moveLastOver(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}

        // Column added, remove rows again.
        t.addColumn(RealmFieldType.STRING, "");
        try { t.moveLastOver(0);  fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        try { t.moveLastOver(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException ignored) {}
        sharedRealm.commitTransaction();
    }

    @Test
    public void zeroColOperations() {
        Table tableZeroCols = TestHelper.createTable(sharedRealm, "temp");

        sharedRealm.beginTransaction();
        // Col operations
        try {
            tableZeroCols.removeColumn(0);
            fail("No columns in table");
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        try {
            tableZeroCols.renameColumn(0, "newName");
            fail("No columns in table");
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        try {
            tableZeroCols.removeColumn(10);
            fail("No columns in table");
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        try {
            tableZeroCols.renameColumn(10, "newName");
            fail("No columns in table");
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        sharedRealm.commitTransaction();
    }

    @Test
    public void findFirstNonExisting() {
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        sharedRealm.beginTransaction();
        TestHelper.addRowWithValues(t, new byte[] {1, 2, 3}, true, new Date(1384423149761L), 4.5D, 5.7F, 100, "string");
        sharedRealm.commitTransaction();

        assertEquals(-1, t.findFirstBoolean(1, false));
        assertEquals(-1, t.findFirstDate(2, new Date(138442314986L)));
        assertEquals(-1, t.findFirstDouble(3, 1.0D));
        assertEquals(-1, t.findFirstFloat(4, 1.0F));
        assertEquals(-1, t.findFirstLong(5, 50));
    }

    @Test
    public void findFirst() {
        final int TEST_SIZE = 10;
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        sharedRealm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            TestHelper.addRowWithValues(t, new byte[] {1, 2, 3}, true, new Date(i), (double) i, (float) i, i, "string " + i);
        }
        TestHelper.addRowWithValues(t, new byte[] {1, 2, 3}, true, new Date(TEST_SIZE), (double) TEST_SIZE, (float) TEST_SIZE, TEST_SIZE, "");
        sharedRealm.commitTransaction();

        assertEquals(0, t.findFirstBoolean(1, true));
        for (int i = 0; i < TEST_SIZE; i++) {
            assertEquals(i, t.findFirstDate(2, new Date(i)));
            assertEquals(i, t.findFirstDouble(3, (double) i));
            assertEquals(i, t.findFirstFloat(4, (float) i));
            assertEquals(i, t.findFirstLong(5, i));
        }

        try {
            t.findFirstString(6, null);
            fail();
        } catch (IllegalArgumentException ignored) {}

        try {
            t.findFirstDate(2, null);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void getValuesFromNonExistingColumn() {
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        sharedRealm.beginTransaction();
        for (int i = 0; i < 10; i++) {
            OsObject.createRow(t);
        }
        sharedRealm.commitTransaction();

        try {
            t.getBinaryByteArray(-1, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getBinaryByteArray(-10, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getBinaryByteArray(9, 0);
            fail("Column does not exist");
        } catch (ArrayIndexOutOfBoundsException ignored) { }

        try {
            t.getBoolean(-1, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getBoolean(-10, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getBoolean(9, 0);
            fail("Column does not exist");
        } catch (ArrayIndexOutOfBoundsException ignored) { }

        try {
            t.getDate(-1, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getDate(-10, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getDate(9, 0);
            fail("Column does not exist");
        } catch (ArrayIndexOutOfBoundsException ignored) { }

        try {
            t.getDouble(-1, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getDouble(-10, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getDouble(9, 0);
            fail("Column does not exist");
        } catch (ArrayIndexOutOfBoundsException ignored) { }

        try {
            t.getFloat(-1, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getFloat(-10, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getFloat(9, 0);
            fail("Column does not exist");
        } catch (ArrayIndexOutOfBoundsException ignored) { }

        try {
            t.getLong(-1, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getLong(-10, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getLong(9, 0);
            fail("Column does not exist");
        } catch (ArrayIndexOutOfBoundsException ignored) { }

        try {
            t.getString(-1, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getString(-10, 0);
            fail("Column is less than 0");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
        try {
            t.getString(9, 0);
            fail("Column does not exist");
        } catch (ArrayIndexOutOfBoundsException ignored) { }
    }

    @Test
    public void getNonExistingColumn() {
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                t.addColumn(RealmFieldType.INTEGER, "int");
            }
        });

        assertEquals(-1, t.getColumnIndex("non-existing column"));
        try {
            t.getColumnIndex(null);
            fail("column name null");
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void setNulls() {
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                t.addColumn(RealmFieldType.STRING, "");
                t.addColumn(RealmFieldType.DATE, "");
                t.addColumn(RealmFieldType.BINARY, "");
                TestHelper.addRowWithValues(t, "String val", new Date(), new byte[] {1, 2, 3});
            }
        });

        sharedRealm.beginTransaction();
        try {
            t.setString(0, 0, null, false);
            fail("null string not allowed");
        } catch (IllegalArgumentException ignored) { }
        try {
            t.setDate(1, 0, null, false);
            fail("null Date not allowed");
        } catch (IllegalArgumentException ignored) { }
        sharedRealm.commitTransaction();
    }

    @Test
    public void getName() {
        String TABLE_NAME = "tableName";
        //noinspection TryFinallyCanBeTryWithResources
        try {

            // Writes transaction must be run so we are sure a db exists with the correct table.
            sharedRealm.beginTransaction();
            sharedRealm.createTable(TABLE_NAME);
            sharedRealm.commitTransaction();

            Table table = sharedRealm.getTable(TABLE_NAME);
            assertEquals(TABLE_NAME, table.getName());
        } finally {
            sharedRealm.close();
        }
    }

    @Test
    public void shouldThrowWhenSetIndexOnWrongRealmFieldType() {
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        for (long colIndex = 0; colIndex < t.getColumnCount(); colIndex++) {

            // All types supported addSearchIndex and removeSearchIndex.
            boolean exceptionExpected = (
                    t.getColumnType(colIndex) != RealmFieldType.STRING &&
                            t.getColumnType(colIndex) != RealmFieldType.INTEGER &&
                            t.getColumnType(colIndex) != RealmFieldType.BOOLEAN &&
                            t.getColumnType(colIndex) != RealmFieldType.DATE);

            // Tries to addSearchIndex().
            sharedRealm.beginTransaction();
            try {
                t.addSearchIndex(colIndex);
                if (exceptionExpected) {
                    fail("Expected exception for colIndex " + colIndex);
                }
            } catch (IllegalArgumentException ignored) {
            }
            sharedRealm.commitTransaction();

            // Tries to removeSearchIndex().
            sharedRealm.beginTransaction();
            try {
                // Currently core will do nothing if the column doesn't have a search index.
                t.removeSearchIndex(colIndex);
                if (exceptionExpected) {
                    fail("Expected exception for colIndex " + colIndex);
                }
            } catch (IllegalArgumentException ignored) {
            }
            sharedRealm.commitTransaction();

            // Tries to hasSearchIndex() for all columnTypes.
            t.hasSearchIndex(colIndex);
        }
    }

    @Test
    public void columnName() {
        TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                try {
                    t.addColumn(RealmFieldType.STRING, "I am 64 characters..............................................");
                    fail("Only 63 characters supported");
                } catch (IllegalArgumentException ignored) { }
                t.addColumn(RealmFieldType.STRING, "I am 63 characters.............................................");
            }
        });
    }

    @Test
    public void tableNumbers() {
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                t.addColumn(RealmFieldType.INTEGER, "intCol");
                t.addColumn(RealmFieldType.DOUBLE, "doubleCol");
                t.addColumn(RealmFieldType.FLOAT, "floatCol");
                t.addColumn(RealmFieldType.STRING, "StringCol");

                // Adds 3 rows of data with same values in each column.
                TestHelper.addRowWithValues(t, 1, 2.0D, 3.0F, "s1");
                TestHelper.addRowWithValues(t, 1, 2.0D, 3.0F, "s1");
                TestHelper.addRowWithValues(t, 1, 2.0D, 3.0F, "s1");

                // Adds other values.
                TestHelper.addRowWithValues(t, 10, 20.0D, 30.0F, "s10");
                TestHelper.addRowWithValues(t, 100, 200.0D, 300.0F, "s100");
                TestHelper.addRowWithValues(t, 1000, 2000.0D, 3000.0F, "s1000");
            }
        });

        // Counts instances of values added in the first 3 rows.
        assertEquals(3, t.count(0, 1));
        assertEquals(3, t.count(1, 2.0D));
        assertEquals(3, t.count(2, 3.0F));
        assertEquals(3, t.count(3, "s1"));


        assertEquals(3, t.findFirstDouble(1, 20.0D)); // Find rows index for first double value of 20.0 in column 1.
        assertEquals(4, t.findFirstFloat(2, 300.0F)); // Find rows index for first float value of 300.0 in column 2.

        // Sets double and float.
        sharedRealm.beginTransaction();
        t.setDouble(1, 2, -2.0D, false);
        t.setFloat(2, 2, -3.0F, false);
        sharedRealm.commitTransaction();

        // Gets double tests.
        assertEquals(-2.0D, t.getDouble(1, 2));
        assertEquals(20.0D, t.getDouble(1, 3));
        assertEquals(200.0D, t.getDouble(1, 4));
        assertEquals(2000.0D, t.getDouble(1, 5));

        // Gets float test.
        assertEquals(-3.0F, t.getFloat(2, 2));
        assertEquals(30.0F, t.getFloat(2, 3));
        assertEquals(300.0F, t.getFloat(2, 4));
        assertEquals(3000.0F, t.getFloat(2, 5));
    }

    // Tests the migration of a string column to be nullable.
    @Test
    public void convertToNullable() {
        RealmFieldType[] columnTypes = {RealmFieldType.BOOLEAN, RealmFieldType.DATE, RealmFieldType.DOUBLE,
                RealmFieldType.FLOAT, RealmFieldType.INTEGER, RealmFieldType.BINARY, RealmFieldType.STRING};
        int tableIndex = 0;
        for (final RealmFieldType columnType : columnTypes) {
            // Tests various combinations of column names and nullability.
            String[] columnNames = {"foobar", "__TMP__0"};
            for (final boolean nullable : new boolean[] {Table.NOT_NULLABLE, Table.NULLABLE}) {
                for (final String columnName : columnNames) {
                    final AtomicLong colIndexRef = new AtomicLong();
                    Table table = TestHelper.createTable(sharedRealm, "temp" + tableIndex, new TestHelper.AdditionalTableSetup() {
                        @Override
                        public void execute(Table table) {
                            long colIndex = table.addColumn(columnType, columnName, nullable);
                            colIndexRef.set(colIndex);
                            table.addColumn(RealmFieldType.BOOLEAN, "bool");
                            OsObject.createRow(table);
                            if (columnType == RealmFieldType.BOOLEAN) {
                                table.setBoolean(colIndex, 0, true, false);
                            } else if (columnType == RealmFieldType.DATE) {
                                table.setDate(colIndex, 0, new Date(0), false);
                            } else if (columnType == RealmFieldType.DOUBLE) {
                                table.setDouble(colIndex, 0, 1.0, false);
                            } else if (columnType == RealmFieldType.FLOAT) {
                                table.setFloat(colIndex, 0, 1.0F, false);
                            } else if (columnType == RealmFieldType.INTEGER) {
                                table.setLong(colIndex, 0, 1, false);
                            } else if (columnType == RealmFieldType.BINARY) {
                                table.setBinaryByteArray(colIndex, 0, new byte[] {0}, false);
                            } else if (columnType == RealmFieldType.STRING) {
                                table.setString(colIndex, 0, "Foo", false);
                            }
                            try {
                                OsObject.createRow(table);
                                if (columnType == RealmFieldType.BINARY) {
                                    table.setBinaryByteArray(colIndex, 1, null, false);
                                } else if (columnType == RealmFieldType.STRING) {
                                    table.setString(colIndex, 1, null, false);
                                } else {
                                    table.getCheckedRow(1).setNull(colIndex);
                                }

                                if (!nullable) {
                                    fail();
                                }
                            } catch (IllegalArgumentException ignored) {
                            }
                            table.moveLastOver(table.size() - 1);
                        }
                    });
                    assertEquals(1, table.size());

                    long colIndex = colIndexRef.get();

                    sharedRealm.beginTransaction();
                    table.convertColumnToNullable(colIndex);
                    sharedRealm.commitTransaction();
                    assertTrue(table.isColumnNullable(colIndex));
                    assertEquals(1, table.size());
                    assertEquals(2, table.getColumnCount());
                    assertTrue(table.getColumnIndex(columnName) >= 0);
                    assertEquals(colIndex, table.getColumnIndex(columnName));

                    sharedRealm.beginTransaction();
                    OsObject.createRow(table);
                    if (columnType == RealmFieldType.BINARY) {
                        table.setBinaryByteArray(colIndex, 0, null, false);
                    } else if (columnType == RealmFieldType.STRING) {
                        table.setString(colIndex, 0, null, false);
                    } else {
                        table.getCheckedRow(0).setNull(colIndex);
                    }
                    sharedRealm.commitTransaction();

                    assertEquals(2, table.size());

                    if (columnType == RealmFieldType.BINARY) {
                        assertNull(table.getBinaryByteArray(colIndex, 1));
                    } else if (columnType == RealmFieldType.STRING) {
                        assertNull(table.getString(colIndex, 1));
                    } else {
                        assertTrue(table.getUncheckedRow(1).isNull(colIndex));
                    }
                    tableIndex++;
                }
            }
        }
    }

    @Test
    public void convertToNotNullable() {
        RealmFieldType[] columnTypes = {RealmFieldType.BOOLEAN, RealmFieldType.DATE, RealmFieldType.DOUBLE,
                RealmFieldType.FLOAT, RealmFieldType.INTEGER, RealmFieldType.BINARY, RealmFieldType.STRING};
        int tableIndex = 0;
        for (final RealmFieldType columnType : columnTypes) {
            // Tests various combinations of column names and nullability.
            String[] columnNames = {"foobar", "__TMP__0"};
            for (final boolean nullable : new boolean[] {Table.NOT_NULLABLE, Table.NULLABLE}) {
                for (final String columnName : columnNames) {
                    final AtomicLong colIndexRef = new AtomicLong();
                    Table table = TestHelper.createTable(sharedRealm, "temp" + tableIndex, new TestHelper.AdditionalTableSetup() {
                        @Override
                        public void execute(Table table) {
                            long colIndex = table.addColumn(columnType, columnName, nullable);
                            colIndexRef.set(colIndex);
                            table.addColumn(RealmFieldType.BOOLEAN, "bool");
                            OsObject.createRow(table);
                            if (columnType == RealmFieldType.BOOLEAN) {
                                table.setBoolean(colIndex, 0, true, false);
                            } else if (columnType == RealmFieldType.DATE) {
                                table.setDate(colIndex, 0, new Date(1), false);
                            } else if (columnType == RealmFieldType.DOUBLE) {
                                table.setDouble(colIndex, 0, 1.0, false);
                            } else if (columnType == RealmFieldType.FLOAT) {
                                table.setFloat(colIndex, 0, 1.0F, false);
                            } else if (columnType == RealmFieldType.INTEGER) {
                                table.setLong(colIndex, 0, 1, false);
                            } else if (columnType == RealmFieldType.BINARY) {
                                table.setBinaryByteArray(colIndex, 0, new byte[] {0}, false);
                            } else if (columnType == RealmFieldType.STRING) { table.setString(colIndex, 0, "Foo", false); }
                            try {
                                OsObject.createRow(table);
                                if (columnType == RealmFieldType.BINARY) {
                                    table.setBinaryByteArray(colIndex, 1, null, false);
                                } else if (columnType == RealmFieldType.STRING) {
                                    table.setString(colIndex, 1, null, false);
                                } else {
                                    table.getCheckedRow(1).setNull(colIndex);
                                }

                                if (!nullable) {
                                    fail();
                                }
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    });
                    assertEquals(2, table.size());

                    long colIndex = colIndexRef.get();

                    sharedRealm.beginTransaction();
                    table.convertColumnToNotNullable(colIndex);
                    sharedRealm.commitTransaction();
                    assertFalse(table.isColumnNullable(colIndex));
                    assertEquals(2, table.size());
                    assertEquals(2, table.getColumnCount());
                    assertTrue(table.getColumnIndex(columnName) >= 0);
                    assertEquals(colIndex, table.getColumnIndex(columnName));

                    sharedRealm.beginTransaction();
                    OsObject.createRow(table);
                    try {
                        if (columnType == RealmFieldType.BINARY) {
                            table.setBinaryByteArray(colIndex, 0, null, false);
                        } else if (columnType == RealmFieldType.STRING) {
                            table.setString(colIndex, 0, null, false);
                        } else {
                            table.getCheckedRow(0).setNull(colIndex);
                        }
                        if (!nullable) {
                            fail();
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                    table.moveLastOver(table.size() -1);
                    sharedRealm.commitTransaction();

                    assertEquals(2, table.size());

                    if (columnType == RealmFieldType.BINARY) {
                        assertNotNull(table.getBinaryByteArray(colIndex, 1));
                    } else if (columnType == RealmFieldType.STRING) {
                        assertNotNull(table.getString(colIndex, 1));
                        assertEquals("", table.getString(colIndex, 1));
                    } else {
                        assertFalse(table.getUncheckedRow(1).isNull(colIndex));
                        if (columnType == RealmFieldType.BOOLEAN) {
                            assertEquals(false, table.getBoolean(colIndex, 1));
                        } else if (columnType == RealmFieldType.DATE) {
                            assertEquals(0, table.getDate(colIndex, 1).getTime());
                        } else if (columnType == RealmFieldType.DOUBLE) {
                            assertEquals(0.0, table.getDouble(colIndex, 1));
                        } else if (columnType == RealmFieldType.FLOAT) {
                            assertEquals(0.0F, table.getFloat(colIndex, 1));
                        } else if (columnType == RealmFieldType.INTEGER) {
                            assertEquals(0, table.getLong(colIndex, 1));
                        }
                    }
                    tableIndex++;
                }
            }
        }
    }

    // Adds column and read back if it is nullable or not.
    @Test
    public void isNullable() {
        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                table.addColumn(RealmFieldType.STRING, "string1", Table.NOT_NULLABLE);
                table.addColumn(RealmFieldType.STRING, "string2", Table.NULLABLE);
            }
        });

        assertFalse(table.isColumnNullable(0));
        assertTrue(table.isColumnNullable(1));
    }

    @Test
    public void defaultValue_setAndGet() {
        final OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configFactory.createConfiguration());
        //noinspection TryFinallyCanBeTryWithResources
        try {
            sharedRealm.beginTransaction();
            final Table table = sharedRealm.createTable(Table.getTableNameForClass("DefaultValueTest"));
            sharedRealm.commitTransaction();

            List<Pair<RealmFieldType, Object>> columnInfoList = Arrays.asList(
                    new Pair<RealmFieldType, Object>(RealmFieldType.STRING, "string value"),
                    new Pair<RealmFieldType, Object>(RealmFieldType.INTEGER, 100L),
                    new Pair<RealmFieldType, Object>(RealmFieldType.BOOLEAN, true),
                    new Pair<RealmFieldType, Object>(RealmFieldType.BINARY, new byte[] {123}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.DATE, new Date(123456)),
                    new Pair<RealmFieldType, Object>(RealmFieldType.FLOAT, 1.234F),
                    new Pair<RealmFieldType, Object>(RealmFieldType.DOUBLE, Math.PI),
                    new Pair<RealmFieldType, Object>(RealmFieldType.OBJECT, 0L)
                    // FIXME: Currently, LIST does not support default value.
                    // new CollectionChange<RealmFieldType, Object>(RealmFieldType.LIST, )
            );

            for (Pair<RealmFieldType, Object> columnInfo : columnInfoList) {
                final RealmFieldType type = columnInfo.first;
                if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
                    table.addColumnLink(type, type.name().toLowerCase(Locale.ENGLISH) + "Col", table);
                } else {
                    table.addColumn(type, type.name().toLowerCase(Locale.ENGLISH) + "Col");
                }
            }

            sharedRealm.beginTransaction();
            OsObject.createRow(table);

            ListIterator<Pair<RealmFieldType, Object>> it = columnInfoList.listIterator();
            for (int columnIndex = 0; columnIndex < columnInfoList.size(); columnIndex++) {
                Pair<RealmFieldType, Object> columnInfo = it.next();
                final RealmFieldType type = columnInfo.first;
                final Object value = columnInfo.second;

                switch (type) {
                    case STRING:
                        table.setString(columnIndex, 0, (String) value, true);
                        assertEquals(value, table.getString(columnIndex, 0));
                        break;
                    case INTEGER:
                        table.setLong(columnIndex, 0, (long) value, true);
                        assertEquals(value, table.getLong(columnIndex, 0));
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnIndex, 0, (boolean) value, true);
                        assertEquals(value, table.getBoolean(columnIndex, 0));
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnIndex, 0, (byte[]) value, true);
                        assertTrue(Arrays.equals((byte[]) value, table.getBinaryByteArray(columnIndex, 0)));
                        break;
                    case DATE:
                        table.setDate(columnIndex, 0, (Date) value, true);
                        assertEquals(value, table.getDate(columnIndex, 0));
                        break;
                    case FLOAT:
                        table.setFloat(columnIndex, 0, (float) value, true);
                        assertEquals(value, table.getFloat(columnIndex, 0));
                        break;
                    case DOUBLE:
                        table.setDouble(columnIndex, 0, (double) value, true);
                        assertEquals(value, table.getDouble(columnIndex, 0));
                        break;
                    case OBJECT:
                        table.setLink(columnIndex, 0, (long) value, true);
                        assertEquals(value, table.getLink(columnIndex, 0));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Checks if the value can be read after committing transaction.
            it = columnInfoList.listIterator();
            for (int columnIndex = 0; columnIndex < columnInfoList.size(); columnIndex++) {
                Pair<RealmFieldType, Object> columnInfo = it.next();
                final RealmFieldType type = columnInfo.first;
                final Object value = columnInfo.second;

                switch (type) {
                    case STRING:
                        assertEquals(value, table.getString(columnIndex, 0));
                        break;
                    case INTEGER:
                        assertEquals(value, table.getLong(columnIndex, 0));
                        break;
                    case BOOLEAN:
                        assertEquals(value, table.getBoolean(columnIndex, 0));
                        break;
                    case BINARY:
                        assertTrue(Arrays.equals((byte[]) value, table.getBinaryByteArray(columnIndex, 0)));
                        break;
                    case DATE:
                        assertEquals(value, table.getDate(columnIndex, 0));
                        break;
                    case FLOAT:
                        assertEquals(value, table.getFloat(columnIndex, 0));
                        break;
                    case DOUBLE:
                        assertEquals(value, table.getDouble(columnIndex, 0));
                        break;
                    case OBJECT:
                        assertEquals(value, table.getLink(columnIndex, 0));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }

        } finally {
            sharedRealm.close();
        }
    }

    @Test
    public void defaultValue_setMultipleTimes() {
        final OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configFactory.createConfiguration());
        //noinspection TryFinallyCanBeTryWithResources
        try {
            sharedRealm.beginTransaction();
            final Table table = sharedRealm.createTable(Table.getTableNameForClass("DefaultValueTest"));
            sharedRealm.commitTransaction();

            List<Pair<RealmFieldType, Object>> columnInfoList = Arrays.asList(
                    new Pair<RealmFieldType, Object>(RealmFieldType.STRING, new String[] {"string value1", "string value2"}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.INTEGER, new Long[] {100L, 102L}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.BOOLEAN, new Boolean[] {false, true}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.BINARY, new byte[][] {new byte[] {123}, new byte[] {-123}}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.DATE, new Date[] {new Date(123456), new Date(13579)}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.FLOAT, new Float[] {1.234F, 100F}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.DOUBLE, new Double[] {Math.PI, Math.E}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.OBJECT, new Long[] {0L, 1L})
                    // FIXME: Currently, LIST does not support default value.
                    // new CollectionChange<RealmFieldType, Object>(RealmFieldType.LIST, )
            );

            for (Pair<RealmFieldType, Object> columnInfo : columnInfoList) {
                final RealmFieldType type = columnInfo.first;
                if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
                    table.addColumnLink(type, type.name().toLowerCase(Locale.ENGLISH) + "Col", table);
                } else {
                    table.addColumn(type, type.name().toLowerCase(Locale.ENGLISH) + "Col");
                }
            }

            sharedRealm.beginTransaction();
            OsObject.createRow(table);
            OsObject.createRow(table); // For link field update.

            ListIterator<Pair<RealmFieldType, Object>> it = columnInfoList.listIterator();
            for (int columnIndex = 0; columnIndex < columnInfoList.size(); columnIndex++) {
                Pair<RealmFieldType, Object> columnInfo = it.next();
                final RealmFieldType type = columnInfo.first;
                final Object value1 = ((Object[]) columnInfo.second)[0];
                final Object value2 = ((Object[]) columnInfo.second)[1];

                switch (type) {
                    case STRING:
                        table.setString(columnIndex, 0, (String) value1, true);
                        table.setString(columnIndex, 0, (String) value2, true);
                        assertEquals(value2, table.getString(columnIndex, 0));
                        break;
                    case INTEGER:
                        table.setLong(columnIndex, 0, (long) value1, true);
                        table.setLong(columnIndex, 0, (long) value2, true);
                        assertEquals(value2, table.getLong(columnIndex, 0));
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnIndex, 0, (boolean) value1, true);
                        table.setBoolean(columnIndex, 0, (boolean) value2, true);
                        assertEquals(value2, table.getBoolean(columnIndex, 0));
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnIndex, 0, (byte[]) value1, true);
                        table.setBinaryByteArray(columnIndex, 0, (byte[]) value2, true);
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnIndex, 0)));
                        break;
                    case DATE:
                        table.setDate(columnIndex, 0, (Date) value1, true);
                        table.setDate(columnIndex, 0, (Date) value2, true);
                        assertEquals(value2, table.getDate(columnIndex, 0));
                        break;
                    case FLOAT:
                        table.setFloat(columnIndex, 0, (float) value1, true);
                        table.setFloat(columnIndex, 0, (float) value2, true);
                        assertEquals(value2, table.getFloat(columnIndex, 0));
                        break;
                    case DOUBLE:
                        table.setDouble(columnIndex, 0, (double) value1, true);
                        table.setDouble(columnIndex, 0, (double) value2, true);
                        assertEquals(value2, table.getDouble(columnIndex, 0));
                        break;
                    case OBJECT:
                        table.setLink(columnIndex, 0, (long) value1, true);
                        table.setLink(columnIndex, 0, (long) value2, true);
                        assertEquals(value2, table.getLink(columnIndex, 0));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Checks if the value can be read after committing transaction.
            it = columnInfoList.listIterator();
            for (int columnIndex = 0; columnIndex < columnInfoList.size(); columnIndex++) {
                Pair<RealmFieldType, Object> columnInfo = it.next();
                final RealmFieldType type = columnInfo.first;
                final Object value2 = ((Object[]) columnInfo.second)[1];

                switch (type) {
                    case STRING:
                        assertEquals(value2, table.getString(columnIndex, 0));
                        break;
                    case INTEGER:
                        assertEquals(value2, table.getLong(columnIndex, 0));
                        break;
                    case BOOLEAN:
                        assertEquals(value2, table.getBoolean(columnIndex, 0));
                        break;
                    case BINARY:
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnIndex, 0)));
                        break;
                    case DATE:
                        assertEquals(value2, table.getDate(columnIndex, 0));
                        break;
                    case FLOAT:
                        assertEquals(value2, table.getFloat(columnIndex, 0));
                        break;
                    case DOUBLE:
                        assertEquals(value2, table.getDouble(columnIndex, 0));
                        break;
                    case OBJECT:
                        assertEquals(value2, table.getLink(columnIndex, 0));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
        } finally {
            sharedRealm.close();
        }
    }

    @Test
    public void defaultValue_overwrittenByNonDefault() {
        final OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configFactory.createConfiguration());
        //noinspection TryFinallyCanBeTryWithResources
        try {
            sharedRealm.beginTransaction();
            final Table table = sharedRealm.createTable(Table.getTableNameForClass("DefaultValueTest"));
            sharedRealm.commitTransaction();

            List<Pair<RealmFieldType, Object>> columnInfoList = Arrays.asList(
                    new Pair<RealmFieldType, Object>(RealmFieldType.STRING, new String[] {"string value1", "string value2"}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.INTEGER, new Long[] {100L, 102L}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.BOOLEAN, new Boolean[] {false, true}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.BINARY, new byte[][] {new byte[] {123}, new byte[] {-123}}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.DATE, new Date[] {new Date(123456), new Date(13579)}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.FLOAT, new Float[] {1.234F, 100F}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.DOUBLE, new Double[] {Math.PI, Math.E}),
                    new Pair<RealmFieldType, Object>(RealmFieldType.OBJECT, new Long[] {0L, 1L})
                    // FIXME: Currently, LIST does not support default value.
                    // new CollectionChange<RealmFieldType, Object>(RealmFieldType.LIST, )
            );

            for (Pair<RealmFieldType, Object> columnInfo : columnInfoList) {
                final RealmFieldType type = columnInfo.first;
                if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
                    table.addColumnLink(type, type.name().toLowerCase(Locale.ENGLISH) + "Col", table);
                } else {
                    table.addColumn(type, type.name().toLowerCase(Locale.ENGLISH) + "Col");
                }
            }

            sharedRealm.beginTransaction();
            OsObject.createRow(table);
            OsObject.createRow(table); // For link field update.

            // Sets as default.
            ListIterator<Pair<RealmFieldType, Object>> it = columnInfoList.listIterator();
            for (int columnIndex = 0; columnIndex < columnInfoList.size(); columnIndex++) {
                Pair<RealmFieldType, Object> columnInfo = it.next();
                final RealmFieldType type = columnInfo.first;
                final Object value1 = ((Object[]) columnInfo.second)[0];

                switch (type) {
                    case STRING:
                        table.setString(columnIndex, 0, (String) value1, true);
                        break;
                    case INTEGER:
                        table.setLong(columnIndex, 0, (long) value1, true);
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnIndex, 0, (boolean) value1, true);
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnIndex, 0, (byte[]) value1, true);
                        break;
                    case DATE:
                        table.setDate(columnIndex, 0, (Date) value1, true);
                        break;
                    case FLOAT:
                        table.setFloat(columnIndex, 0, (float) value1, true);
                        break;
                    case DOUBLE:
                        table.setDouble(columnIndex, 0, (double) value1, true);
                        break;
                    case OBJECT:
                        table.setLink(columnIndex, 0, (long) value1, true);
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Updates as non default.
            sharedRealm.beginTransaction();
            it = columnInfoList.listIterator();
            for (int columnIndex = 0; columnIndex < columnInfoList.size(); columnIndex++) {
                Pair<RealmFieldType, Object> columnInfo = it.next();
                final RealmFieldType type = columnInfo.first;
                final Object value2 = ((Object[]) columnInfo.second)[1];

                switch (type) {
                    case STRING:
                        table.setString(columnIndex, 0, (String) value2, false);
                        assertEquals(value2, table.getString(columnIndex, 0));
                        break;
                    case INTEGER:
                        table.setLong(columnIndex, 0, (long) value2, false);
                        assertEquals(value2, table.getLong(columnIndex, 0));
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnIndex, 0, (boolean) value2, false);
                        assertEquals(value2, table.getBoolean(columnIndex, 0));
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnIndex, 0, (byte[]) value2, false);
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnIndex, 0)));
                        break;
                    case DATE:
                        table.setDate(columnIndex, 0, (Date) value2, false);
                        assertEquals(value2, table.getDate(columnIndex, 0));
                        break;
                    case FLOAT:
                        table.setFloat(columnIndex, 0, (float) value2, false);
                        assertEquals(value2, table.getFloat(columnIndex, 0));
                        break;
                    case DOUBLE:
                        table.setDouble(columnIndex, 0, (double) value2, false);
                        assertEquals(value2, table.getDouble(columnIndex, 0));
                        break;
                    case OBJECT:
                        table.setLink(columnIndex, 0, (long) value2, false);
                        assertEquals(value2, table.getLink(columnIndex, 0));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Checks if the value was overwritten.
            it = columnInfoList.listIterator();
            for (int columnIndex = 0; columnIndex < columnInfoList.size(); columnIndex++) {
                Pair<RealmFieldType, Object> columnInfo = it.next();
                final RealmFieldType type = columnInfo.first;
                final Object value2 = ((Object[]) columnInfo.second)[1];

                switch (type) {
                    case STRING:
                        assertEquals(value2, table.getString(columnIndex, 0));
                        break;
                    case INTEGER:
                        assertEquals(value2, table.getLong(columnIndex, 0));
                        break;
                    case BOOLEAN:
                        assertEquals(value2, table.getBoolean(columnIndex, 0));
                        break;
                    case BINARY:
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnIndex, 0)));
                        break;
                    case DATE:
                        assertEquals(value2, table.getDate(columnIndex, 0));
                        break;
                    case FLOAT:
                        assertEquals(value2, table.getFloat(columnIndex, 0));
                        break;
                    case DOUBLE:
                        assertEquals(value2, table.getDouble(columnIndex, 0));
                        break;
                    case OBJECT:
                        assertEquals(value2, table.getLink(columnIndex, 0));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
        } finally {
            sharedRealm.close();
        }
    }
}

