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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.Sort;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SortDescriptorTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private SharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfiguration();
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        table = sharedRealm.getTable("test_table");
    }

    @After
    public void tearDown() {
        sharedRealm.close();
    }

    @Test
    public void getInstanceForDistinct() {
        for (RealmFieldType type : SortDescriptor.validFieldTypesForDistinct) {
            long column = table.addColumn(type, type.name());
            table.addSearchIndex(column);
        }

        long i = 0;
        for (RealmFieldType type : SortDescriptor.validFieldTypesForDistinct) {
            SortDescriptor sortDescriptor = SortDescriptor.getInstanceForDistinct(table, type.name());
            assertEquals(1, sortDescriptor.getColumnIndices()[0].length);
            assertEquals(i, sortDescriptor.getColumnIndices()[0][0]);
            assertNull(sortDescriptor.getAscendings());
            i++;
        }
    }

    @Test
    public void getInstanceForDistinct_shouldThrowOnLinkAndListListField() {
        RealmFieldType type = RealmFieldType.STRING;
        RealmFieldType objectType = RealmFieldType.OBJECT;
        RealmFieldType listType = RealmFieldType.LIST;
        table.addColumn(type, type.name());
        table.addColumnLink(objectType, objectType.name(), table);
        table.addColumnLink(listType, listType.name(), table);

        try {
            SortDescriptor.getInstanceForDistinct(table, String.format("%s.%s", listType.name(), type.name()));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            SortDescriptor.getInstanceForDistinct(table, String.format("%s.%s", objectType.name(), type.name()));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getInstanceForDistinct_multipleFields() {
        RealmFieldType stringType = RealmFieldType.STRING;
        long stringColumn = table.addColumn(stringType, stringType.name());
        table.addSearchIndex(stringColumn);
        RealmFieldType intType = RealmFieldType.INTEGER;
        long intColumn = table.addColumn(intType, intType.name());
        table.addSearchIndex(intColumn);

        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForDistinct(table, new String[] {
               stringType.name(), intType.name()});
        assertEquals(2, sortDescriptor.getColumnIndices().length);
        assertNull(sortDescriptor.getAscendings());
        assertEquals(1, sortDescriptor.getColumnIndices()[0].length);
        assertEquals(stringColumn, sortDescriptor.getColumnIndices()[0][0]);
        assertEquals(1, sortDescriptor.getColumnIndices()[1].length);
        assertEquals(intColumn, sortDescriptor.getColumnIndices()[1][0]);
    }

    @Test
    public void getInstanceForDistinct_shouldThrowOnInvalidField() {
        List<RealmFieldType> types = new ArrayList<RealmFieldType>();
        for (RealmFieldType type : RealmFieldType.values()) {
            if (!SortDescriptor.validFieldTypesForDistinct.contains(type) &&
                    type != RealmFieldType.UNSUPPORTED_DATE &&
                    type != RealmFieldType.UNSUPPORTED_TABLE &&
                    type != RealmFieldType.UNSUPPORTED_MIXED) {
                if (type == RealmFieldType.LIST || type == RealmFieldType.OBJECT) {
                    table.addColumnLink(type, type.name(), table);
                } else {
                    table.addColumn(type, type.name());
                }
                types.add(type);
            }
        }

        for (RealmFieldType type : types) {
            try {
                SortDescriptor.getInstanceForDistinct(table, type.name());
                fail();
            } catch (IllegalArgumentException ignored) {
                assertTrue(ignored.getMessage().contains("Distinct is not supported"));
            }
        }
    }

    @Test
    public void getInstanceForSort() {
        for (RealmFieldType type : SortDescriptor.validFieldTypesForSort) {
            table.addColumn(type, type.name());
        }

        long i = 0;
        for (RealmFieldType type : SortDescriptor.validFieldTypesForSort) {
            SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(table, type.name(), Sort.DESCENDING);
            assertEquals(1, sortDescriptor.getColumnIndices()[0].length);
            assertEquals(i, sortDescriptor.getColumnIndices()[0][0]);
            assertFalse(sortDescriptor.getAscendings()[0]);
            i++;
        }
    }

    @Test
    public void getInstanceForSort_linkField() {
        for (RealmFieldType type : SortDescriptor.validFieldTypesForDistinct) {
            long column = table.addColumn(type, type.name());
            table.addSearchIndex(column);
        }
        RealmFieldType objectType = RealmFieldType.OBJECT;
        long columnLink = table.addColumnLink(objectType, objectType.name(), table);

        long i = 0;
        for (RealmFieldType type : SortDescriptor.validFieldTypesForDistinct) {
            SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(table,
                    String.format("%s.%s", objectType.name(), type.name()), Sort.ASCENDING);
            assertEquals(2, sortDescriptor.getColumnIndices()[0].length);
            assertEquals(columnLink, sortDescriptor.getColumnIndices()[0][0]);
            assertEquals(i, sortDescriptor.getColumnIndices()[0][1]);
            assertTrue(sortDescriptor.getAscendings()[0]);
            i++;
        }
    }

    @Test
    public void getInstanceForSort_multipleFields() {
        RealmFieldType stringType = RealmFieldType.STRING;
        long stringColumn = table.addColumn(stringType, stringType.name());
        RealmFieldType intType = RealmFieldType.INTEGER;
        long intColumn = table.addColumn(intType, intType.name());

        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(table, new String[] {
                stringType.name(), intType.name()}, new Sort[] {Sort.ASCENDING, Sort.DESCENDING});

        assertEquals(2, sortDescriptor.getAscendings().length);
        assertEquals(2, sortDescriptor.getColumnIndices().length);

        assertEquals(1, sortDescriptor.getColumnIndices()[0].length);
        assertEquals(stringColumn, sortDescriptor.getColumnIndices()[0][0]);
        assertTrue(sortDescriptor.getAscendings()[0]);

        assertEquals(1, sortDescriptor.getColumnIndices()[1].length);
        assertEquals(intColumn, sortDescriptor.getColumnIndices()[1][0]);
        assertFalse(sortDescriptor.getAscendings()[1]);

    }

    @Test
    public void getInstanceForSort_numOfFeildsAndSortOrdersNotMatch() {
        RealmFieldType stringType = RealmFieldType.STRING;
        table.addColumn(stringType, stringType.name());
        RealmFieldType intType = RealmFieldType.INTEGER;
        table.addColumn(intType, intType.name());

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Number of fields and sort orders do not match.");
        SortDescriptor.getInstanceForSort(table,
                new String[] { stringType.name(), intType.name()}, new Sort[] {Sort.ASCENDING});

    }

    @Test
    public void getInstanceForSort_shouldThrowOnInvalidField() {
        List<RealmFieldType> types = new ArrayList<RealmFieldType>();
        for (RealmFieldType type : RealmFieldType.values()) {
            if (!SortDescriptor.validFieldTypesForSort.contains(type) &&
                    type != RealmFieldType.UNSUPPORTED_DATE &&
                    type != RealmFieldType.UNSUPPORTED_TABLE&&
                    type != RealmFieldType.UNSUPPORTED_MIXED) {
                if (type == RealmFieldType.LIST || type == RealmFieldType.OBJECT) {
                    table.addColumnLink(type, type.name(), table);
                } else {
                    table.addColumn(type, type.name());
                }
                types.add(type);
            }
        }

        for (RealmFieldType type : types) {
            try {
                SortDescriptor.getInstanceForSort(table, type.name(), Sort.ASCENDING);
                fail();
            } catch (IllegalArgumentException ignored) {
                assertTrue(ignored.getMessage().contains("Sort is not supported"));
            }
        }
    }

    @Test
    public void getInstanceForSort_shouldThrowOnLinkListField() {
        RealmFieldType type = RealmFieldType.STRING;
        RealmFieldType listType = RealmFieldType.LIST;
        table.addColumn(type, type.name());
        table.addColumnLink(listType, listType.name(), table);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("is not a supported link field");
        SortDescriptor.getInstanceForSort(table, String.format("%s.%s", listType.name(), type.name()), Sort.ASCENDING);
    }
}
