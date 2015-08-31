/*
 * Copyright 2015 Realm Inc.
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
 *
 */

package io.realm;


import android.test.AndroidTestCase;

import io.realm.dynamic.DynamicRealm;
import io.realm.entities.AllTypes;

public class DynamicRealmTest extends AndroidTestCase {

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }

    // Test that the SharedGroupManager is not reused across Realm/DynamicRealm on the same thread.
    // This is done by starting a write transaction in one Realm and verifying that none of the data
    // written (but not committed) is available in the other Realm.
    public void testSeperateSharedGroups() {
        RealmConfiguration config = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(config);
        Realm typedRealm = Realm.getInstance(config);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);

        assertEquals(0, typedRealm.where(AllTypes.class).count());
        assertEquals(0, dynamicRealm.where("AllTypes").count());

        typedRealm.beginTransaction();
        try {
            typedRealm.createObject(AllTypes.class);
            assertEquals(1, typedRealm.where(AllTypes.class).count());
            assertEquals(0, dynamicRealm.where("AllTypes").count());
            typedRealm.cancelTransaction();
        } finally {
            typedRealm.close();
            dynamicRealm.close();
        }
    }

    // Test that Realms can only be deleted after all Typed and Dynamic instances are closed.
    public void testDeleteAfterAllIsClosed() {
        RealmConfiguration config = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(config);

        Realm typedRealm = Realm.getInstance(config);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);

        typedRealm.close();
        try {
            Realm.deleteRealm(config);
            fail();
        } catch (IllegalStateException ignored) {
        }

        dynamicRealm.close();
        assertTrue(Realm.deleteRealm(config));
    }

//    // Test that the closed Realm isn't kept in the Realm instance cache
//    public void testRealmCacheIsCleared() {
//        RealmConfiguration config = TestHelper.createConfiguration(getContext());
//        Realm.deleteRealm(config);
//
//        Realm typedRealm = Realm.getInstance(config);
//        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
//
//        typedRealm.close(); // Still a instance open, but typed Realm cache must still be cleared.
//        dynamicRealm.close();
//
//        try {
//            typedRealm = Realm.getInstance(config);
//            assertEquals(0, typedRealm.where(AllTypes.class).count());
//        } finally {
//            typedRealm.close();
//        }
//    }
//
//    // Test that the closed DynamicRealms isn't kept in the DynamicRealm instance cache
//    public void testDynamicRealmCacheIsCleared() {
//        RealmConfiguration config = TestHelper.createConfiguration(getContext());
//        Realm.deleteRealm(config);
//
//        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
//        Realm typedRealm = Realm.getInstance(config);
//
//        dynamicRealm.close(); // Still a instance open, but DynamicRealm cache must still be cleared.
//        typedRealm.close();
//
//        try {
//            dynamicRealm = DynamicRealm.getInstance(config);
//            assertEquals(0, dynamicRealm.getVersion());
//        } finally {
//            dynamicRealm.close();
//        }
//    }
}
