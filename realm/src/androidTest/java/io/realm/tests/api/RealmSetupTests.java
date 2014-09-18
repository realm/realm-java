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
import io.realm.tests.api.entities.AllTypes;


public class RealmSetupTests extends AndroidTestCase {

    // Test setup methods:
    protected void setupSharedGroup() {
        Realm.setDefaultDurability(SharedGroup.Durability.FULL);
    }

    protected Realm getTestRealm() throws IOException {
        setupSharedGroup();
        
        Realm testRealm = new Realm(getContext().getFilesDir());
        testRealm.clear();
        return testRealm;
    }

    protected <E extends RealmObject> E getTestObject(Realm realm, Class<E> clazz) {
        return realm.create(clazz);
    }

    protected void buildAllTypesTestData(Realm realm, int numberOfRecords) {
        realm.clear();
        realm.beginWrite();

        for (int i = 0; i < numberOfRecords; ++i) {
            AllTypes allTypes = getTestObject(realm, AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415 + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
        }
        realm.commit();
    }

}