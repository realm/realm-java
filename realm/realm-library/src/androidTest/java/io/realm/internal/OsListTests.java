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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OsListTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private SharedRealm sharedRealm;
    private UncheckedRow row;
    private OsObjectSchemaInfo testObjectSchemaInfo;

    @Before
    public void setUp() {
        OsObjectSchemaInfo objectSchemaInfo = new OsObjectSchemaInfo.Builder("TestModel")
                .addPersistedLinkProperty("longList", RealmFieldType.INTEGER_LIST, null, !Property.REQUIRED)
                .addPersistedLinkProperty("doubleList", RealmFieldType.DOUBLE_LIST, null, !Property.REQUIRED)
                .addPersistedLinkProperty("floatList", RealmFieldType.FLOAT_LIST, null, !Property.REQUIRED)
                .addPersistedLinkProperty("booleanList", RealmFieldType.BOOLEAN_LIST, null, !Property.REQUIRED)
                .addPersistedLinkProperty("binaryList", RealmFieldType.BINARY_LIST, null, !Property.REQUIRED)
                .addPersistedLinkProperty("dateList", RealmFieldType.DATE_LIST, null, !Property.REQUIRED)
                .addPersistedLinkProperty("stringList", RealmFieldType.STRING_LIST, null, !Property.REQUIRED)
                .build();
        List<OsObjectSchemaInfo> objectSchemaInfoList = new ArrayList<OsObjectSchemaInfo>();
        objectSchemaInfoList.add(objectSchemaInfo);

        OsSchemaInfo schemaInfo = new OsSchemaInfo(objectSchemaInfoList);

        RealmConfiguration config = configFactory.createConfiguration();
        OsRealmConfig.Builder configBuilder = new OsRealmConfig.Builder(config)
                .autoUpdateNotification(true)
                .schemaInfo(schemaInfo);
        sharedRealm = SharedRealm.getInstance(configBuilder);
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

    @Test
    public void add_insert_get_Long() {
        long index = testObjectSchemaInfo.getProperty("longList").getColumnIndex();
        OsList osList = new OsList(row, index);

        osList.addLong(42);
        Long value = (Long) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42, value.longValue());

        osList.insertLong(0, 24);
        value = (Long) osList.getValue(0);
        assertNotNull(value);
        assertEquals(24, value.longValue());

        osList.insertNull(0);
        value = (Long) osList.getValue(0);
        assertNull(value);

        osList.addNull();
        assertNull(osList.getValue(3));

        osList.setLong(3, 42);
        value = (Long) osList.getValue(3);
        assertNotNull(value);
        assertEquals(42, value.longValue());
    }

    @Test
    public void add_insert_get_Double() {
        long index = testObjectSchemaInfo.getProperty("doubleList").getColumnIndex();
        OsList osList = new OsList(row, index);

        osList.addDouble(42.0D);
        Double value = (Double) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42.0D, value, 0.0D);

        osList.insertDouble(0, 24.0D);
        value = (Double) osList.getValue(0);
        assertNotNull(value);
        assertEquals(24.0D, value, 0.0D);

        osList.insertNull(0);
        value = (Double) osList.getValue(0);
        assertNull(value);

        osList.addNull();
        assertNull(osList.getValue(3));

        osList.setDouble(3, 42.0D);
        value = (Double) osList.getValue(3);
        assertNotNull(value);
        assertEquals(42.0D, value, 0.0D);
    }

    @Test
    public void add_insert_get_Float() {
        long index = testObjectSchemaInfo.getProperty("floatList").getColumnIndex();
        OsList osList = new OsList(row, index);

        osList.addFloat(42.0F);
        Float value = (Float) osList.getValue(0);
        assertNotNull(value);
        assertEquals(42.0F, value, 0.0F);

        osList.insertFloat(0, 24.0F);
        value = (Float) osList.getValue(0);
        assertNotNull(value);
        assertEquals(24.0F, value, 0.0F);

        osList.insertNull(0);
        value = (Float) osList.getValue(0);
        assertNull(value);

        osList.addNull();
        assertNull(osList.getValue(3));

        osList.setFloat(3, 42.0F);
        value = (Float) osList.getValue(3);
        assertNotNull(value);
        assertEquals(42.0F, value, 0.0F);
    }

    @Test
    public void add_insert_get_Boolean() {
        long index = testObjectSchemaInfo.getProperty("booleanList").getColumnIndex();
        OsList osList = new OsList(row, index);

        osList.addBoolean(true);
        Boolean value = (Boolean) osList.getValue(0);
        assertNotNull(value);
        assertTrue(value);

        osList.insertBoolean(0, false);
        value = (Boolean) osList.getValue(0);
        assertNotNull(value);
        assertFalse(value);

        osList.insertNull(0);
        value = (Boolean) osList.getValue(0);
        assertNull(value);

        osList.addNull();
        assertNull(osList.getValue(3));

        osList.setBoolean(3, false);
        value = (Boolean) osList.getValue(3);
        assertNotNull(value);
        assertFalse(value);
    }

    @Test
    public void add_insert_get_Date() {
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
}
