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

public class MixedSubtableTest extends AbstractTest {
    /*
    @Test
    public void shouldStoreSubtableInMixedTypeColumn() {
        TestEmployeeRow employee = employees.get(0);
        TestPhoneTable phones = employee.extra.createSubtable(TestPhoneTable.class);

        phones.add("mobile", "123");
        assertEquals(1, phones.size());

        TestPhoneTable phones2 = employee.extra.getSubtable(TestPhoneTable.class);
        assertEquals(1, phones2.size());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailOnOnWrongSubtableRetrievalFromMixedTypeColumn() {
        TestEmployeeRow employee = employees.get(0);
        TestPhoneTable phones = employee.extra.createSubtable(TestPhoneTable.class);

        phones.add("mobile", "123");
        assertEquals(1, phones.size());

        // should fail - since we try to get the wrong subtable class
        employee.extra.getSubtable(TestEmployeeTable.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailOnOnSubtableRetrtievalFromIncorrectType() {
        TestEmployeeRow employee = employees.get(0);
        employee.extra.set(123);

        // should fail
        employee.extra.getSubtable(TestPhoneTable.class);
    }
    */
}
