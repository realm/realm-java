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

import io.realm.Table;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.test.TestNumbersTable;
import io.realm.test.TestNumbersView;

public class NumbersTest {

    private TestNumbersTable numbers;
    private TestNumbersView view;

    @BeforeMethod
    public void init() {
        numbers = new TestNumbersTable();

        numbers.add(10000, 10000.1f, 10000.1d);
        numbers.add(10000, 10000.1f, 10000.1d);
        numbers.insert(1, 30000, 30000.6f, 30000.6d);

        assertEquals(3, numbers.size());

        view = numbers.where().findAll();

        assertEquals(3, view.size());

    }

    @Test
    public void shouldMatchFloats() {
        assertEquals(1, numbers.floatNum.equalTo(30000.6f).findAll().size());

        assertEquals(2, numbers.floatNum.notEqualTo(30000.6f).findAll().size());

        assertEquals(2, numbers.floatNum.lessThan(30000.6f).findAll().size());

        assertEquals(3, numbers.floatNum.lessThanOrEqual(30000.6f).findAll().size());

        assertEquals(3, numbers.floatNum.greaterThan(5000).findAll().size());

        assertEquals(3, numbers.floatNum.greaterThanOrEqual(10000.1f).findAll().size());

        assertEquals(2, numbers.floatNum.between(5000, 15000).findAll().size());
    }

    @Test
    public void shouldMatchDoubles() {
        assertEquals(1, numbers.doubleNum.equalTo(30000.6).findAll().size());

        assertEquals(2, numbers.doubleNum.notEqualTo(30000.6).findAll().size());

        assertEquals(2, numbers.doubleNum.lessThan(30000.6).findAll().size());

        assertEquals(3, numbers.doubleNum.lessThanOrEqual(30000.6).findAll().size());

        assertEquals(3, numbers.doubleNum.greaterThan(5000).findAll().size());

        assertEquals(3, numbers.doubleNum.greaterThanOrEqual(10000.1).findAll().size());

        assertEquals(2, numbers.doubleNum.between(5000, 15000).findAll().size());
    }

