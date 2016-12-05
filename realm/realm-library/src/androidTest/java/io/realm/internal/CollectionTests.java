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


import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class CollectionTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private SharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfiguration();
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        table = sharedRealm.getTable("test_table");
        populateData(table);
    }

    @After
    public void tearDown() {
        sharedRealm.cancelTransaction();
        sharedRealm.close();
    }

    private void populateData(Table table) {
        // Specify the column types and names
        table.addColumn(RealmFieldType.STRING, "firstName");
        table.addColumn(RealmFieldType.STRING, "lastName");
        table.addColumn(RealmFieldType.INTEGER, "age");

        // Add data to the table
        long row = table.addEmptyRow();
        table.setString(0, row, "John", false);
        table.setString(1, row, "Lee", false);
        table.setLong(2, row, 4, false);

        row = table.addEmptyRow();
        table.setString(0, row, "John", false);
        table.setString(1, row, "Anderson", false);
        table.setLong(2, row, 3, false);

        row = table.addEmptyRow();
        table.setString(0, row, "Erik", false);
        table.setString(1, row, "Lee", false);
        table.setLong(2, row, 1, false);

        row = table.addEmptyRow();
        table.setString(0, row, "Henry", false);
        table.setString(1, row, "Anderson", false);
        table.setLong(2, row, 1, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void constructor_queryIsValidated() {
        // Collection's constructor should call TableQuery.validateQuery()
        new Collection(sharedRealm, table.where().or());
    }

    @Test
    public void size() {
        Collection collection = new Collection(sharedRealm, table.where());
        assertEquals(3, collection.size());
    }

    @Test
    public void where() {
        Collection collection = new Collection(sharedRealm, table.where());
        Collection collection2 =new Collection(sharedRealm, collection.where().equalTo(new long[]{0}, "John"));
        Collection collection3 =new Collection(sharedRealm, collection2.where().equalTo(new long[]{1}, "Anderson"));

        // A new native Results should be created.
        assertTrue(collection.getNativePtr() != collection2.getNativePtr());
        assertTrue(collection2.getNativePtr() != collection3.getNativePtr());

        assertEquals(4, collection.size());
        assertEquals(2, collection2.size());
        assertEquals(1, collection3.size());
    }

    @Test
    public void sort() {
        Collection collection = new Collection(sharedRealm, table.where());
        SortDescriptor sortDescriptor = new SortDescriptor(table, new long[] {2});
        try {
            Collection collection2 =collection.sort(sortDescriptor);

            // A new native Results should be created.
            assertTrue(collection.getNativePtr() != collection2.getNativePtr());
            assertEquals(4, collection.size());
            assertEquals(4, collection2.size());

            assertEquals(collection2.getUncheckedRow(0).getLong(2), 1);
            assertEquals(collection2.getUncheckedRow(3).getLong(2), 4);
        } finally {
            sortDescriptor.close();
        }
    }

    @Test
    public void clear() {
        assertEquals(table.size(), 4);
        Collection collection = new Collection(sharedRealm, table.where());
        collection.clear();
        assertEquals(table.size(), 0);
    }

    @Test
    public void contains() {
        Collection collection = new Collection(sharedRealm, table.where());
        UncheckedRow row = table.getUncheckedRow(0);
        assertTrue(collection.contains(row));
    }

    @Test
    public void indexOf() {
        SortDescriptor sortDescriptor = new SortDescriptor(table, new long[] {2});
        try {
            Collection collection = new Collection(sharedRealm, table.where(), sortDescriptor);
            UncheckedRow row = table.getUncheckedRow(0);
            assertEquals(collection.indexOf(row), 3);
        } finally {
            sortDescriptor.close();
        }
    }

    @Test
    public void indexOf_long() {
        SortDescriptor sortDescriptor = new SortDescriptor(table, new long[] {2});
        try {
            Collection collection = new Collection(sharedRealm, table.where(), sortDescriptor);
            assertEquals(collection.indexOf(0), 3);
        } finally {
            sortDescriptor.close();
        }
    }

    @Test
    public void distinct() {
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(table, "firstName");
        Collection collection = new Collection(sharedRealm, table.where(), null, distinctDescriptor);

        assertEquals(collection.size(), 3);
        assertEquals(collection.getUncheckedRow(0).getString(0), "John");
        assertEquals(collection.getUncheckedRow(1).getString(0), "Erik");
        assertEquals(collection.getUncheckedRow(2).getString(0), "Henry");
    }
}
