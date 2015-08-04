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

public class DynamicRealmTest extends AndroidTestCase {

    private Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(realmConfig);
        testRealm = Realm.getInstance(realmConfig);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (testRealm != null) {
            testRealm.close();
        }
    }

    // Test that if the same configuration is used for both dynamic/typed Realm that the underlying
    // instance is the same.This can be done by saving a object in one Realm and having it
    // immediately available in the other without refreshing.
    public void NotYet_testInstanceCache() {
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).name("my-realm").build();
        Realm.deleteRealm(config);
        Realm typedRealm = Realm.getInstance(config);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);

        // TODO
        typedRealm.close();
        dynamicRealm.close();
    }


    // Test that it is not possible to manipulate a Realm file if either a DynamicRealm or typed
    // realm is still open
    public void testTrackingClosedInstances() {
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).name("closed-instances").build();
        Realm.deleteRealm(config);

        Realm realm = Realm.getInstance(config);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        realm.close();

        try {
            Realm.deleteRealm(config); // DynamicRealm should still keep the file open
            fail();
        } catch (IllegalStateException ignored) {
        }

        dynamicRealm.close(); // Closing last open instance
        Realm.deleteRealm(config); // It is now safe to delete the file.
    }
}
