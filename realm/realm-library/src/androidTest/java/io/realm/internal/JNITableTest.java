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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
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
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);
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
                long colKey1 = t.addColumn(RealmFieldType.STRING, "stringCol");
                long colKey2 = t.addColumn(RealmFieldType.INTEGER, "intCol");
                long colKey3 = t.addColumn(RealmFieldType.BOOLEAN, "boolCol");

                TestHelper.addRowWithValues(t, new long[]{colKey1, colKey2, colKey3}, new Object[]{"s1", 1, true});
                TestHelper.addRowWithValues(t, new long[]{colKey1, colKey2, colKey3}, new Object[]{"s2", 2, false});
            }
        });

        String expected = "The Table temp contains 3 columns: stringCol, intCol, boolCol. And 2 rows.";
        assertEquals(expected, t.toString());
    }

    @Test
    public void findFirstNonExisting() {
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        long colKey1 = t.getColumnKey("binary");
        long colKey2 = t.getColumnKey("boolean");
        long colKey3 = t.getColumnKey("date");
        long colKey4 = t.getColumnKey("double");
        long colKey5 = t.getColumnKey("float");
        long colKey6 = t.getColumnKey("long");
        long colKey7 = t.getColumnKey("string");

        sharedRealm.beginTransaction();
        TestHelper.addRowWithValues(t, new long[]{colKey1, colKey2, colKey3, colKey4, colKey5, colKey6, colKey7}, new Object[] {new byte[] {1, 2, 3}, true, new Date(1384423149761L), 4.5D, 5.7F, 100, "string"});
        sharedRealm.commitTransaction();

        assertEquals(-1, t.findFirstBoolean(colKey2, false));
        assertEquals(-1, t.findFirstDate(colKey3, new Date(138442314986L)));
        assertEquals(-1, t.findFirstDouble(colKey4, 1.0D));
        assertEquals(-1, t.findFirstFloat(colKey5, 1.0F));
        assertEquals(-1, t.findFirstLong(colKey6, 50));
    }

    @Test
    public void findFirst() {
        final int TEST_SIZE = 10;
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);
        long colKey1 = t.getColumnKey("binary");
        long colKey2 = t.getColumnKey("boolean");
        long colKey3 = t.getColumnKey("date");
        long colKey4 = t.getColumnKey("double");
        long colKey5 = t.getColumnKey("float");
        long colKey6 = t.getColumnKey("long");
        long colKey7 = t.getColumnKey("string");
        sharedRealm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            TestHelper.addRowWithValues(t, new long[]{colKey1, colKey2, colKey3, colKey4, colKey5, colKey6, colKey7}, new Object[] {new byte[] {1, 2, 3}, true, new Date(i), (double) i, (float) i, i, "string " + i});
        }
        TestHelper.addRowWithValues(t, new long[]{colKey1, colKey2, colKey3, colKey4, colKey5, colKey6, colKey7}, new Object[] {new byte[] {1, 2, 3}, true, new Date(TEST_SIZE), (double) TEST_SIZE, (float) TEST_SIZE, TEST_SIZE, ""});
        sharedRealm.commitTransaction();

        assertEquals(0, t.findFirstBoolean(colKey2, true));
        for (int i = 0; i < TEST_SIZE; i++) {
            assertEquals(i, t.findFirstDate(colKey3, new Date(i)));
            assertEquals(i, t.findFirstDouble(colKey4, (double) i));
            assertEquals(i, t.findFirstFloat(colKey5, (float) i));
            assertEquals(i, t.findFirstLong(colKey6, i));
        }

        try {
            t.findFirstString(colKey7, null);
            fail();
        } catch (IllegalArgumentException ignored) {}

        try {
            t.findFirstDate(colKey3, null);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }


    @Test
    public void getNonExistingColumn() {
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                t.addColumn(RealmFieldType.INTEGER, "int");
            }
        });

        assertEquals(-1, t.getColumnKey("non-existing column"));
        try {
            t.getColumnKey(null);
            fail("column name null");
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void setNulls() {
        final AtomicLong colKey1 = new AtomicLong(-1);
        final AtomicLong colKey2 = new AtomicLong(-1);
        final AtomicLong colKey3 = new AtomicLong(-1);
        final AtomicLong rowKey = new AtomicLong(-1);
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                colKey1.set(t.addColumn(RealmFieldType.STRING, "col1"));
                colKey2.set(t.addColumn(RealmFieldType.DATE, "col2"));
                colKey3.set(t.addColumn(RealmFieldType.BINARY, "col3"));
                rowKey.set(TestHelper.addRowWithValues(t, new long[]{colKey1.get(), colKey2.get(), colKey3.get()},
                        new Object[]{"String val", new Date(), new byte[] {1, 2, 3}}));
            }
        });

        sharedRealm.beginTransaction();
        try {
            t.setString(colKey1.get(), rowKey.get(), null, false);
            fail("null string not allowed");
        } catch (IllegalArgumentException ignored) { }
        try {
            t.setDate(colKey2.get(), rowKey.get(), null, false);
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

        long columnKey1 = t.getColumnKey("binary");
        long columnKey2 = t.getColumnKey("boolean");
        long columnKey3 = t.getColumnKey("date");
        long columnKey4 = t.getColumnKey("double");
        long columnKey5 = t.getColumnKey("float");
        long columnKey6 = t.getColumnKey("long");
        long columnKey7 = t.getColumnKey("string");

        long[] columnsKeys = new long[]{columnKey1, columnKey2, columnKey3, columnKey4, columnKey5, columnKey6, columnKey7};

        for (int i = 0; i < columnsKeys.length; i++) {

            // All types supported addSearchIndex and removeSearchIndex.
            boolean exceptionExpected = (
                    t.getColumnType(columnsKeys[i]) != RealmFieldType.STRING &&
                            t.getColumnType(columnsKeys[i]) != RealmFieldType.INTEGER &&
                            t.getColumnType(columnsKeys[i]) != RealmFieldType.BOOLEAN &&
                            t.getColumnType(columnsKeys[i]) != RealmFieldType.DATE);

            // Tries to addSearchIndex().
            sharedRealm.beginTransaction();
            try {
                t.addSearchIndex(columnsKeys[i]);
                if (exceptionExpected) {
                    fail("Expected exception for colIndex " + columnsKeys[i]);
                }
            } catch (IllegalArgumentException ignored) {
            }
            sharedRealm.commitTransaction();

            // Tries to removeSearchIndex().
            sharedRealm.beginTransaction();
            try {
                // Currently core will do nothing if the column doesn't have a search index.
                t.removeSearchIndex(columnsKeys[i]);
                if (exceptionExpected) {
                    fail("Expected exception for colIndex " + columnsKeys[i]);
                }
            } catch (IllegalArgumentException ignored) {
            }
            sharedRealm.commitTransaction();

            // Tries to hasSearchIndex() for all columnTypes.
            t.hasSearchIndex(columnsKeys[i]);
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
        final AtomicLong colKey1 = new AtomicLong(-1);
        final AtomicLong colKey2 = new AtomicLong(-1);
        final AtomicLong colKey3 = new AtomicLong(-1);
        final AtomicLong colKey4 = new AtomicLong(-1);

        final AtomicLong rowKey0 = new AtomicLong(-1);
        final AtomicLong rowKey1 = new AtomicLong(-1);
        final AtomicLong rowKey2 = new AtomicLong(-1);
        final AtomicLong rowKey3 = new AtomicLong(-1);
        final AtomicLong rowKey4 = new AtomicLong(-1);
        final AtomicLong rowKey5 = new AtomicLong(-1);
        final AtomicLong rowKey6 = new AtomicLong(-1);
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                colKey1.set(t.addColumn(RealmFieldType.INTEGER, "intCol"));
                colKey2.set(t.addColumn(RealmFieldType.DOUBLE, "doubleCol"));
                colKey3.set(t.addColumn(RealmFieldType.FLOAT, "floatCol"));
                colKey4.set(t.addColumn(RealmFieldType.STRING, "StringCol"));

                // Adds 3 rows of data with same values in each column.
                rowKey0.set(TestHelper.addRowWithValues(t, new long[]{colKey1.get(), colKey2.get(), colKey3.get(), colKey4.get()}, new Object[]{1, 2.0D, 3.0F, "s1"}));
                rowKey1.set(TestHelper.addRowWithValues(t, new long[]{colKey1.get(), colKey2.get(), colKey3.get(), colKey4.get()}, new Object[]{1, 2.0D, 3.0F, "s1"}));
                rowKey2.set(TestHelper.addRowWithValues(t, new long[]{colKey1.get(), colKey2.get(), colKey3.get(), colKey4.get()}, new Object[]{1, 2.0D, 3.0F, "s1"}));

                // Adds other values.
                rowKey3.set(TestHelper.addRowWithValues(t, new long[]{colKey1.get(), colKey2.get(), colKey3.get(), colKey4.get()}, new Object[]{10, 20.0D, 30.0F, "s10"}));
                rowKey4.set(TestHelper.addRowWithValues(t, new long[]{colKey1.get(), colKey2.get(), colKey3.get(), colKey4.get()}, new Object[]{100, 200.0D, 300.0F, "s100"}));
                rowKey5.set(TestHelper.addRowWithValues(t, new long[]{colKey1.get(), colKey2.get(), colKey3.get(), colKey4.get()}, new Object[]{1000, 2000.0D, 3000.0F, "s1000"}));
            }
        });

        // Counts instances of values added in the first 3 rows.
        assertEquals(3, t.count(colKey1.get(), 1));
        assertEquals(3, t.count(colKey2.get(), 2.0D));
        assertEquals(3, t.count(colKey3.get(), 3.0F));
        assertEquals(3, t.count(colKey4.get(), "s1"));


        assertEquals(3, t.findFirstDouble(colKey2.get(), 20.0D)); // Find rows index for first double value of 20.0 in column 1.
        assertEquals(4, t.findFirstFloat(colKey3.get(), 300.0F)); // Find rows index for first float value of 300.0 in column 2.

        // Sets double and float.
        sharedRealm.beginTransaction();
        t.setDouble(colKey2.get(), rowKey2.get(), -2.0D, false);
        t.setFloat(colKey3.get(), rowKey2.get(), -3.0F, false);
        sharedRealm.commitTransaction();

        // Gets double tests.
        assertEquals(-2.0D, t.getDouble(colKey2.get(), rowKey2.get()));
        assertEquals(20.0D, t.getDouble(colKey2.get(), rowKey3.get()));
        assertEquals(200.0D, t.getDouble(colKey2.get(), rowKey4.get()));
        assertEquals(2000.0D, t.getDouble(colKey2.get(), rowKey5.get()));

        // Gets float test.
        assertEquals(-3.0F, t.getFloat(colKey3.get(), rowKey2.get()));
        assertEquals(30.0F, t.getFloat(colKey3.get(), rowKey3.get()));
        assertEquals(300.0F, t.getFloat(colKey3.get(), rowKey4.get()));
        assertEquals(3000.0F, t.getFloat(colKey3.get(), rowKey5.get()));
    }

    // Adds column and read back if it is nullable or not.
    @Test
    public void isNullable() {
        final AtomicLong columnKey0 = new AtomicLong(-1);
        final AtomicLong columnKey1 = new AtomicLong(-1);
        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                columnKey0.set(table.addColumn(RealmFieldType.STRING, "string1", Table.NOT_NULLABLE));
                columnKey1.set(table.addColumn(RealmFieldType.STRING, "string2", Table.NULLABLE));
            }
        });

        assertFalse(table.isColumnNullable(columnKey0.get()));
        assertTrue(table.isColumnNullable(columnKey1.get()));
    }

    @Test
    public void defaultValue_setAndGet() {
        final OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configFactory.createConfiguration(), OsSharedRealm.VersionID.LIVE);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            sharedRealm.beginTransaction();
            final Table table = sharedRealm.createTable(Table.getTableNameForClass("DefaultValueTest"));
            long colKey1 = table.addColumn(RealmFieldType.STRING, RealmFieldType.STRING.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey2 = table.addColumn(RealmFieldType.INTEGER, RealmFieldType.INTEGER.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey3 = table.addColumn(RealmFieldType.BOOLEAN, RealmFieldType.BOOLEAN.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey4 = table.addColumn(RealmFieldType.BINARY, RealmFieldType.BINARY.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey5 = table.addColumn(RealmFieldType.DATE, RealmFieldType.DATE.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey6 = table.addColumn(RealmFieldType.FLOAT, RealmFieldType.FLOAT.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey7 = table.addColumn(RealmFieldType.DOUBLE, RealmFieldType.DOUBLE.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey8 = table.addColumnLink(RealmFieldType.OBJECT, RealmFieldType.OBJECT.name().toLowerCase(Locale.ENGLISH) + "Col", table);

            long[] columnKeys = new long[]{colKey1, colKey2, colKey3, colKey4, colKey5, colKey6, colKey7, colKey8};
            Object[] datas = new Object[]{"string value",
                    100L,
                    true,
                    new byte[]{123},
                    new Date(123456),
                    1.234F,
                    Math.PI,
                    0L};

            RealmFieldType[] types = new RealmFieldType[]{RealmFieldType.STRING,
                    RealmFieldType.INTEGER,
                    RealmFieldType.BOOLEAN,
                    RealmFieldType.BINARY,
                    RealmFieldType.DATE,
                    RealmFieldType.FLOAT,
                    RealmFieldType.DOUBLE,
                    RealmFieldType.OBJECT};

            long rowKey = OsObject.createRow(table);

            for (int i = 0; i < columnKeys.length; i++) {
                final RealmFieldType type = types[i];
                final Object value = datas[i];

                switch (type) {
                    case STRING:
                        table.setString(columnKeys[i], rowKey, (String) value, true);
                        assertEquals(value, table.getString(columnKeys[i], rowKey));
                        break;
                    case INTEGER:
                        table.setLong(columnKeys[i], rowKey, (long) value, true);
                        assertEquals(value, table.getLong(columnKeys[i], rowKey));
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnKeys[i], rowKey, (boolean) value, true);
                        assertEquals(value, table.getBoolean(columnKeys[i], rowKey));
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnKeys[i], rowKey, (byte[]) value, true);
                        assertTrue(Arrays.equals((byte[]) value, table.getBinaryByteArray(columnKeys[i], rowKey)));
                        break;
                    case DATE:
                        table.setDate(columnKeys[i], rowKey, (Date) value, true);
                        assertEquals(value, table.getDate(columnKeys[i], rowKey));
                        break;
                    case FLOAT:
                        table.setFloat(columnKeys[i], rowKey, (float) value, true);
                        assertEquals(value, table.getFloat(columnKeys[i], rowKey));
                        break;
                    case DOUBLE:
                        table.setDouble(columnKeys[i], rowKey, (double) value, true);
                        assertEquals(value, table.getDouble(columnKeys[i], rowKey));
                        break;
                    case OBJECT:
                        table.setLink(columnKeys[i], rowKey, (long) value, true);
                        assertEquals(value, table.getLink(columnKeys[i], rowKey));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Checks if the value can be read after committing transaction.
            for (int i = 0; i < columnKeys.length; i++) {
                final RealmFieldType type = types[i];
                final Object value = datas[i];

                switch (type) {
                    case STRING:
                        assertEquals(value, table.getString(columnKeys[i], rowKey));
                        break;
                    case INTEGER:
                        assertEquals(value, table.getLong(columnKeys[i], rowKey));
                        break;
                    case BOOLEAN:
                        assertEquals(value, table.getBoolean(columnKeys[i], rowKey));
                        break;
                    case BINARY:
                        assertTrue(Arrays.equals((byte[]) value, table.getBinaryByteArray(columnKeys[i], rowKey)));
                        break;
                    case DATE:
                        assertEquals(value, table.getDate(columnKeys[i], rowKey));
                        break;
                    case FLOAT:
                        assertEquals(value, table.getFloat(columnKeys[i], rowKey));
                        break;
                    case DOUBLE:
                        assertEquals(value, table.getDouble(columnKeys[i], rowKey));
                        break;
                    case OBJECT:
                        assertEquals(value, table.getLink(columnKeys[i], rowKey));
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
        final OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configFactory.createConfiguration(), OsSharedRealm.VersionID.LIVE);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            sharedRealm.beginTransaction();
            final Table table = sharedRealm.createTable(Table.getTableNameForClass("DefaultValueTest"));
            long colKey1 = table.addColumn(RealmFieldType.STRING, RealmFieldType.STRING.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey2 = table.addColumn(RealmFieldType.INTEGER, RealmFieldType.INTEGER.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey3 = table.addColumn(RealmFieldType.BOOLEAN, RealmFieldType.BOOLEAN.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey4 = table.addColumn(RealmFieldType.BINARY, RealmFieldType.BINARY.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey5 = table.addColumn(RealmFieldType.DATE, RealmFieldType.DATE.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey6 = table.addColumn(RealmFieldType.FLOAT, RealmFieldType.FLOAT.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey7 = table.addColumn(RealmFieldType.DOUBLE, RealmFieldType.DOUBLE.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey8 = table.addColumnLink(RealmFieldType.OBJECT, RealmFieldType.OBJECT.name().toLowerCase(Locale.ENGLISH) + "Col", table);


            long[] columnKeys = new long[]{colKey1, colKey2, colKey3, colKey4, colKey5, colKey6, colKey7, colKey8};
            Object[] datas = new Object[]{new String[] {"string value1", "string value2"},
                    new Long[] {100L, 102L},
                    new Boolean[] {false, true},
                    new byte[][] {new byte[] {123}, new byte[] {-123}},
                    new Date[] {new Date(123456), new Date(13579)},
                    new Float[] {1.234F, 100F},
                    new Double[] {Math.PI, Math.E},
                    new Long[] {0L, 1L}

            };
            RealmFieldType[] types = new RealmFieldType[]{RealmFieldType.STRING,
                    RealmFieldType.INTEGER,
                    RealmFieldType.BOOLEAN,
                    RealmFieldType.BINARY,
                    RealmFieldType.DATE,
                    RealmFieldType.FLOAT,
                    RealmFieldType.DOUBLE,
                    RealmFieldType.OBJECT};

            long rowKey = OsObject.createRow(table);
            OsObject.createRow(table); // For link field update.

            for (int i = 0; i < columnKeys.length; i++) {
                final RealmFieldType type = types[i];
                final Object value1 = ((Object[]) datas[i])[0];
                final Object value2 = ((Object[]) datas[i])[1];

                switch (type) {
                    case STRING:
                        table.setString(columnKeys[i], rowKey, (String) value1, true);
                        table.setString(columnKeys[i], rowKey, (String) value2, true);
                        assertEquals(value2, table.getString(columnKeys[i], rowKey));
                        break;
                    case INTEGER:
                        table.setLong(columnKeys[i], rowKey, (long) value1, true);
                        table.setLong(columnKeys[i], rowKey, (long) value2, true);
                        assertEquals(value2, table.getLong(columnKeys[i], rowKey));
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnKeys[i], rowKey, (boolean) value1, true);
                        table.setBoolean(columnKeys[i], rowKey, (boolean) value2, true);
                        assertEquals(value2, table.getBoolean(columnKeys[i], rowKey));
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnKeys[i], rowKey, (byte[]) value1, true);
                        table.setBinaryByteArray(columnKeys[i], rowKey, (byte[]) value2, true);
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnKeys[i], rowKey)));
                        break;
                    case DATE:
                        table.setDate(columnKeys[i], rowKey, (Date) value1, true);
                        table.setDate(columnKeys[i], rowKey, (Date) value2, true);
                        assertEquals(value2, table.getDate(columnKeys[i], rowKey));
                        break;
                    case FLOAT:
                        table.setFloat(columnKeys[i], rowKey, (float) value1, true);
                        table.setFloat(columnKeys[i], rowKey, (float) value2, true);
                        assertEquals(value2, table.getFloat(columnKeys[i], rowKey));
                        break;
                    case DOUBLE:
                        table.setDouble(columnKeys[i], rowKey, (double) value1, true);
                        table.setDouble(columnKeys[i], rowKey, (double) value2, true);
                        assertEquals(value2, table.getDouble(columnKeys[i], rowKey));
                        break;
                    case OBJECT:
                        table.setLink(columnKeys[i], rowKey, (long) value1, true);
                        table.setLink(columnKeys[i], rowKey, (long) value2, true);
                        assertEquals(value2, table.getLink(columnKeys[i], rowKey));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Checks if the value can be read after committing transaction.
            for (int i = 0; i < columnKeys.length; i++) {
                final RealmFieldType type = types[i];
                final Object value2 = ((Object[]) datas[i])[1];

                switch (type) {
                    case STRING:
                        assertEquals(value2, table.getString(columnKeys[i], rowKey));
                        break;
                    case INTEGER:
                        assertEquals(value2, table.getLong(columnKeys[i], rowKey));
                        break;
                    case BOOLEAN:
                        assertEquals(value2, table.getBoolean(columnKeys[i], rowKey));
                        break;
                    case BINARY:
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnKeys[i], rowKey)));
                        break;
                    case DATE:
                        assertEquals(value2, table.getDate(columnKeys[i], rowKey));
                        break;
                    case FLOAT:
                        assertEquals(value2, table.getFloat(columnKeys[i], rowKey));
                        break;
                    case DOUBLE:
                        assertEquals(value2, table.getDouble(columnKeys[i], rowKey));
                        break;
                    case OBJECT:
                        assertEquals(value2, table.getLink(columnKeys[i], rowKey));
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
        final OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configFactory.createConfiguration(), OsSharedRealm.VersionID.LIVE);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            sharedRealm.beginTransaction();
            final Table table = sharedRealm.createTable(Table.getTableNameForClass("DefaultValueTest"));
            long colKey1 = table.addColumn(RealmFieldType.STRING, RealmFieldType.STRING.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey2 = table.addColumn(RealmFieldType.INTEGER, RealmFieldType.INTEGER.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey3 = table.addColumn(RealmFieldType.BOOLEAN, RealmFieldType.BOOLEAN.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey4 = table.addColumn(RealmFieldType.BINARY, RealmFieldType.BINARY.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey5 = table.addColumn(RealmFieldType.DATE, RealmFieldType.DATE.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey6 = table.addColumn(RealmFieldType.FLOAT, RealmFieldType.FLOAT.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey7 = table.addColumn(RealmFieldType.DOUBLE, RealmFieldType.DOUBLE.name().toLowerCase(Locale.ENGLISH) + "Col");
            long colKey8 = table.addColumnLink(RealmFieldType.OBJECT, RealmFieldType.OBJECT.name().toLowerCase(Locale.ENGLISH) + "Col", table);

            long[] columnKeys = new long[]{colKey1, colKey2, colKey3, colKey4, colKey5, colKey6, colKey7, colKey8};
            Object[] datas = new Object[]{new String[] {"string value1", "string value2"},
                    new Long[] {100L, 102L},
                    new Boolean[] {false, true},
                    new byte[][] {new byte[] {123}, new byte[] {-123}},
                    new Date[] {new Date(123456), new Date(13579)},
                    new Float[] {1.234F, 100F},
                    new Double[] {Math.PI, Math.E},
                    new Long[] {0L, 1L}

            };
            RealmFieldType[] types = new RealmFieldType[]{RealmFieldType.STRING,
                    RealmFieldType.INTEGER,
                    RealmFieldType.BOOLEAN,
                    RealmFieldType.BINARY,
                    RealmFieldType.DATE,
                    RealmFieldType.FLOAT,
                    RealmFieldType.DOUBLE,
                    RealmFieldType.OBJECT};
            long rowKey = OsObject.createRow(table);
            OsObject.createRow(table); // For link field update.

            // Sets as default.
            for (int i = 0; i< columnKeys.length; i++) {
                final RealmFieldType type = types[i];
                final Object value1 = ((Object[]) datas[i])[0];

                switch (type) {
                    case STRING:
                        table.setString(columnKeys[i], rowKey, (String) value1, true);
                        break;
                    case INTEGER:
                        table.setLong(columnKeys[i], rowKey, (long) value1, true);
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnKeys[i], rowKey, (boolean) value1, true);
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnKeys[i], rowKey, (byte[]) value1, true);
                        break;
                    case DATE:
                        table.setDate(columnKeys[i], rowKey, (Date) value1, true);
                        break;
                    case FLOAT:
                        table.setFloat(columnKeys[i], rowKey, (float) value1, true);
                        break;
                    case DOUBLE:
                        table.setDouble(columnKeys[i], rowKey, (double) value1, true);
                        break;
                    case OBJECT:
                        table.setLink(columnKeys[i], rowKey, (long) value1, true);
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Updates as non default.
            sharedRealm.beginTransaction();
            for (int i = 0; i< columnKeys.length; i++) {
                final RealmFieldType type = types[i];
                final Object value2 = ((Object[]) datas[i])[1];

                switch (type) {
                    case STRING:
                        table.setString(columnKeys[i], rowKey, (String) value2, false);
                        assertEquals(value2, table.getString(columnKeys[i], rowKey));
                        break;
                    case INTEGER:
                        table.setLong(columnKeys[i], rowKey, (long) value2, false);
                        assertEquals(value2, table.getLong(columnKeys[i], rowKey));
                        break;
                    case BOOLEAN:
                        table.setBoolean(columnKeys[i], rowKey, (boolean) value2, false);
                        assertEquals(value2, table.getBoolean(columnKeys[i], rowKey));
                        break;
                    case BINARY:
                        table.setBinaryByteArray(columnKeys[i], rowKey, (byte[]) value2, false);
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnKeys[i], rowKey)));
                        break;
                    case DATE:
                        table.setDate(columnKeys[i], rowKey, (Date) value2, false);
                        assertEquals(value2, table.getDate(columnKeys[i], rowKey));
                        break;
                    case FLOAT:
                        table.setFloat(columnKeys[i], rowKey, (float) value2, false);
                        assertEquals(value2, table.getFloat(columnKeys[i], rowKey));
                        break;
                    case DOUBLE:
                        table.setDouble(columnKeys[i], rowKey, (double) value2, false);
                        assertEquals(value2, table.getDouble(columnKeys[i], rowKey));
                        break;
                    case OBJECT:
                        table.setLink(columnKeys[i], 0, (long) value2, false);
                        assertEquals(value2, table.getLink(columnKeys[i], rowKey));
                        break;
                    default:
                        throw new RuntimeException("unexpected field type: " + type);
                }
            }
            sharedRealm.commitTransaction();

            // Checks if the value was overwritten.
            for (int i = 0; i < columnKeys.length; i++) {
                final RealmFieldType type = types[i];
                final Object value2 = ((Object[]) datas[i])[1];

                switch (type) {
                    case STRING:
                        assertEquals(value2, table.getString(columnKeys[i], rowKey));
                        break;
                    case INTEGER:
                        assertEquals(value2, table.getLong(columnKeys[i], rowKey));
                        break;
                    case BOOLEAN:
                        assertEquals(value2, table.getBoolean(columnKeys[i], rowKey));
                        break;
                    case BINARY:
                        assertTrue(Arrays.equals((byte[]) value2, table.getBinaryByteArray(columnKeys[i], rowKey)));
                        break;
                    case DATE:
                        assertEquals(value2, table.getDate(columnKeys[i], rowKey));
                        break;
                    case FLOAT:
                        assertEquals(value2, table.getFloat(columnKeys[i], rowKey));
                        break;
                    case DOUBLE:
                        assertEquals(value2, table.getDouble(columnKeys[i], rowKey));
                        break;
                    case OBJECT:
                        assertEquals(value2, table.getLink(columnKeys[i], rowKey));
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

