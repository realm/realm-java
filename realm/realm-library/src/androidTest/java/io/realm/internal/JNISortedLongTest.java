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
public class JNISortedLongTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @SuppressWarnings("FieldCanBeLocal")
    private RealmConfiguration config;
    private SharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() throws Exception {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        config = configFactory.createConfiguration();
        sharedRealm = SharedRealm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (sharedRealm != null && !sharedRealm.isClosed()) {
            sharedRealm.close();
        }
    }

    private void init() {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                table.addColumn(RealmFieldType.INTEGER, "number");
                table.addColumn(RealmFieldType.STRING, "name");

                TestHelper.addRowWithValues(table, 1, "A");
                TestHelper.addRowWithValues(table, 10, "B");
                TestHelper.addRowWithValues(table, 20, "C");
                TestHelper.addRowWithValues(table, 30, "B");
                TestHelper.addRowWithValues(table, 40, "D");
                TestHelper.addRowWithValues(table, 50, "D");
                TestHelper.addRowWithValues(table, 60, "D");
                TestHelper.addRowWithValues(table, 60, "D");
            }
        });

        assertEquals(8, table.size());
    }

    @Test
    public void shouldTestSortedIntTable() {
        init();

        // Before first entry.
        assertEquals(0, table.lowerBoundLong(0, 0));
        assertEquals(0, table.upperBoundLong(0, 0));

        // Finds middle match.
        assertEquals(4, table.lowerBoundLong(0, 40));
        assertEquals(5, table.upperBoundLong(0, 40));

        // Finds middle (nonexisting).
        assertEquals(5, table.lowerBoundLong(0, 41));
        assertEquals(5, table.upperBoundLong(0, 41));

        // Beyond last entry.
        assertEquals(8, table.lowerBoundLong(0, 100));
        assertEquals(8, table.upperBoundLong(0, 100));

        // Finds last match (duplicated).
        assertEquals(6, table.lowerBoundLong(0, 60));
        assertEquals(8, table.upperBoundLong(0, 60));

    }

}
