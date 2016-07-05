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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class JNILinkTest {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private SharedRealm sharedRealm;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfiguration();
        sharedRealm = SharedRealm.getInstance(config);
        sharedRealm.beginTransaction();
    }

    @After
    public void tearDown() {
        sharedRealm.cancelTransaction();
        sharedRealm.close();
    }

    @Test
    public void testLinkColumns() {
        Table table1 = sharedRealm.getTable("table1");

        Table table2 = sharedRealm.getTable("table2");
        table2.addColumn(RealmFieldType.INTEGER, "int");
        table2.addColumn(RealmFieldType.STRING, "string");

        table2.add(1, "c");
        table2.add(2, "b");
        table2.add(3, "a");

        table1.addColumnLink(RealmFieldType.OBJECT, "Link", table2);

        table1.addEmptyRow();
        table1.setLink(0, 0, 1);

        Table target = table1.getLinkTarget(0);

        System.gc();

        assertEquals(target.getColumnCount(), 2);

        String test = target.getString(1, table1.getLink(0, 0));

        assertEquals(test, "b");

    }

    @Test
    public void testLinkList() {
        Table table1 = sharedRealm.getTable("table1");
        table1.addColumn(RealmFieldType.INTEGER, "int");
        table1.addColumn(RealmFieldType.STRING, "string");
        table1.add(1, "c");
        table1.add(2, "b");
        table1.add(3, "a");

        Table table2 = sharedRealm.getTable("table2");

        table2.addColumnLink(RealmFieldType.LIST, "LinkList", table1);

        table2.addEmptyRow();

        LinkView links = table2.getUncheckedRow(0).getLinkList(0);

        assertEquals(links.isEmpty(), true);
        assertEquals(links.size(), 0);

        links.add(2);
        links.add(1);

        assertEquals(links.isEmpty(), false);
        assertEquals(links.size(), 2);

        assertEquals(links.getUncheckedRow(0).getColumnName(1), "string");

        assertEquals(links.getUncheckedRow(0).getString(1), "a");

        links.move(1, 0);

        assertEquals(links.getUncheckedRow(0).getString(1), "b");

        links.remove(0);

        assertEquals(links.getUncheckedRow(0).getString(1), "a");
        assertEquals(links.size(), 1);
    }
}
