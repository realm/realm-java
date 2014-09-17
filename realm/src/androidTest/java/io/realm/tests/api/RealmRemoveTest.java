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

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.tests.api.entities.AllColumns;


public class RealmRemoveTest extends RealmSetupTests {
    // <E> void remove(Class<E> clazz, long objectIndex)
    public void testShouldRemoveRow() {
        Realm realm = getTestRealm();
        //buildAllColumnsTestData(realm);


        realm.clear();

        realm.beginWrite();

        AllColumns allColumns = null;
        allColumns = getTestObject(realm, AllColumns.class);
        allColumns.setColumnBoolean(true);
        allColumns.setColumnBinary(new byte[]{1,2,3});
        allColumns.setColumnDate(new Date());
        allColumns.setColumnDouble(3.1415);
        allColumns.setColumnFloat(1.234567f);
        allColumns.setColumnString("test data");
        allColumns.setColumnLong(45);

        allColumns = null;
        allColumns = getTestObject(realm, AllColumns.class);
        allColumns.setColumnBoolean(false);
        allColumns.setColumnBinary(new byte[]{4,5,6});
        allColumns.setColumnDate(new Date());
        allColumns.setColumnDouble(9999.99);
        allColumns.setColumnFloat(0.1f);
        allColumns.setColumnString("more data");
        allColumns.setColumnLong(46);

        realm.commit();

        realm.beginWrite();

        realm.remove(AllColumns.class,0);

        realm.commit();

        RealmList<AllColumns> realmList = realm.where(AllColumns.class).findAll();
        boolean checkListSize = realmList.size() == TEST_DATA_SIZE - 1;
        assertTrue("Realm.delete has not deleted record correctly",checkListSize);
    }

}