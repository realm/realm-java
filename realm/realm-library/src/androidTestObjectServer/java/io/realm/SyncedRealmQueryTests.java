/*
 * Copyright 2018 Realm Inc.
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.rule.RunInLooperThread;
import io.realm.sync.Subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing sync specific methods on {@link RealmQuery}.
 */
@RunWith(AndroidJUnit4.class)
public class SyncedRealmQueryTests {

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private DynamicRealm dynamicRealm;

    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
        if (dynamicRealm != null && !dynamicRealm.isClosed()) {
            dynamicRealm.close();
        }
        for (SyncUser user : SyncUser.all().values()) {
            user.logOut();
        }
    }

    private Realm getPartialRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/partialSync")
                .build();
        realm = Realm.getInstance(config);
        return realm;
    }

    private Realm getFullySyncRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/fullSync")
                .fullSynchronization()
                .build();
        realm = Realm.getInstance(config);
        return realm;
    }

    @Test
    public void subscribe() {
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo");
        Subscription sub = query.subscribe();
        assertTrue(sub.getName().startsWith("[AllTypes] "));
        assertEquals(Subscription.State.PENDING, sub.getState());
        assertEquals("", sub.getErrorMessage());
        assertEquals(query.getDescription(), sub.getQueryDescription());
        assertEquals("AllTypes", sub.getQueryClassName());
    }

    @Test
    public void subscribe_withName() {
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo");
        Subscription sub = query.subscribe("sub");
        assertEquals("sub", sub.getName());
        assertEquals(Subscription.State.PENDING, sub.getState());
        assertEquals("", sub.getErrorMessage());
        assertEquals(query.getDescription(), sub.getQueryDescription());
        assertEquals("AllTypes", sub.getQueryClassName());
    }

    @Test
    public void subscribe_throwIfNameIsAlreadyUsed() {
        realm = getPartialRealm();
        realm.beginTransaction();
        realm.where(Dog.class).subscribe("foo");
        try {
            realm.where(AllTypes.class).subscribe("foo");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void subscribe_throwOnDynamicRealm() {
        getPartialRealm().close(); // Build schema
        dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        dynamicRealm.beginTransaction();
        RealmQuery<DynamicRealmObject> query = dynamicRealm.where(AllTypes.CLASS_NAME);
        try {
            query.subscribe("sub");
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void subscribe_throwIfOutsideWriteTransaction() {
        realm = getPartialRealm();
        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        try {
            query.subscribe("sub");
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void subscribe_throwIfBasedOnList() {
        realm = getPartialRealm();
        realm.beginTransaction();
        realm.createObject(AllTypes.class).getColumnRealmList().add(new Dog("fido"));
        RealmQuery<Dog> query = realm.where(AllTypes.class).findFirst().getColumnRealmList().where();
        try {
            query.subscribe("sub");
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void subscribe_throwIfNonPartialRealm() {
        realm = getFullySyncRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        try {
            query.subscribe("sub");
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void subscribe_throwIfRealmClosed() {
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        realm.close();
        try {
            query.subscribe("sub");
            fail();
        } catch (IllegalStateException ignore) {
        }
    }
}
