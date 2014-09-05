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

package io.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import io.realm.test.TestEmployeeQuery;
import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeView;

public abstract class AbstractNavigationTest {

    protected abstract AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getTableOrView();

    @Test
    public void shouldNavigateToFirstRecord() {
        TestEmployeeRow first = getTableOrView().first();

        assertEquals(0, first.getPosition());
    }

    @Test
    public void shouldNavigateToLastRecord() {
        TestEmployeeRow last = getTableOrView().last();

        assertEquals(getTableOrView().size() - 1, last.getPosition());
    }

    @Test
    public void shouldNavigateToNextRecord() {
        TestEmployeeRow e = getTableOrView().get(0).next();

        assertEquals(1, e.getPosition());
    }

    @Test
    public void shouldNavigateToPreviousRecord() {
        TestEmployeeRow e = getTableOrView().get(1).previous();

        assertEquals(0, e.getPosition());
    }

    @Test
    public void shouldNavigateAfterSpecifiedRecords() {
        TestEmployeeRow e = getTableOrView().get(0).after(2);

        assertEquals(2, e.getPosition());
    }

    @Test
    public void shouldNavigateBeforeSpecifiedRecords() {
        TestEmployeeRow e = getTableOrView().get(2).before(2);

        assertEquals(0, e.getPosition());
    }

    @Test
    public void shouldReturnNullOnInvalidPosition() {
        assertNull(getTableOrView().get(0).previous());
        assertNull(getTableOrView().last().next());
        assertNull(getTableOrView().get(1).before(2));
        assertNull(getTableOrView().get(2).after(1000));
    }

}
