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

import io.realm.entities.AllTypes;
import io.realm.internal.Row;


public class RealmObjectTest extends RealmSetupTests {

    // test io.realm.RealmObject Api

    // Row realmGetRow()
    public void testRealmGetRowReturnsValidRow() {

        testRealm.beginTransaction();
        RealmObject realmObject = testRealm.createObject( AllTypes.class);

        Row row = realmObject.realmGetRow();

        testRealm.commitTransaction();
        assertNotNull("RealmObject.realmGetRow returns zero ", row);
        assertEquals("RealmObject.realmGetRow seems to return wrong row type: ",7 , row.getColumnCount());
    }

}
