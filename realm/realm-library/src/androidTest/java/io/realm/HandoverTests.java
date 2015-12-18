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

import android.test.AndroidTestCase;
import android.util.Log;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.util.RealmBackgroundTask;

/**
 * Class for testing the public handover functionality
 */
public class HandoverTests extends AndroidTestCase {

    private static final int TEST_DATA_SIZE = 10;
    private RealmConfiguration config;
    private Realm realm;

    @Override
    protected void setUp() throws Exception {
        config = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
    }

    @Override
    protected void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    // Add a number of AllType objects with an increasing number of items in `columnRealmList`
    private void populateTestRealm(int dataSize) {
        realm.beginTransaction();
        for (int i = 0; i < dataSize; i++) {
            AllTypes obj = realm.createObject(AllTypes.class);
            obj.setColumnLong(i);
            obj.setColumnString(Integer.toString(i));
            obj.setColumnBoolean(i % 2 == 0);

            Dog singleDog = realm.createObject(Dog.class);
            singleDog.setName("Name " + i);
            obj.setColumnRealmObject(singleDog);

            // Add increasing number of dogs
            for (int j = 0; j < i; j++) {
                Dog dog = realm.createObject(Dog.class);
                dog.setName(String.format("Dog (%d, %d)", i, j));
                dog.setAge(j);
                obj.getColumnRealmList().add(dog);
            }
        }
        realm.commitTransaction();
    }

    // Query on a regular table
    public void testThreadLocalVersion_realmQuery_tableView() {
        populateTestRealm(TEST_DATA_SIZE);
        final RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        RealmBackgroundTask bgTask = new RealmBackgroundTask(config);
        bgTask.run(new RealmBackgroundTask.Task() {
            @Override
            public void run(Realm realm) {
                RealmQuery<AllTypes> bgQuery = realm.threadLocalVersion(query);
                RealmResults<AllTypes> results = bgQuery.findAll();
                assertEquals(TEST_DATA_SIZE, results.size());
            }
        });
        bgTask.checkFailure();
    }

    // Link query on a regular table
    public void testThreadLocalVersion_realmQuery_tableViewLinkQuery() {
        populateTestRealm(10);
        final RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo("columnRealmObject.name", "Name 1");
        RealmBackgroundTask bgTask = new RealmBackgroundTask(config);
        bgTask.run(new RealmBackgroundTask.Task() {
            @Override
            public void run(Realm realm) {
                RealmQuery<AllTypes> bgQuery = realm.threadLocalVersion(query);
                RealmResults<AllTypes> results = bgQuery.findAll();
                assertEquals(1, results.size());
            }
        });
        bgTask.checkFailure();
    }

    // Query on a table view (sub query)
    public void testThreadLocalVersion_realmQuery_tableViewSubQuery() {
        populateTestRealm(10);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 9).findAll();
        final RealmQuery<AllTypes> subQuery = results.where();
        RealmBackgroundTask bgTask = new RealmBackgroundTask(config);
        bgTask.run(new RealmBackgroundTask.Task() {
            @Override
            public void run(Realm realm) {
                // FIXME: This fails. Running the same query on the same thread as the original query gives the correct result
                RealmQuery<AllTypes> bgSubQuery = realm.threadLocalVersion(subQuery);
                RealmResults<AllTypes> results = bgSubQuery.findAll();
                assertEquals(1, results.size());
            }
        });
        bgTask.checkFailure();
    }

    // Link query on a table view
    public void testThreadLocalVersion_realmQuery_tableViewSubQueryLinkQuery() {
        fail();
    }

    // Query on a LinkView
    public void testThreadLocalVersion_realmQuery_linkView() {
        fail();
    }

    // Link query on a LinkView
    public void testThreadLocalVersion_realmQuery_linkViewLinkQuery() {
        fail();
    }
}
