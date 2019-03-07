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

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.entities.AllJavaTypes;
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
        Date now = new Date();
        SystemClock.sleep(2);
        Subscription sub = query.subscribe();
        assertTrue(sub.getName().startsWith("[AllTypes] "));
        assertEquals(Subscription.State.PENDING, sub.getState());
        assertEquals("", sub.getErrorMessage());
        assertEquals(query.getDescription(), sub.getQueryDescription());
        assertEquals("AllTypes", sub.getQueryClassName());
        assertTrue(now.getTime() < sub.getCreatedAt().getTime());
        assertTrue(now.getTime() < sub.getUpdatedAt().getTime());
        assertTrue(sub.getCreatedAt().getTime() == sub.getUpdatedAt().getTime());
        assertEquals(Long.MAX_VALUE, sub.getTimeToLive());
        assertEquals(new Date(Long.MAX_VALUE), sub.getExpiresAt());
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
    public void subscribe_withTimeToLive() {
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo");
        Date now = new Date();
        SystemClock.sleep(2);
        Subscription sub = query.subscribe("sub2", 0, TimeUnit.MILLISECONDS);
        assertTrue(now.getTime() < sub.getCreatedAt().getTime());
        assertEquals(sub.getCreatedAt(), sub.getUpdatedAt());
        assertEquals(sub.getUpdatedAt(), sub.getExpiresAt());
        assertEquals(0, sub.getTimeToLive());
    }

    @Test
    public void subscription_setQuery() {
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query1 = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo");
        Date now = new Date();
        SystemClock.sleep(2);
        Subscription sub = query1.subscribe("sub3");
        RealmQuery<AllTypes> query2 = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_BOOLEAN, false);
        assertEquals("AllTypes", sub.getQueryClassName());
        assertTrue(now.getTime() < sub.getUpdatedAt().getTime());
        Date query1Updated = sub.getUpdatedAt();
        SystemClock.sleep(2);
        sub.setQuery(query2);
        assertEquals(query2.getDescription(), sub.getQueryDescription());
        assertEquals("AllTypes", sub.getQueryClassName());
        assertTrue(query1Updated.getTime() < sub.getUpdatedAt().getTime());
    }

    @Test
    public void subscription_setQuery_wrongTypeThrows() {
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query1 = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo");
        Subscription sub = query1.subscribe("sub4");
        RealmQuery<AllJavaTypes> query2 = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_BOOLEAN, false);
        try {
            sub.setQuery(query2);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("It is only allowed to replace a query"));
        }
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
