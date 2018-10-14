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

    @Before
    public void setUp() throws Exception {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config);

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
                table.addColumn(RealmFieldType.INTEGER, "number");
                table.addColumn(RealmFieldType.STRING, "name");

                TestHelper.addRowWithValues(table, 0, "A");
                TestHelper.addRowWithValues(table, 1, "B");
                TestHelper.addRowWithValues(table, 2, "C");
                TestHelper.addRowWithValues(table, 3, "B");
                TestHelper.addRowWithValues(table, 4, "D");
                TestHelper.addRowWithValues(table, 5, "D");
                TestHelper.addRowWithValues(table, 6, "D");
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

        // Creates a table only with String type columns
        Table t = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table t) {
                t.addColumn(RealmFieldType.STRING, "col1");
                t.addColumn(RealmFieldType.STRING, "col2");
                t.addColumn(RealmFieldType.STRING, "col3");
                t.addColumn(RealmFieldType.STRING, "col4");
                t.addColumn(RealmFieldType.STRING, "col5");
                TestHelper.addRowWithValues(t, "row1", "row2", "row3", "row4", "row5");
                TestHelper.addRowWithValues(t, "row1", "row2", "row3", "row4", "row5");
                TestHelper.addRowWithValues(t, "row1", "row2", "row3", "row4", "row5");
                TestHelper.addRowWithValues(t, "row1", "row2", "row3", "row4", "row5");
                TestHelper.addRowWithValues(t, "row1", "row2", "row3", "row4", "row5");
            }
        });

        for (long c=0;c<t.getColumnCount();c++){
            t.addSearchIndex(c);
            assertEquals(true, t.hasSearchIndex(c));
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
        table.addSearchIndex(1);
    }

    @Test
    public void removeSearchIndex() {
        init();
        table.addSearchIndex(1);
        assertEquals(true, table.hasSearchIndex(1));

        table.removeSearchIndex(1);
        assertEquals(false, table.hasSearchIndex(1));
    }

    @Test
    public void removeSearchIndexNoOp() {
        init();
        assertEquals(false, table.hasSearchIndex(1));

        // Removes index from non-indexed column is a no-op.
        table.removeSearchIndex(1);
        assertEquals(false, table.hasSearchIndex(1));
    }
}

