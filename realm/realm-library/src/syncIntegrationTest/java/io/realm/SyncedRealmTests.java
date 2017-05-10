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

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.StringOnly;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.objectserver.BaseIntegrationTest;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Catch all class for tests that not naturally fit anywhere else.
 */
public class SyncedRealmTests extends BaseIntegrationTest {

    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Rule
    public final TestSyncConfigurationFactory configurationFactory = new TestSyncConfigurationFactory();

    @Test
    @UiThreadTest
    public void waitForInitialRemoteData_mainThreadThrows() {
        final SyncUser user = loginUser();

        SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .waitForInitialRemoteData()
                .build();

        Realm realm = null;
        try {
            realm = Realm.getInstance(config);
            fail();
        } catch (IllegalStateException ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // Login user on a worker thread, so this method can be used from both UI and non-ui threads.
    @NonNull
    private SyncUser loginUser() {
        final CountDownLatch userReady = new CountDownLatch(1);
        final AtomicReference<SyncUser> user = new AtomicReference<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
                user.set(SyncUser.login(credentials, Constants.AUTH_URL));
                userReady.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(userReady);
        return user.get();
    }

    @Test
    public void waitForInitialRemoteData() {
        // TODO We can improve this test once we got Sync Progress Notifications. Right now we cannot detect
        // when a Realm has been uploaded.
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .waitForInitialRemoteData()
                .build();

        Realm realm = null;
        try {
            realm = Realm.getInstance(config);
            assertTrue(realm.isEmpty());
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // This tests will start and cancel getting a Realm 10 times. The Realm should be resilient towards that
    // We cannot do much better since we cannot control the order of events internally in Realm which would be
    // needed to correctly test all error paths.
    @Test
    public void waitForInitialData_resilientInCaseOfRetries() throws InterruptedException {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        final SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .waitForInitialRemoteData()
                .build();

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Realm realm = null;
                    try {
                        // This will cause the download latch called later to immediately throw an InterruptedException.
                        Thread.currentThread().interrupt();
                        realm = Realm.getInstance(config);
                    } catch (DownloadingRealmInterruptedException ignored) {
                        assertFalse(new File(config.getPath()).exists());
                    } finally {
                        if (realm != null) {
                            realm.close();
                            Realm.deleteRealm(config);
                        }
                    }
                }
            });
            t.start();
            t.join();
        }
    }

    // This tests will start and cancel getting a Realm 10 times. The Realm should be resilient towards that
    // We cannot do much better since we cannot control the order of events internally in Realm which would be
    // needed to correctly test all error paths.
    @Test
    @RunTestInLooperThread
    public void waitForInitialData_resilientInCaseOfRetriesAsync() {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        final SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .waitForInitialRemoteData()
                .build();
        Random randomizer = new Random();

        for (int i = 0; i < 10; i++) {
            RealmAsyncTask task = Realm.getInstanceAsync(config, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    fail();
                }

                @Override
                public void onError(Throwable exception) {
                    fail(exception.toString());
                }
            });
            SystemClock.sleep(randomizer.nextInt(5));
            task.cancel();
        }
        looperThread.testComplete();
    }

    @Test
    public void waitForInitialRemoteData_readOnlyTrue() {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(configOld);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < 10; i++) {
                    realm.createObject(StringOnly.class).setChars("Foo" + i);
                }
            }
        });
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(10));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(configOld);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes (pray it managed to do so within the time frame).
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL);
        final SyncConfiguration configNew = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .waitForInitialRemoteData()
                .readOnly()
                .schema(StringOnly.class)
                .build();
        assertFalse(configNew.realmExists());

        realm = Realm.getInstance(configNew);
        assertEquals(10, realm.where(StringOnly.class).count());
        realm.close();
        user.logout();
    }


    @Test
    public void waitForInitialRemoteData_readOnlyTrue_throwsIfWrongServerSchema() {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        final SyncConfiguration configNew = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .waitForInitialRemoteData()
                .readOnly()
                .schema(StringOnly.class)
                .build();
        assertFalse(configNew.realmExists());

        Realm realm = null;
        try {
            // This will fail, because the server Realm is completely empty and the Client is not allowed to write the
            // schema.
            realm = Realm.getInstance(configNew);
            fail();
        } catch (RealmMigrationNeededException ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
            user.logout();
        }
    }

    @Test
    public void waitForInitialRemoteData_readOnlyFalse_upgradeSchema() {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        final SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .waitForInitialRemoteData() // Not readonly so Client should be allowed to write schema
                .schema(StringOnly.class) // This schema should be written when opening the empty Realm.
                .schemaVersion(2)
                .build();
        assertFalse(config.realmExists());

        Realm realm = Realm.getInstance(config);
        try {
            assertEquals(0, realm.where(StringOnly.class).count());
        } finally {
            realm.close();
            user.logout();
        }
    }

}
