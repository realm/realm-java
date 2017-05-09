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

package io.realm.objectserver;

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
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

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
            final int iteration = i;
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
}
