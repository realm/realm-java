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

// TODO: Check that Index can be set on multiple columns.

import junit.framework.TestCase;

import io.realm.RealmFieldType;

@SuppressWarnings("unused")
public class JNIDistinctTest extends TestCase {
    Table table;

    void init() {
        table = new Table();
        table.addColumn(RealmFieldType.INTEGER, "number");
        table.addColumn(RealmFieldType.STRING, "name");

        long i = 0;
        table.add(0, "A");
        table.add(1, "B");
        table.add(2, "C");
        table.add(3, "B");
        table.add(4, "D");
        table.add(5, "D");
        table.add(6, "D");
        assertEquals(7, table.size());
    }

    public void testShouldTestDistinct() {
        init();

        // Must set index before using distinct()
        table.addSearchIndex(1);
        assertEquals(true, table.hasSearchIndex(1));

        TableView view = table.getDistinctView(1);
        assertEquals(4, view.size());
        assertEquals(0, view.getLong(0, 0));
        assertEquals(1, view.getLong(0, 1));
        assertEquals(2, view.getLong(0, 2));
        assertEquals(4, view.getLong(0, 3));
    }

    public void testShouldTestDistinctErrorWhenNoIndex() {
        init();
        try {
            TableView view = table.getDistinctView(1);
            fail();
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }
    }

    public void testShouldTestDistinctErrorWhenIndexOutOfBounds() {
        init();
        try {
            TableView view = table.getDistinctView(3);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    public void testShouldTestDistinctErrorWhenWrongColumnType() {
        init();
        table.addSearchIndex(1);
        try {
            TableView view = table.getDistinctView(0);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

}
