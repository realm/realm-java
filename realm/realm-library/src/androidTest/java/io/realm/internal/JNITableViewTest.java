/*
 * Copyright 2016 Realm Inc.
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmFieldType;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class JNITableViewTest {
    static {
        Realm.init(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private static final String TABLE_NAME = Table.TABLE_PREFIX + "JNITableViewTest";
    private static final int ROW_COUNT = 10;

    private static final List<RealmFieldType> FIELDS = Arrays.asList(
            RealmFieldType.INTEGER,
            RealmFieldType.BOOLEAN,
            RealmFieldType.STRING,
            RealmFieldType.BINARY,
            RealmFieldType.DATE,
            RealmFieldType.FLOAT,
            RealmFieldType.DOUBLE);
    private static final long INTEGER_COLUMN_INDEX = 0;
    private static final long STRING_COLUMN_INDEX = 2;

    private SharedRealm sharedRealm;

    private Table table;

    @Before
    public void setUp() {
        sharedRealm = SharedRealm.getInstance(configFactory.createConfiguration());
        sharedRealm.beginTransaction();
        try {
            table = sharedRealm.getTable(TABLE_NAME);

            for (RealmFieldType field : FIELDS) {
                final long index = table.addColumn(field, field.name().toLowerCase(Locale.ENGLISH) + "Column");
                table.convertColumnToNullable(index);
            }

            for (int i = 0; i < ROW_COUNT; i++) {
                table.add(i, true, "abcd", new byte[]{123, -123}, new Date(12345), 1.234f, 3.446d);
            }
        } finally {
            sharedRealm.commitTransaction();
        }
    }

    @Test
    public void setNull() {
        TableQuery query = table.where();
        for (int i = 0; i < ROW_COUNT; i++) {
            if (isOdd(i)) {
                query = query.or().equalTo(new long[]{INTEGER_COLUMN_INDEX}, (long) i);
            }
        }
        final TableView oddRows = query.findAll();

        sharedRealm.beginTransaction();
        for (int i = 0; i < oddRows.size(); i++) {
            oddRows.setNull(STRING_COLUMN_INDEX, i, false);
        }
        sharedRealm.commitTransaction();

        // check if TableView#setNull() worked as expected
        for (int i = 0; i < table.size(); i++) {
            assertEquals("index: " + i, isOdd(i), table.isNull(STRING_COLUMN_INDEX, i));
        }
    }

    @Test
    public void isNull() {

        sharedRealm.beginTransaction();
        for (int i = 0; i < table.size(); i++) {
            if (isOdd(i)) {
                table.setNull(STRING_COLUMN_INDEX, i, false);
            }
        }
        sharedRealm.commitTransaction();

        TableQuery query = table.where();
        for (int i = 0; i < ROW_COUNT; i++) {
            if (isOdd(i)) {
                query = query.or().equalTo(new long[]{INTEGER_COLUMN_INDEX}, (long) i);
            }
        }
        final TableView oddRows = query.findAll();
        for (int i = 0; i < oddRows.size(); i++) {
            assertEquals("index: " + i, true, oddRows.isNull(STRING_COLUMN_INDEX, i));
        }

        query = table.where();
        for (int i = 0; i < ROW_COUNT; i++) {
            if (isEven(i)) {
                query = query.or().equalTo(new long[]{INTEGER_COLUMN_INDEX}, (long) i);
            }
        }
        final TableView evenRows = query.findAll();
        for (int i = 0; i < evenRows.size(); i++) {
            assertEquals("index: " + i, false, evenRows.isNull(STRING_COLUMN_INDEX, i));
        }
    }

    private static boolean isEven(int i) {
        return i % 2 == 0;
    }
    private static boolean isOdd(int i) {
        return i % 2 == 1;
    }
}
