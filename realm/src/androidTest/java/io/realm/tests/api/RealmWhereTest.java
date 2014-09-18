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
import io.realm.ResultList;
import io.realm.tests.api.entities.AllColumns;


public class RealmWhereTest extends RealmSetupTests {
    final static int TEST_DATA_SIZE = 2;

    // <E extends RealmObject> RealmQuery<E> where(Class<E> clazz)
    public void testShouldReturnResultSet() {
        Realm realm = getTestRealm();
        buildAllColumnsTestData(realm, TEST_DATA_SIZE);

        ResultList<AllColumns> realmList = realm.where(AllColumns.class).findAll();

        boolean checkListSize = realmList.size() == TEST_DATA_SIZE;
        assertTrue("Realm.get is returning wrong number of objects",checkListSize);
    }

}