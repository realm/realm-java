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

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;


@RunWith(AndroidJUnit4.class)
public class JNIColumnInfoTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private OsSharedRealm sharedRealm;
    private Table table;

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        RealmConfiguration config = configFactory.createConfiguration();
        sharedRealm = OsSharedRealm.getInstance(config, OsSharedRealm.VersionID.LIVE);

        table = TestHelper.createTable(sharedRealm, "temp", new TestHelper.AdditionalTableSetup() {
            @Override
            public void execute(Table table) {
                table.addColumn(RealmFieldType.STRING, "firstName");
                table.addColumn(RealmFieldType.STRING, "lastName");
            }
        });
    }

    @After
    public void tearDown() {
        if (sharedRealm != null) {
            sharedRealm.close();
        }
    }

    @Test
    public void shouldGetColumnInformation() {
        assertEquals(2, table.getColumnCount());

        long columnKey = table.getColumnKey("lastName");
        assertNotSame(Table.NO_MATCH, columnKey);

        assertEquals(RealmFieldType.STRING, table.getColumnType(columnKey));
    }

}
