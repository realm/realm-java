/*
 * Copyright 2014 Realm Inc.
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

package io.realm;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

// TODO: Check that Index can be set on multiple columns.

@SuppressWarnings("unused")
public class JNIDistinctTest {
    Table table;

    void init() {
        table = new Table();
        table.addColumn(ColumnType.INTEGER, "number");
        table.addColumn(ColumnType.STRING, "name");

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

    @Test
    public void shouldTestDistinct() {
        init();

        // Must set index before using distinct()
        table.setIndex(1);
        assertEquals(true, table.hasIndex(1));

        TableView view = table.getDistinctView(1);
        assertEquals(4, view.size());
        assertEquals(0, view.getLong(0, 0));
        assertEquals(1, view.getLong(0, 1));
        assertEquals(2, view.getLong(0, 2));
        assertEquals(4, view.getLong(0, 3));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldTestDistinctErrorWhenNoIndex() {
        init();
        TableView view = table.getDistinctView(1);
    }

    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void shouldTestDistinctErrorWhenIndexOutOfBounds() {
        init();

        TableView view = table.getDistinctView(3);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldTestDistinctErrorWhenWrongColumnType() {
        init();
        table.setIndex(1);
        TableView view = table.getDistinctView(0);
    }

}
