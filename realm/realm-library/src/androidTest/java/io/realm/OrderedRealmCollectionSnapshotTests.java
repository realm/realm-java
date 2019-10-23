/*
 * Copyright 2017 Realm Inc.
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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Unit tests specific for {@link OrderedRealmCollectionSnapshot} that cannot be covered by
 * {@link OrderedRealmCollectionTests}, {@link ManagedRealmCollectionTests}, {@link UnManagedRealmCollectionTests} or
 * {@link RealmCollectionTests}.
 */
@RunWith(AndroidJUnit4.class)
public class OrderedRealmCollectionSnapshotTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private static final int TEST_SIZE = 10;
    private Realm realm;
    private OrderedRealmCollection<AllTypes> snapshot;

    @Before
    public void setup() {
        realm = Realm.getInstance(configFactory.createConfiguration());
        populateRealm(realm, TEST_SIZE);
        snapshot = realm.where(AllTypes.class).findAll().createSnapshot();
    }

    @After
    public void tearDown() {
        realm.close();
    }

    private void populateRealm(Realm realm, int testSize) {
        realm.beginTransaction();
        for (int i = 0; i < testSize; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
        }
        realm.commitTransaction();
    }

    @Test
    public void deleteFromRealm_twice() {
        realm.beginTransaction();
        snapshot.deleteFromRealm(0);
        snapshot.deleteFromRealm(0);
        realm.commitTransaction();
        assertFalse(snapshot.get(0).isValid());
    }

    @Test
    public void deleteFirstFromRealm_twice() {
        realm.beginTransaction();
        assertTrue(snapshot.deleteFirstFromRealm());
        assertFalse(snapshot.deleteFirstFromRealm());
        realm.commitTransaction();
    }

    @Test
    public void deleteLastFromRealm_twice() {
        realm.beginTransaction();
        assertTrue(snapshot.deleteLastFromRealm());
        assertFalse(snapshot.deleteLastFromRealm());
        realm.commitTransaction();
    }

    @Test
    public void deleteAllFromRealmTwice() {
        realm.beginTransaction();
        assertTrue(snapshot.deleteAllFromRealm());
        assertTrue(snapshot.deleteAllFromRealm());
        realm.commitTransaction();
    }
}
