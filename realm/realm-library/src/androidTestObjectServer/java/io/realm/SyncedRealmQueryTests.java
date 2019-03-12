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
import java.util.UUID;
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

    private String randomName() {
        return UUID.randomUUID().toString();
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
        String name = randomName();
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo");
        Subscription sub = query.subscribe(name);
        assertEquals(name, sub.getName());
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
        Subscription sub = query.subscribe(randomName(), 0, TimeUnit.MILLISECONDS);
        assertTrue(now.getTime() < sub.getCreatedAt().getTime());
        assertEquals(sub.getCreatedAt(), sub.getUpdatedAt());
        assertEquals(sub.getUpdatedAt(), sub.getExpiresAt());
        assertEquals(0, sub.getTimeToLive());
    }

    @Test
    public void subscribeOrUpdate() {
        String name = randomName();
        realm = getPartialRealm();
        realm.beginTransaction();
        RealmQuery<AllTypes> query1 = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo");
        Subscription sub1 = query1.subscribe(name);
        Date firstUpdate = sub1.getUpdatedAt();
        RealmQuery<AllTypes> query2 = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_BOOLEAN, false);
        SystemClock.sleep(2);
        Subscription sub2 = query2.subscribeOrUpdate(name);
        assertEquals(sub1, sub2);
        assertEquals(query2.getDescription(), sub2.getQueryDescription());
        assertTrue(firstUpdate.getTime() < sub2.getUpdatedAt().getTime());
    }

    @Test
    public void subscribeOrUpdate_failsWithDifferentQueryType() {
        String name = randomName();
        realm = getPartialRealm();
        realm.beginTransaction();
        realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo").subscribe(name);
        try {
            realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_BOOLEAN, false).subscribeOrUpdate(name);
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void subscribeOrUpdate_withTimeToLive() {
        String name = randomName();
        realm = getPartialRealm();
        realm.beginTransaction();
        realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo").subscribe(name, 10, TimeUnit.MILLISECONDS);
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_BOOLEAN, false);
        Subscription sub = query.subscribeOrUpdate(name, 20, TimeUnit.DAYS);
        assertEquals(TimeUnit.MILLISECONDS.convert(20, TimeUnit.DAYS), sub.getTimeToLive());
        assertEquals(query.getDescription(), sub.getQueryDescription());
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
    public void subscription_setTimeToLive() {
        realm = getPartialRealm();
        realm.beginTransaction();

        Subscription sub = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo").subscribe();
        assertEquals(Long.MAX_VALUE, sub.getExpiresAt().getTime());
        assertEquals(Long.MAX_VALUE, sub.getTimeToLive());

        Date now = new Date();
        Date now_plus_1_sec = new Date(now.getTime() + 1000);
        Date now_plus_11_sec = new Date(now.getTime() + 11000);
        SystemClock.sleep(2);
        sub.setTimeToLive(10, TimeUnit.SECONDS);
        assertEquals(10000, sub.getTimeToLive());
        assertTrue(now.getTime() < sub.getUpdatedAt().getTime());
        assertTrue(now.getTime() < sub.getExpiresAt().getTime());
        assertTrue(sub.getUpdatedAt().getTime() < now_plus_1_sec.getTime());
        assertTrue(sub.getExpiresAt().getTime() < now_plus_11_sec.getTime());
    }

    @Test
    public void subscription_setTimeToLive_illegalValuesThrows() {
        realm = getPartialRealm();
        realm.beginTransaction();
        Subscription sub = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "foo").subscribe();
        try {
            sub.setTimeToLive(-1, TimeUnit.SECONDS);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("A negative time-to-live is not allowed"));
        }
        try {
            sub.setTimeToLive(0, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Non-null 'timeUnit' required"));
        }
    }

}