    @Test
    public void shouldAggregateFloats() {
        assertEquals(10000.1f, numbers.floatNum.minimum());
        assertEquals(10000.1f, numbers.floatNum.minimum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6f, numbers.floatNum.minimum(1, 2, Table.INFINITE)); // second
        assertEquals(10000.1f, numbers.floatNum.minimum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(30000.6f, numbers.floatNum.maximum());
        assertEquals(10000.1f, numbers.floatNum.maximum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6f, numbers.floatNum.maximum(1, 2, Table.INFINITE)); // second
        assertEquals(30000.6f, numbers.floatNum.maximum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(50000.8d, numbers.floatNum.sum(), 0.01);
        assertEquals(10000.1d, numbers.floatNum.sum(0, 1, Table.INFINITE), 0.01); // first
        assertEquals(30000.6d, numbers.floatNum.sum(1, 2, Table.INFINITE), 0.01); // second
        assertEquals(40000.7d, numbers.floatNum.sum(0, 2, Table.INFINITE), 0.01); // 1st & 2nd

        assertEquals(50000.8d/3, numbers.floatNum.average(), 0.01);
        assertEquals(30000.6d, numbers.floatNum.average(1, 2, Table.INFINITE), 0.01); // second
        assertEquals(40000.7d/2, numbers.floatNum.average(0, 2, Table.INFINITE), 0.01); // 1st & 2nd
        assertEquals(10000.1d, numbers.floatNum.average(0, 1, Table.INFINITE), 0.01); // first
    }

    @Test
    public void shouldAggregateDoubles() {
        assertEquals(10000.1d, numbers.doubleNum.minimum());
        assertEquals(10000.1d, numbers.doubleNum.minimum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6d, numbers.doubleNum.minimum(1, 2, Table.INFINITE)); // second
        assertEquals(10000.1d, numbers.doubleNum.minimum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(30000.6d, numbers.doubleNum.maximum());
        assertEquals(10000.1d, numbers.doubleNum.maximum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6d, numbers.doubleNum.maximum(1, 2, Table.INFINITE)); // second
        assertEquals(30000.6d, numbers.doubleNum.maximum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(50000.8d, numbers.doubleNum.sum(), 0.01);
        assertEquals(10000.1d, numbers.doubleNum.sum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6d, numbers.doubleNum.sum(1, 2, Table.INFINITE)); // second
        assertEquals(40000.7d, numbers.doubleNum.sum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(50000.8d/3, numbers.doubleNum.average(), 0.01);
        assertEquals(30000.6d, numbers.doubleNum.average(1, 2, Table.INFINITE)); // second
        assertEquals(40000.7d/2, numbers.doubleNum.average(0, 2, Table.INFINITE)); // 1st & 2nd
        assertEquals(10000.1d, numbers.doubleNum.average(0, 1, Table.INFINITE)); // first
    }


    @Test
    public void shouldAggregateIntegersOnView() {
        assertEquals(10000, view.longNum.minimum());
        assertEquals(30000, view.longNum.maximum());
        assertEquals(50000, view.longNum.sum());
        assertEquals(50000d / 3, view.longNum.average());
    }


    @Test
    public void shouldAggregateFloatsOnView() {
        assertEquals(10000.1f, view.floatNum.minimum());
        assertEquals(30000.6f, view.floatNum.maximum());
        assertEquals(50000.8d, view.floatNum.sum(), 0.01);
        assertEquals(50000.8d/3, view.floatNum.average(), 0.01);
    }

    @Test
    public void shouldAggregateDoublesOnView() {
        assertEquals(10000.1d, view.doubleNum.minimum());
        assertEquals(30000.6d, view.doubleNum.maximum());
        assertEquals(50000.8d, view.doubleNum.sum(), 0.01);
        assertEquals(50000.8d/3, view.doubleNum.average(), 0.01);
    }


    @Test
    public void searchValuesOnView() {
        assertEquals(2 , view.doubleNum.findAll(10000.1d).size() );
        assertEquals(2 , view.floatNum.findAll(10000.1f).size() );

        assertEquals(1 , view.doubleNum.findFirst(30000.6d).getPosition());
        assertEquals(1 , view.floatNum.findFirst(30000.6f).getPosition() );

        // Double get and set all
        {
            Double[] excpected = new Double[(int) 3];
            excpected[0] = 10000.1d;
            excpected[1] = 30000.6d;
            excpected[2] = 10000.1d;

            Double[] actual = view.doubleNum.getAll(); // Get all return a Double

            for (int i=0;i<actual.length;i++){
                assertEquals(excpected[i], actual[i]);
            }

            view.doubleNum.setAll(999d);
            for (long row = 0;row<view.size();row++){
                assertEquals(999d, view.get(row).getDoubleNum());
            }
        }

        // Float get and set all
        {
            Float[] excpected = new Float[(int) 3];
            excpected[0] = 10000.1f;
            excpected[1] = 30000.6f;
            excpected[2] = 10000.1f;

            Float[] actual = view.floatNum.getAll(); // Get all return a Float

            for (int i=0;i<actual.length;i++){
                assertEquals(excpected[i], actual[i]);
            }

            view.floatNum.setAll(999f);
            for (long row = 0;row<view.size();row++){
                assertEquals(999f, view.get(row).getFloatNum());
            }
        }
    }


    @Test
    public void viewShouldAggregatesLong() {
        assertEquals(50000d / 3, view.longNum.average());
        assertEquals(50000.8d / 3, view.doubleNum.average(), 0.000001);
        assertEquals(50000.8d / 3, view.floatNum.average(), 0.01);
    }

    @Test
    public void setAndGetNumbers() {

        // Integer set all columns
        view.longNum.setAll(400);
        assertEquals(400, view.get(0).getLongNum());
        view.longNum.setAll(-1);
        assertEquals(-1, view.get(1).getLongNum());

        // Double columns
        view.get(0).setDoubleNum(400d);
        assertEquals(400d, view.get(0).getDoubleNum());
        view.get(1).setDoubleNum(-0.01d);
        assertEquals(-0.01d, view.get(1).getDoubleNum());

        // FLoat columns
        view.get(0).setFloatNum(400f);
        assertEquals(400f, view.get(0).getFloatNum());
        view.get(1).setFloatNum(-0.01f);
        assertEquals(-0.01f, view.get(1).getFloatNum());
    }
}
