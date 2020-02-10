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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class JNIRowTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @SuppressWarnings("FieldCanBeLocal")
    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;

    @Before
    public void setUp() throws Exception {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);

        sharedRealm.beginTransaction();
    }

    @After
    public void tearDown() {
        if (sharedRealm != null && sharedRealm.isInTransaction()) {
            sharedRealm.cancelTransaction();
        }

        if (sharedRealm != null && !sharedRealm.isClosed()) {
            sharedRealm.close();
        }
    }

    @Test
    public void nonNullValues() {
        final byte[] data = new byte[2];

        final AtomicLong colKey1 = new AtomicLong(-1);
        final AtomicLong colKey2 = new AtomicLong(-1);
        final AtomicLong colKey3 = new AtomicLong(-1);
        final AtomicLong colKey4 = new AtomicLong(-1);
        final AtomicLong colKey5 = new AtomicLong(-1);
        final AtomicLong colKey6 = new AtomicLong(-1);
        final AtomicLong colKey7 = new AtomicLong(-1);

        Table table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                colKey1.set(table.addColumn(RealmFieldType.STRING, "string"));
                colKey2.set(table.addColumn(RealmFieldType.INTEGER, "integer"));
                colKey3.set(table.addColumn(RealmFieldType.FLOAT, "float"));
                colKey4.set(table.addColumn(RealmFieldType.DOUBLE, "double"));
                colKey5.set(table.addColumn(RealmFieldType.BOOLEAN, "boolean"));
                colKey6.set(table.addColumn(RealmFieldType.DATE, "date"));
                colKey7.set(table.addColumn(RealmFieldType.BINARY, "binary"));

                TestHelper.addRowWithValues(table, new long[]{colKey1.get(), colKey2.get(), colKey3.get(), colKey4.get(), colKey5.get(), colKey6.get(), colKey7.get()}, new Object[]{"abc", 3, (float) 1.2, 1.3, true, new Date(0), data});
            }
        });

        UncheckedRow row = table.getUncheckedRow(0);

        assertEquals("abc", row.getString(colKey1.get()));
        assertEquals(3, row.getLong(colKey2.get()));
        assertEquals(1.2F, row.getFloat(colKey3.get()), Float.MIN_NORMAL);
        assertEquals(1.3, row.getDouble(colKey4.get()), Double.MIN_NORMAL);
        assertEquals(true, row.getBoolean(colKey5.get()));
        assertEquals(new Date(0), row.getDate(colKey6.get()));
        assertArrayEquals(data, row.getBinaryByteArray(colKey7.get()));

        row.setString(colKey1.get(), "a");
        row.setLong(colKey2.get(), 1);
        row.setFloat(colKey3.get(), (float) 8.8);
        row.setDouble(colKey4.get(), 9.9);
        row.setBoolean(colKey5.get(), false);
        row.setDate(colKey6.get(), new Date(10000));

        byte[] newData = new byte[3];
        row.setBinaryByteArray(colKey7.get(), newData);

        assertEquals("a", row.getString(colKey1.get()));
        assertEquals(1, row.getLong(colKey2.get()));
        assertEquals(8.8F, row.getFloat(colKey3.get()), Float.MIN_NORMAL);
        assertEquals(9.9, row.getDouble(colKey4.get()), Double.MIN_NORMAL);
        assertEquals(false, row.getBoolean(colKey5.get()));
        assertEquals(new Date(10000), row.getDate(colKey6.get()));
        assertArrayEquals(newData, row.getBinaryByteArray(colKey7.get()));
    }

    @Test
    public void nullValues() {

        Table table = TestHelper.createTable(sharedRealm, "temp");
        long colStringIndex = table.addColumn(RealmFieldType.STRING, "string", true);
        long colIntIndex = table.addColumn(RealmFieldType.INTEGER, "integer", true);
        table.addColumn(RealmFieldType.FLOAT, "float");
        table.addColumn(RealmFieldType.DOUBLE, "double");
        long colBoolIndex = table.addColumn(RealmFieldType.BOOLEAN, "boolean", true);
        table.addColumn(RealmFieldType.DATE, "date");
        table.addColumn(RealmFieldType.BINARY, "binary");
        long rowIndex = OsObject.createRow(table);

        UncheckedRow row = table.getUncheckedRow(rowIndex);

        row.setString(colStringIndex, "test");
        assertEquals("test", row.getString(colStringIndex));
        row.setNull(colStringIndex);
        assertNull(row.getString(colStringIndex));

        row.setLong(colIntIndex, 1);
        assertFalse(row.isNull(colIntIndex));
        row.setNull(colIntIndex);
        assertTrue(row.isNull(colIntIndex));

        row.setBoolean(colBoolIndex, true);
        assertFalse(row.isNull(colBoolIndex));
        row.setNull(colBoolIndex);
        assertTrue(row.isNull(colBoolIndex));
    }

}
