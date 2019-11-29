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

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class TableIndexAndDistinctTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @SuppressWarnings("FieldCanBeLocal")
    private RealmConfiguration config;
    private OsSharedRealm sharedRealm;
    private Table table;

    private long colKey1;
    private long colKey2;

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

    private void init() {
        table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                colKey1 = table.addColumn(RealmFieldType.INTEGER, "number");
                colKey2 = table.addColumn(RealmFieldType.STRING, "name");

                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{0, "A"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{1, "B"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{2, "C"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{3, "B"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{4, "D"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{5, "D"});
                TestHelper.addRowWithValues(table, new long[]{colKey1, colKey2}, new Object[]{6, "D"});
            }
        });

        assertEquals(7, table.size());
    }

    /**
     * Checks that Index can be set on multiple columns, with the String.
     * @param
     */
    @Test
    public void shouldTestSettingIndexOnMultipleColumns() {
        long[] columnsKey = new long[5];
        // Creates a table only with String type columns
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                columnsKey[0] = t.addColumn(RealmFieldType.STRING, "col1");
                columnsKey[1] = t.addColumn(RealmFieldType.STRING, "col2");
                columnsKey[2] = t.addColumn(RealmFieldType.STRING, "col3");
                columnsKey[3] = t.addColumn(RealmFieldType.STRING, "col4");
                columnsKey[4] = t.addColumn(RealmFieldType.STRING, "col5");

                TestHelper.addRowWithValues(t, columnsKey, new Object[]{"row1", "row2", "row3", "row4", "row5"});
                TestHelper.addRowWithValues(t, columnsKey, new Object[]{"row1", "row2", "row3", "row4", "row5"});
                TestHelper.addRowWithValues(t, columnsKey, new Object[]{"row1", "row2", "row3", "row4", "row5"});
                TestHelper.addRowWithValues(t, columnsKey, new Object[]{"row1", "row2", "row3", "row4", "row5"});
                TestHelper.addRowWithValues(t, columnsKey, new Object[]{"row1", "row2", "row3", "row4", "row5"});
            }
        });

        for (int i = 0; i < columnsKey.length; i++) {
            t.addSearchIndex(columnsKey[i]);
            assertEquals(true, t.hasSearchIndex(columnsKey[i]));
        }

    }


// TODO: parametric test
/*    *//**
     * Checks that all other column types than String throws exception.
     *//*

    @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "columnIndex")
    public void shouldTestIndexOnWrongColumnType(Long index) {

        // Gets a table with all available column types.
        Table t = TestHelper.createTableWithAllColumnTypes(sharedRealm);

        // If column type is String, then throw the excepted exception.
        if (t.getColumnType(index).equals(RealmFieldType.STRING)){
            throw new IllegalArgumentException();
        }

        t.addSearchIndex(index);
    }*/

    @Test
    public void shouldCheckIndexIsOkOnColumn() {
        init();
        table.addSearchIndex(colKey1);
    }

    @Test
    public void removeSearchIndex() {
        init();
        table.addSearchIndex(colKey1);
        assertEquals(true, table.hasSearchIndex(colKey1));

        table.removeSearchIndex(colKey1);
        assertEquals(false, table.hasSearchIndex(colKey1));
    }

    @Test
    public void removeSearchIndexNoOp() {
        init();
        assertEquals(false, table.hasSearchIndex(colKey1));

        // Removes index from non-indexed column is a no-op.
        table.removeSearchIndex(colKey1);
        assertEquals(false, table.hasSearchIndex(colKey1));
    }
}

