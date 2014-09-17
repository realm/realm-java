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

package io.realm.tests.api;

import io.realm.Realm;

import io.realm.tests.api.entities.AllColumns;

public class RealmContainsTest extends RealmSetupTests {

    //boolean contains(Class<?> clazz)
    public void testShouldContainTable() {
        Realm testRealm = getTestRealm();
        testRealm.getTable(AllColumns.class);

        boolean testIfContained = testRealm.contains(AllColumns.class);
        assertTrue("contains returns false for newly created table", testIfContained);
    }

    //boolean contains(Class<?> clazz)
    public void testShouldNotContainTable() {
        Realm testRealm = getTestRealm();

        boolean testIfContained = testRealm.contains(AllColumns.class);
        assertFalse("contains returns true for non-existing table", testIfContained);
    }

}