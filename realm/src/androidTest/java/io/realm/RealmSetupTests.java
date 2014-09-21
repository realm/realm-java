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

import android.test.AndroidTestCase;

import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.internal.SharedGroup;


public class RealmSetupTests extends AndroidTestCase {

    protected final static int TEST_DATA_SIZE = 159;
    //private   final static SharedGroup.Durability SG_DURABILITY = SharedGroup.Durability.MEM_ONLY;
    private   final static SharedGroup.Durability SG_DURABILITY = SharedGroup.Durability.FULL;

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {

        Realm.setDefaultDurability(SharedGroup.Durability.MEM_ONLY);

        testRealm = new Realm(getContext());

        testRealm.clear();

        testRealm.beginWrite();

        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            AllTypes allTypes = testRealm.create(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415 + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
        }
        testRealm.commit();
    }


}