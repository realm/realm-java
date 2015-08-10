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
        value.add(6, new byte[]{1, 2, 3, 4, 5});
        return Arrays.asList(
                new Object[]{value},
                new Object[]{value}
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

    public void testShouldThrowExceptionWhenColumnNameIsTooLong() {

        Table table = new Table();
        try {
            table.addColumn(ColumnType.STRING, "THIS STRING HAS 64 CHARACTERS, "
                    + "LONGER THAN THE MAX 63 CHARACTERS");
            fail("Too long name");
        } catch (IllegalArgumentException e) {
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

