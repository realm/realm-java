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

import java.util.HashSet;
import java.util.Set;

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

    private OsSharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
        table = sharedRealm.createTable("test_table");
    }

    @After
    public void tearDown() {
        sharedRealm.close();
    }

    @Test
    public void getInstanceForDistinct() {
        for (RealmFieldType type : SortDescriptor.DISTINCT_VALID_FIELD_TYPES) {
            long column = table.addColumn(type, type.name());
            table.addSearchIndex(column);
        }

        long i = 0;
        for (RealmFieldType type : SortDescriptor.DISTINCT_VALID_FIELD_TYPES) {
            SortDescriptor sortDescriptor = SortDescriptor.getInstanceForDistinct(null, table, type.name());
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
            SortDescriptor.getInstanceForDistinct(null, table, String.format("%s.%s", listType.name(), type.name()));
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            SortDescriptor.getInstanceForDistinct(null, table, String.format("%s.%s", objectType.name(), type.name()));
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

        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForDistinct(null, table, new String[] {
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
        Set<RealmFieldType> types = getValidFieldTypes(SortDescriptor.DISTINCT_VALID_FIELD_TYPES);

        for (RealmFieldType type : types) {
            try {
                SortDescriptor.getInstanceForDistinct(null, table, type.name());
                fail();
            } catch (IllegalArgumentException ignored) {
                assertTrue(ignored.getMessage().contains("Distinct is not supported"));
            }
        }
    }

    @Test
    public void getInstanceForSort() {
        for (RealmFieldType type : SortDescriptor.SORT_VALID_FIELD_TYPES) {
            table.addColumn(type, type.name());
        }

        long i = 0;
        for (RealmFieldType type : SortDescriptor.SORT_VALID_FIELD_TYPES) {
            SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(null, table, type.name(), Sort.DESCENDING);
            assertEquals(1, sortDescriptor.getColumnIndices()[0].length);
            assertEquals(i, sortDescriptor.getColumnIndices()[0][0]);
            assertFalse(sortDescriptor.getAscendings()[0]);
            i++;
        }
    }

    @Test
    public void getInstanceForSort_linkField() {
        for (RealmFieldType type : SortDescriptor.DISTINCT_VALID_FIELD_TYPES) {
            long column = table.addColumn(type, type.name());
            table.addSearchIndex(column);
        }
        RealmFieldType objectType = RealmFieldType.OBJECT;
        long columnLink = table.addColumnLink(objectType, objectType.name(), table);

        long i = 0;
        for (RealmFieldType type : SortDescriptor.DISTINCT_VALID_FIELD_TYPES) {
            SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(null, table,
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

        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(null, table, new String[] {
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
        SortDescriptor.getInstanceForSort(null, table,
                new String[] {stringType.name(), intType.name()}, new Sort[] {Sort.ASCENDING});

    }

    @Test
    public void getInstanceForSort_shouldThrowOnInvalidField() {
        Set<RealmFieldType> types = getValidFieldTypes(SortDescriptor.SORT_VALID_FIELD_TYPES);

        for (RealmFieldType type : types) {
            try {
                SortDescriptor.getInstanceForSort(null, table, type.name(), Sort.ASCENDING);
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
        thrown.expectMessage("Invalid query: field 'LIST' in class 'test_table' is of invalid type 'LIST'.");
        SortDescriptor.getInstanceForSort(null, table, String.format("%s.%s", listType.name(), type.name()), Sort.ASCENDING);
    }

    private Set<RealmFieldType> getValidFieldTypes(Set<RealmFieldType> filter) {
        Set<RealmFieldType> types = new HashSet<>();
        for (RealmFieldType type : RealmFieldType.values()) {
            if (!filter.contains(type)) {
                switch (type) {
                    case LINKING_OBJECTS: // TODO: should be supported?s
                    case INTEGER_LIST: // FIXME zaki50 revisit this once Primitive List query is implemented
                    case BOOLEAN_LIST:
                    case STRING_LIST:
                    case BINARY_LIST:
                    case DATE_LIST:
                    case FLOAT_LIST:
                    case DOUBLE_LIST:
                        break;
                    case LIST:
                    case OBJECT:
                        table.addColumnLink(type, type.name(), table);
                        types.add(type);
                        break;
                    default:
                        table.addColumn(type, type.name());
                        types.add(type);
                }
            }
        }
        return types;
    }
}
