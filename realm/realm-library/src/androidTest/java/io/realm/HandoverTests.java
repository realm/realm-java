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
 */

package io.realm;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.util.RealmBackgroundTask;

/**
 * Class for testing the public handover functionality
 */
@RunWith(AndroidJUnit4.class)
public class HandoverTests extends AndroidTestCase {

    @Rule
    public final TestRealmConfigurationFactory configurationFactory = new TestRealmConfigurationFactory();

    private static final int TEST_DATA_SIZE = 10;
    private RealmConfiguration config;
    private Realm realm;

    @Before
    public void setUp() {
        config = configurationFactory.createConfiguration();
        realm = Realm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    // Add a number of AllType objects with an increasing number of items in `columnRealmList`
    private void populateTestRealm(int dataSize) {
        realm.beginTransaction();
        for (int i = 0; i < dataSize; i++) {
            Owner owner = realm.createObject(Owner.class);
            owner.setName("Owner " + i);

            Cat cat = realm.createObject(Cat.class);
            cat.setName("Cat " + i);
            owner.setCat(cat);

            // Add increasing number of dogs
            for (int j = 0; j < i; j++) {
                Dog dog = realm.createObject(Dog.class);
                dog.setName(String.format("Dog (%d, %d)", i, j));
                dog.setAge(j);
                dog.setOwner(owner);
                owner.getDogs().add(dog);
            }
        }
        realm.commitTransaction();
    }

    // Query on a regular table
    @Test
    public void threadLocalVersion_realmQuery_tableView() {
        populateTestRealm(TEST_DATA_SIZE);

        final RealmQuery<Owner> query = realm.where(Owner.class);
        RealmBackgroundTask bgTask = new RealmBackgroundTask(config);
        bgTask.run(new RealmBackgroundTask.Task() {
            @Override
            public void run(Realm realm) {
                RealmQuery<Owner> bgQuery = realm.threadLocalVersion(query);
                RealmResults<Owner> results = bgQuery.findAll();
                assertEquals(TEST_DATA_SIZE, results.size());
            }
        });
        bgTask.checkFailure();
    }

    // Query on a table view (sub query)
    @Test
    public void threadLocalVersion_realmQuery_tableViewSubQuery() {
        populateTestRealm(TEST_DATA_SIZE);
        RealmResults<Dog> results = realm.where(Dog.class).beginsWith("name", "Dog (9, ").findAll();
        assertEquals(9, results.size());

        final RealmQuery<Dog> subQuery = results.where().endsWith("name", "1)");
        RealmBackgroundTask bgTask = new RealmBackgroundTask(config);
        bgTask.run(new RealmBackgroundTask.Task() {
            @Override
            public void run(Realm realm) {
                RealmQuery<Dog> bgSubQuery = realm.threadLocalVersion(subQuery);
                RealmResults<Dog> results = bgSubQuery.findAll();
                assertEquals(1, results.size());
            }
        });
        bgTask.checkFailure();
    }

    // Query on a LinkView
    @Test
    public void threadLocalVersion_realmQuery_linkView() {
        populateTestRealm(10);
        RealmList<Dog> list = realm.where(Owner.class).equalTo("name", "Owner 9").findFirst().getDogs();
        assertEquals(9, list.size());

        final RealmQuery<Dog> query = list.where().equalTo("age", 1);
        RealmBackgroundTask bgTask = new RealmBackgroundTask(config);
        bgTask.run(new RealmBackgroundTask.Task() {
            @Override
            public void run(Realm realm) {
                RealmQuery<Dog> bgQuery = realm.threadLocalVersion(query);
                RealmResults<Dog> results = bgQuery.findAll();
                assertEquals(1, results.size());
            }
        });
        bgTask.checkFailure();
    }

    // Sub query on a LinkView
    @Test
    public void threadLocalVersion_realmQuery_linkViewSubQuery() {
        populateTestRealm(10);
        RealmList<Dog> list = realm.where(Owner.class).equalTo("name", "Owner 9").findFirst().getDogs();
        assertEquals(9, list.size());
        final RealmResults<Dog> results = list.where().lessThan("age", 5).findAll();
        assertEquals(5, results.size());

        final RealmQuery<Dog> subQuery = results.where().greaterThan("age", 3);
        RealmBackgroundTask bgTask = new RealmBackgroundTask(config);
        bgTask.run(new RealmBackgroundTask.Task() {
            @Override
            public void run(Realm realm) {
                RealmQuery<Dog> bgSubQuery = realm.threadLocalVersion(subQuery);
                RealmResults<Dog> results = bgSubQuery.findAll();
                assertEquals(1, results.size());
            }
        });
        bgTask.checkFailure();
    }
}
