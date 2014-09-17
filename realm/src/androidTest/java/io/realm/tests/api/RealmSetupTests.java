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

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.internal.SharedGroup;
import io.realm.tests.api.entities.AllColumns;


public class RealmSetupTests extends AndroidTestCase {

    // Test setup methods:
    protected void setupSharedGroup() {
        Realm.setDefaultDurability(SharedGroup.Durability.FULL);
    }

    protected Realm getTestRealm() {
        setupSharedGroup();
        
        Realm testRealm = null;
        try {
            testRealm = new Realm(getContext().getFilesDir());
        } catch (IOException ioe) {
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception while initializing test case: "+ex.getMessage());
        }
        return testRealm;
    }

    protected <E extends RealmObject> E getTestObject(Realm realm, Class<E> clazz) {
        setupSharedGroup();

        return realm.create(clazz);
    }

    final static int TEST_DATA_SIZE = 2;
    protected void buildAllColumnsTestData(Realm realm) {
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
    }

}