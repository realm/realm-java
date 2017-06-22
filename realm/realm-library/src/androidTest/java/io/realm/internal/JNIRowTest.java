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
import android.test.MoreAsserts;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import io.realm.RealmFieldType;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class JNIRowTest {

    @Test
    public void nonNullValues() {

        Table table = new Table();

        table.addColumn(RealmFieldType.STRING, "string");
        table.addColumn(RealmFieldType.INTEGER, "integer");
        table.addColumn(RealmFieldType.FLOAT, "float");
        table.addColumn(RealmFieldType.DOUBLE, "double");
        table.addColumn(RealmFieldType.BOOLEAN, "boolean");
        table.addColumn(RealmFieldType.DATE, "date");
        table.addColumn(RealmFieldType.BINARY, "binary");


        byte[] data = new byte[2];

        table.add("abc", 3, (float) 1.2, 1.3, true, new Date(0), data);


        UncheckedRow row = table.getUncheckedRow(0);

        assertEquals("abc", row.getString(0));
        assertEquals(3, row.getLong(1));
        assertEquals(1.2F, row.getFloat(2), Float.MIN_NORMAL);
        assertEquals(1.3, row.getDouble(3), Double.MIN_NORMAL);
        assertEquals(true, row.getBoolean(4));
        assertEquals(new Date(0), row.getDate(5));
        MoreAsserts.assertEquals(data, row.getBinaryByteArray(6));


        row.setString(0, "a");
        row.setLong(1, 1);
        row.setFloat(2, (float) 8.8);
        row.setDouble(3, 9.9);
        row.setBoolean(4, false);
        row.setDate(5, new Date(10000));

        byte[] newData = new byte[3];
        row.setBinaryByteArray(6, newData);

        assertEquals("a", row.getString(0));
        assertEquals(1, row.getLong(1));
        assertEquals(8.8F, row.getFloat(2), Float.MIN_NORMAL);
        assertEquals(9.9, row.getDouble(3), Double.MIN_NORMAL);
        assertEquals(false, row.getBoolean(4));
        assertEquals(new Date(10000), row.getDate(5));
        MoreAsserts.assertEquals(newData, row.getBinaryByteArray(6));
    }

    @Test
    public void nullValues() {

        Table table = new Table();

        long colStringIndex = table.addColumn(RealmFieldType.STRING, "string", true);
        long colIntIndex = table.addColumn(RealmFieldType.INTEGER, "integer", true);
        table.addColumn(RealmFieldType.FLOAT, "float");
        table.addColumn(RealmFieldType.DOUBLE, "double");
        long colBoolIndex = table.addColumn(RealmFieldType.BOOLEAN, "boolean", true);
        table.addColumn(RealmFieldType.DATE, "date");
        table.addColumn(RealmFieldType.BINARY, "binary");

        long rowIndex = table.addEmptyRow();
        UncheckedRow row = table.getUncheckedRow(rowIndex);

        row.setString(colStringIndex, "test");
        assertEquals(row.getString(colStringIndex), "test");
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
