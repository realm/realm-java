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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.net.URI;

import io.realm.internal.ObjectServerFacade;
import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.util.SyncTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
        for (SyncUser syncUser : SyncUser.all().values()) {
            syncUser.logout();
        }
        ObjectServerFacade.getSyncFacadeIfPossible().removeCachedDefaultSyncConfiguration();
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
    public void getDefaultInstance_noUserThrows() {
        try {
            Realm.getDefaultInstance();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().startsWith("No user is logged in"));
        }
    }

    @Test
    public void getDefaultInstance_multipleUsersThrows() {
        SyncTestUtils.createTestUser();
        SyncTestUtils.createTestUser();
        try {
            Realm.getDefaultInstance();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("multiple users are logged in"));
        }
    }


    @Test
    public void getDefaultInstance_clearCacheIfNewDefaultUser() {
        SyncUser user1 = SyncTestUtils.createTestUser();
        Realm realm1 = Realm.getDefaultInstance();
        user1.logout();

        SyncUser user2 = SyncTestUtils.createTestUser();
        Realm realm2 = Realm.getDefaultInstance();

        try {
            assertNotEquals(user1, user2);
            assertFalse(realm1 == realm2);
            assertNotEquals(realm1.getPath(), realm2.getPath());
        } finally {
            realm1.close();
            realm2.close();
        }
    }

    @Test
    @SuppressWarnings("ReferenceEquality")
    public void getDefaultInstance_rebuildDefaultConfigIfCleared() {
        // The interaction between `getDefaultInstance` and `getDefaultConfiguration/removeDefaultConfiguration`
        // is a bit tricky. But the following is implemented:
        //
        // Calling `removeDefaultConfiguration` will delete any cached config until `getDefaultInstance()`
        // is called again, i.e. you can call `removeDefaultConfiguration` and check that `getDefaultConfiguration`
        // returns `null`
        SyncTestUtils.createTestUser();
        Realm realm1 = Realm.getDefaultInstance();
        RealmConfiguration oldConfig = realm1.getConfiguration();

        Realm.removeDefaultConfiguration();
        assertNull(Realm.getDefaultConfiguration());

        Realm realm2 = Realm.getDefaultInstance();
        RealmConfiguration newConfig = realm2.getConfiguration();

        try {
            assertTrue(realm1  == realm2);
            assertTrue(oldConfig == newConfig); // We hit the cache, so old config is reused
            assertEquals(oldConfig, newConfig);
        } finally {
            realm1.close();
            realm2.close();
        }
    }

    @Test
    public void getDefaultInstance() {
        SyncUser user1 = SyncTestUtils.createTestUser();
        realm = Realm.getDefaultInstance();
        assertTrue(realm.isEmpty());
        assertEquals(user1, ((SyncConfiguration) realm.getConfiguration()).getUser());
    }

    @Test
    public void getDefaultInstance_automaticallyConvertUrls() {
        Object[][] input = {
                // AuthUrl -> Expected Realm URL
                { "http://ros.realm.io/auth", "realm://ros.realm.io/default" },
                { "http://ros.realm.io:7777", "realm://ros.realm.io/default" },
                { "http://127.0.0.1/auth", "realm://127.0.0.1/default" },
                { "HTTP://ros.realm.io" , "realm://ros.realm.io/default" },

                { "https://ros.realm.io/auth", "realms://ros.realm.io/default" },
                { "https://ros.realm.io:7777", "realms://ros.realm.io/default" },
                { "https://127.0.0.1/auth", "realms://127.0.0.1/default" },
                { "HTTPS://ros.realm.io" , "realms://ros.realm.io/default" },
        };

        for (Object[] test : input) {
            String authUrl = (String) test[0];
            String realmUrl = (String) test[1];

            SyncUser user = SyncTestUtils.createTestUser(authUrl);
            Realm realm = Realm.getDefaultInstance();
            SyncConfiguration config = (SyncConfiguration) realm.getConfiguration();
            URI url = config.getServerUrl();
            assertEquals(realmUrl, url.toString());
            realm.close();
            user.logout();
        }
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
