/*
 * Copyright 2016 Realm Inc.
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


import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.TestCase.assertNotNull;

// This test is for the fact we don't have locks for native objects creation that when finalizer/phantom thread free the
// native object, the same Realm could have some native objects creation at the same time.
// If the native object's destructor is not thread safe, there is a big chance that those tests crash with a seg-fault.
// test_destructor_thread_safety.cpp in core tests the similar things.
@RunWith(AndroidJUnit4.class)
public class GCTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private RealmConfiguration realmConfig;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
        Realm realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        realm.createObject(AllTypes.class).getColumnRealmList().add(realm.createObject(Dog.class));
        realm.commitTransaction();
        realm.close();

    }

    @After
    public void tearDown() {
    }

    @Test
    public void createRealmResults() {
        for (int i = 0; i < 100; i++) {
            Realm realm = Realm.getInstance(realmConfig);
            for (int j = 0; j < 1000; j++) {
                realm.where(AllTypes.class).findAll();
            }
            realm.close();
        }
    }

    @Test
    public void createRealmResultsFromRealmResults() {
        for (int i = 0; i < 100; i++) {
            Realm realm = Realm.getInstance(realmConfig);
            for (int j = 0; j < 1000; j++) {
                realm.where(AllTypes.class).findAll().where().findAll();
            }
            realm.close();
        }
    }

    @Test
    public void createRealmResultsFromRealmList() {
        for (int i = 0; i < 100; i++) {
            Realm realm = Realm.getInstance(realmConfig);
            for (int j = 0; j < 1000; j++) {
                AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                assertNotNull(allTypes);
                allTypes.getColumnRealmList().where().findAll();
            }
            realm.close();
        }
    }

    @Test
    public void createRealmObject() {
        for (int i = 0; i < 100; i++) {
            Realm realm = Realm.getInstance(realmConfig);
            for (int j = 0; j < 1000; j++) {
                realm.where(AllTypes.class).findFirst();
            }
            realm.close();
        }
    }

    @Test
    public void createRealmObjectFromRealmResults() {
        for (int i = 0; i < 100; i++) {
            Realm realm = Realm.getInstance(realmConfig);
            for (int j = 0; j < 1000; j++) {
                assertNotNull(realm.where(AllTypes.class).findAll().first());
            }
            realm.close();
        }
    }

    @Test
    public void createRealmObjectsFromRealmList() {
        for (int i = 0; i < 100; i++) {
            Realm realm = Realm.getInstance(realmConfig);
            for (int j = 0; j < 1000; j++) {
                AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                assertNotNull(allTypes);
                assertNotNull(allTypes.getColumnRealmList().first());
            }
            realm.close();
        }
    }
}
