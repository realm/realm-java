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

import java.io.File;
import java.io.IOException;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
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
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
        for (SyncUser user : SyncUser.all().values()) {
            user.logOut();
        }
    }

    private Realm getNormalRealm() {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);
        return realm;
    }

    private Realm getPartialRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/fullsync")
                .build();
        realm = Realm.getInstance(config);
        return realm;
    }

    private Realm getFullySyncRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/fullsync")
                .fullSynchronization()
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

    @Test
    public void compactRealm_populatedRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), Constants.DEFAULT_REALM).build();
        realm = Realm.getInstance(config);
        realm.executeTransaction(r -> {
            for (int i = 0; i < 10; i++) {
                r.insert(new AllJavaTypes(i));
            }
        });
        realm.close();
        assertTrue(Realm.compactRealm(config));
        realm = Realm.getInstance(config);
        assertEquals(10, realm.where(AllJavaTypes.class).count());
    }

    @Test
    public void compactOnLaunch_shouldCompact() throws IOException {
        SyncUser user = SyncTestUtils.createTestUser();

        // Fill Realm with data and record size
        SyncConfiguration config1 = configFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM).build();
        realm = Realm.getInstance(config1);
        byte[] oneMBData = new byte[1024 * 1024];
        realm.beginTransaction();
        for (int i = 0; i < 10; i++) {
            realm.createObject(AllTypes.class).setColumnBinary(oneMBData);
        }
        realm.commitTransaction();
        realm.close();
        long originalSize = new File(realm.getPath()).length();

        // Open Realm with CompactOnLaunch
        SyncConfiguration config2 = configFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM)
                .compactOnLaunch(new CompactOnLaunchCallback() {
                    @Override
                    public boolean shouldCompact(long totalBytes, long usedBytes) {
                        return true;
                    }
                })
                .build();
        realm = Realm.getInstance(config2);
        realm.close();
        long compactedSize = new File(realm.getPath()).length();

        assertTrue(originalSize > compactedSize);
    }

}
