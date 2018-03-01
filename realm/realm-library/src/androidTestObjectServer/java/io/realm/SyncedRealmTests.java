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

import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.util.SyncTestUtils;

import static org.junit.Assert.fail;

/**
 * Testing sync specific methods on {@link Realm}.
 */
@RunWith(AndroidJUnit4.class)
public class SyncedRealmTests {

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;

    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    private Realm getNormalRealm() {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);
        return realm;
    }

    private Realm getPartialRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/fullsync")
                .partialRealm()
                .build();
        realm = Realm.getInstance(config);
        return realm;
    }

    private Realm getFullySyncRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/fullsync")
                .build();
        realm = Realm.getInstance(config);
        return realm;
    }

    @Test
    public void unsubscribeAsync_nullOrEmptyArgumentsThrows() {
        Realm realm = getPartialRealm();
        Realm.UnsubscribeCallback callback = new Realm.UnsubscribeCallback() {
            @Override
            public void onSuccess(String subscriptionName) {
            }

            @Override
            public void onError(String subscriptionName, Throwable error) {
            }
        };

        try {
            //noinspection ConstantConditions
            realm.unsubscribeAsync(null, callback);
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            realm.unsubscribeAsync("", callback);
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            //noinspection ConstantConditions
            realm.unsubscribeAsync("my-id", null);
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void unsubscribeAsync_nonLooperThreadThrows() {
        Realm realm = getPartialRealm();
        Realm.UnsubscribeCallback callback = new Realm.UnsubscribeCallback() {
            @Override
            public void onSuccess(String subscriptionName) {
            }

            @Override
            public void onError(String subscriptionName, Throwable error) {
            }
        };

        try {
            //noinspection ConstantConditions
            realm.unsubscribeAsync("my-id", callback);
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void unsubscribeAsync_nonPartialRealmThrows() {
        Realm.UnsubscribeCallback callback = new Realm.UnsubscribeCallback() {
            @Override
            public void onSuccess(String subscriptionName) {
            }

            @Override
            public void onError(String subscriptionName, Throwable error) {
            }
        };

        Realm realm = getNormalRealm();
        try {
            //noinspection ConstantConditions
            realm.unsubscribeAsync("my-id", callback);
            fail();
        } catch (UnsupportedOperationException ignore) {
        } finally {
            realm.close();
        }

        realm = getFullySyncRealm();
        try {
            //noinspection ConstantConditions
            realm.unsubscribeAsync("my-id", callback);
            fail();
        } catch (UnsupportedOperationException ignore) {
        } finally {
            realm.close();
        }

        looperThread.testComplete();
    }

    @Test
    public void delete_throws() {
        realm = getPartialRealm();
        realm.beginTransaction();
            try {
                realm.deleteAll();
                fail();
            } catch (IllegalStateException e) {
            }

            try {
                realm.delete(PartialSyncObjectA.class);
                fail();
            } catch (IllegalStateException e) {
            }
        realm.cancelTransaction();

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        try {
            dynamicRealm.beginTransaction();
            try {
                dynamicRealm.delete(PartialSyncObjectA.class.getSimpleName());
                fail();
            } catch (IllegalStateException e) {
            }
        } finally {
            dynamicRealm.close();
        }
    }

}
