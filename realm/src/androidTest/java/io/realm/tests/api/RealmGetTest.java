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
import io.realm.RealmObject;
import io.realm.tests.api.entities.AllColumns;


public class RealmGetTest extends RealmSetupTests {
 
    // <E extends RealmObject> E get(Class<E> clazz, long rowIndex)
    public void testShouldGetObject() {
        Realm realm = getTestRealm();
        buildAllColumnsTestData(realm, 2);

        RealmObject allColumns = realm.get(AllColumns.class,0);
        boolean instanceMatch = allColumns instanceof AllColumns;
        assertTrue("Realm.get is returning wrong object type", instanceMatch);
    }

}