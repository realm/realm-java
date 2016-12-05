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

import junit.framework.TestCase;

import io.realm.Realm;
import io.realm.RealmFieldType;

public class JNIColumnInfoTest extends TestCase {

    Table table;

    @Override
    public void setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        table = new Table();
        table.addColumn(RealmFieldType.STRING, "firstName");
        table.addColumn(RealmFieldType.STRING, "lastName");
    }

    public void testShouldGetColumnInformation() {

        assertEquals(2, table.getColumnCount());

        assertEquals("lastName", table.getColumnName(1));

        assertEquals(1, table.getColumnIndex("lastName"));

        assertEquals(RealmFieldType.STRING, table.getColumnType(1));

    }

    public void testValidateColumnInfo() {

        assertEquals(2, table.getColumnCount());

        assertEquals("lastName", table.getColumnName(1));

        assertEquals(1, table.getColumnIndex("lastName"));

        assertEquals(RealmFieldType.STRING, table.getColumnType(1));

    }

}
