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

import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmFieldType;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class JNISortedLongTest {
    Table table;

    void init() {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        table = new Table();
        table.addColumn(RealmFieldType.INTEGER, "number");
        table.addColumn(RealmFieldType.STRING, "name");

        table.add(1, "A");
        table.add(10, "B");
        table.add(20, "C");
        table.add(30, "B");
        table.add(40, "D");
        table.add(50, "D");
        table.add(60, "D");
        table.add(60, "D");

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
