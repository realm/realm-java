/*
 * Copyright 2017 Realm Inc.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OsListTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private OsSharedRealm sharedRealm;
    private UncheckedRow row;
    private OsObjectSchemaInfo testObjectSchemaInfo;

    @Before
    public void setUp() {
        OsObjectSchemaInfo objectSchemaInfo = new OsObjectSchemaInfo.Builder("TestModel",14, 0)
                .addPersistedValueListProperty("longList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED)
                .addPersistedValueListProperty("doubleList", RealmFieldType.DOUBLE_LIST,  !Property.REQUIRED)
                .addPersistedValueListProperty("floatList", RealmFieldType.FLOAT_LIST, !Property.REQUIRED)
                .addPersistedValueListProperty("booleanList", RealmFieldType.BOOLEAN_LIST, !Property.REQUIRED)
                .addPersistedValueListProperty("binaryList", RealmFieldType.BINARY_LIST, !Property.REQUIRED)
                .addPersistedValueListProperty("dateList", RealmFieldType.DATE_LIST, !Property.REQUIRED)
                .addPersistedValueListProperty("stringList", RealmFieldType.STRING_LIST, !Property.REQUIRED)

                .addPersistedValueListProperty("requiredLongList", RealmFieldType.INTEGER_LIST, Property.REQUIRED)
                .addPersistedValueListProperty("requiredDoubleList", RealmFieldType.DOUBLE_LIST, Property.REQUIRED)
                .addPersistedValueListProperty("requiredFloatList", RealmFieldType.FLOAT_LIST, Property.REQUIRED)
                .addPersistedValueListProperty("requiredBooleanList", RealmFieldType.BOOLEAN_LIST, Property.REQUIRED)
                .addPersistedValueListProperty("requiredBinaryList", RealmFieldType.BINARY_LIST, Property.REQUIRED)
                .addPersistedValueListProperty("requiredDateList", RealmFieldType.DATE_LIST, Property.REQUIRED)
                .addPersistedValueListProperty("requiredStringList", RealmFieldType.STRING_LIST, Property.REQUIRED)

                .build();
        List<OsObjectSchemaInfo> objectSchemaInfoList = new ArrayList<OsObjectSchemaInfo>();
        objectSchemaInfoList.add(objectSchemaInfo);

        OsSchemaInfo schemaInfo = new OsSchemaInfo(objectSchemaInfoList);

        RealmConfiguration config = configFactory.createConfiguration();
        OsRealmConfig.Builder configBuilder = new OsRealmConfig.Builder(config)
                .autoUpdateNotification(true)
                .schemaInfo(schemaInfo);
        sharedRealm = OsSharedRealm.getInstance(configBuilder);
        sharedRealm.beginTransaction();
        Table table = sharedRealm.getTable(Table.getTableNameForClass("TestModel"));
        row = table.getUncheckedRow(OsObject.createRow(table));
        sharedRealm.commitTransaction();

        schemaInfo = sharedRealm.getSchemaInfo();
        testObjectSchemaInfo = schemaInfo.getObjectSchemaInfo("TestModel");

        sharedRealm.beginTransaction();
    }

    @After
    public void tearDown() {
        sharedRealm.cancelTransaction();
        sharedRealm.close();
    }

    private void addNull_insertNull_setNull_nullableList(OsList osList) {
        assertNotNull(osList.getValue(1));
        osList.insertNull(1);
        assertNull(osList.getValue(1));

        osList.addNull();
        assertNull(osList.getValue(osList.size() - 1));

        assertNotNull(osList.getValue(2));
        osList.setNull(2);
        assertNull(osList.getValue(2));
    }

    private void addNull_insertNull_setNull_requiredList(OsList osList) {
        long initialSize = osList.size();
        try {
            osList.insertNull(0);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertEquals(initialSize, osList.size());
        }

        initialSize = osList.size();
        try {
            osList.addNull();
            fail();
        } catch (IllegalArgumentException ignored) {
            assertEquals(initialSize, osList.size());
        }

        initialSize = osList.size();
        try {
            osList.setNull(0);
            fail();
        } catch (IllegalArgumentException ignored) {
            assertEquals(initialSize, osList.size());
        }
    }

    private void add_insert_set_values_long(OsList osList) {
        osList.addLong(42);
        Long value = (Long) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42, value.longValue());

        osList.insertLong(0, 24);
        value = (Long) osList.getValue(0);
        assertNotNull(value);
        assertEquals(24, value.longValue());

        osList.setLong(0, 42);
        value = (Long) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42, value.longValue());
    }

    @Test
    public void add_insert_set_get_Long() {
        long index = testObjectSchemaInfo.getProperty("longList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_long(osList);
        addNull_insertNull_setNull_nullableList(osList);
    }

    @Test
    public void add_insert_get_set_required_Long() {
        long index = testObjectSchemaInfo.getProperty("requiredLongList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_long(osList);
        addNull_insertNull_setNull_requiredList(osList);
    }

    private void add_insert_set_values_double(OsList osList) {
        osList.addDouble(42d);
        Double value = (Double) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42d, value.doubleValue(), 0d);

        osList.insertDouble(0, 24);
        value = (Double) osList.getValue(0);
        assertNotNull(value);
        assertEquals(24d, value.longValue(), 0d);

        osList.setDouble(0, 42);
        value = (Double) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42d, value.longValue(), 0d);
    }

    @Test
    public void add_insert_set_get_Double() {
        long index = testObjectSchemaInfo.getProperty("doubleList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_double(osList);
        addNull_insertNull_setNull_nullableList(osList);
    }

    @Test
    public void add_insert_set_get_required_Double() {
        long index = testObjectSchemaInfo.getProperty("requiredDoubleList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_double(osList);
        addNull_insertNull_setNull_requiredList(osList);
    }

    private void add_insert_set_values_float(OsList osList) {
        osList.addFloat(42f);
        Float value = (Float) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42f, value.doubleValue(), 0f);

        osList.insertFloat(0, 24f);
        value = (Float) osList.getValue(0);
        assertNotNull(value);
        assertEquals(24f, value.longValue(), 0f);

        osList.setFloat(0, 42f);
        value = (Float) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42f, value.longValue(), 0f);
    }

    @Test
    public void add_insert_get_Float() {
        long index = testObjectSchemaInfo.getProperty("floatList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_float(osList);
        addNull_insertNull_setNull_nullableList(osList);
    }

    @Test
    public void add_insert_get_required_Float() {
        long index = testObjectSchemaInfo.getProperty("requiredFloatList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_float(osList);
        addNull_insertNull_setNull_requiredList(osList);
    }

    private void add_insert_set_values_boolean(OsList osList) {
        osList.addBoolean(true);
        Boolean value = (Boolean) osList.getValue(0);
        assertNotNull(value);
        assertTrue(value);

        osList.insertBoolean(0, false);
        value = (Boolean) osList.getValue(0);
        assertNotNull(value);
        assertFalse(value);

        osList.setBoolean(0, true);
        value = (Boolean) osList.getValue(0);
        assertNotNull(value);
        assertTrue(value);
    }

    @Test
    public void add_insert_set_get_Boolean() {
        long index = testObjectSchemaInfo.getProperty("booleanList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_boolean(osList);
        addNull_insertNull_setNull_nullableList(osList);
    }

    @Test
    public void add_insert_set_get_required_Boolean() {
        long index = testObjectSchemaInfo.getProperty("requiredBooleanList").getColumnIndex();
        OsList osList = new OsList(row, index);

        add_insert_set_values_boolean(osList);
        addNull_insertNull_setNull_requiredList(osList);
    }

    @Test
    public void add_insert_set_get_Date() {
        long index = testObjectSchemaInfo.getProperty("dateList").getColumnIndex();
        OsList osList = new OsList(row, index);

        Date date42 = new Date(42);
        Date date24 = new Date(24);

        osList.addDate(null);
        Date value = (Date) osList.getValue(0);
        assertNull(value);

        osList.addDate(date42);
        value = (Date) osList.getValue(1);
        assertNotNull(value);
        assertEquals(date42, value);

        osList.insertDate(0, null);
        value = (Date) osList.getValue(0);
        assertNull(value);

        osList.insertDate(0, date24);
        value = (Date) osList.getValue(0);
        assertNotNull(value);
        assertEquals(date24, value);

        osList.insertNull(0);
        value = (Date) osList.getValue(0);
        assertNull(value);

        osList.addNull();
        assertNull(osList.getValue(5));

        osList.setDate(5, date42);
        value = (Date) osList.getValue(5);
        assertNotNull(value);
        assertEquals(date42, value);

        osList.setDate(5, null);
        value = (Date) osList.getValue(5);
        assertNull(value);
    }

    @Test
    public void add_insert_set_null_required_Date() {
        long index = testObjectSchemaInfo.getProperty("requiredDateList").getColumnIndex();
        OsList osList = new OsList(row, index);

        addNull_insertNull_setNull_requiredList(osList);

        try {
            osList.insertDate(0, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            osList.addDate(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            osList.setDate(0, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void add_insert_get_String() {
        long index = testObjectSchemaInfo.getProperty("stringList").getColumnIndex();
        OsList osList = new OsList(row, index);

        osList.addString(null);
        String value = (String) osList.getValue(0);
        assertNull(value);

        osList.addString("42");
        value = (String) osList.getValue(1);
        assertNotNull(value);
        assertEquals("42", value);

        osList.insertString(0, null);
        value = (String) osList.getValue(0);
        assertNull(value);

        osList.insertString(0, "24");
        value = (String) osList.getValue(0);
        assertNotNull(value);
        assertEquals("24", value);

        osList.insertNull(0);
        value = (String) osList.getValue(0);
        assertNull(value);

        osList.addNull();
        assertNull(osList.getValue(5));

        osList.setString(5, "24");
        value = (String) osList.getValue(5);
        assertNotNull(value);
        assertEquals("24", value);

        osList.setString(5, null);
        value = (String) osList.getValue(5);
        assertNull(value);
    }

    @Test
    public void add_insert_set_null_required_String() {
        long index = testObjectSchemaInfo.getProperty("requiredStringList").getColumnIndex();
        OsList osList = new OsList(row, index);

        addNull_insertNull_setNull_requiredList(osList);

        try {
            osList.insertString(0, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            osList.addString(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            osList.setString(0, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void add_insert_get_Binary() {
        long index = testObjectSchemaInfo.getProperty("binaryList").getColumnIndex();
        OsList osList = new OsList(row, index);

        byte[] bytes42 = new byte[1];
        bytes42[0] = 42;
        byte[] bytes24 = new byte[2];
        bytes24[0] = 24;
        bytes24[1] = 24;

        osList.addBinary(null);
        byte[] value = (byte[]) osList.getValue(0);
        assertNull(value);

        osList.addBinary(bytes42);
        value = (byte[]) osList.getValue(1);
        assertNotNull(value);
        assertArrayEquals(bytes42, value);

        osList.insertBinary(0, null);
        value = (byte[]) osList.getValue(0);
        assertNull(value);

        osList.insertBinary(0, bytes24);
        value = (byte[]) osList.getValue(0);
        assertNotNull(value);
        assertArrayEquals(bytes24, value);

        osList.insertNull(0);
        value = (byte[]) osList.getValue(0);
        assertNull(value);

        osList.addNull();
        assertNull(osList.getValue(5));

        osList.setBinary(5, bytes24);
        value = (byte[]) osList.getValue(5);
        assertNotNull(value);
        assertArrayEquals(bytes24, value);

        osList.setBinary(5, null);
        value = (byte[]) osList.getValue(5);
        assertNull(value);
    }

    @Test
    public void add_insert_set_null_required_Binary() {
        long index = testObjectSchemaInfo.getProperty("requiredBinaryList").getColumnIndex();
        OsList osList = new OsList(row, index);

        addNull_insertNull_setNull_requiredList(osList);

        try {
            osList.insertBinary(0, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            osList.addBinary(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            osList.setBinary(0, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
