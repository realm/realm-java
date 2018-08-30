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
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Random;
import java.util.UUID;

import io.realm.entities.StringOnly;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.OsRealmConfig;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Catch all class for tests that not naturally fit anywhere else.
 */
@RunWith(AndroidJUnit4.class)
public class SyncedRealmIntegrationTests extends StandardIntegrationTest {

    @Test
    @RunTestInLooperThread
    public void loginLogoutResumeSyncing() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        SyncConfiguration config = user.createConfiguration(Constants.USER_REALM)
                .schema(StringOnly.class)
                .fullSynchronization()
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build();

        Realm realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();
        SyncManager.getSession(config).uploadAllLocalChanges();
        user.logOut();
        realm.close();
        try {
            assertTrue(Realm.deleteRealm(config));
        } catch (IllegalStateException e) {
            // FIXME: We don't have a way to ensure that the Realm instance on client thread has been
            //        closed for now.
            // https://github.com/realm/realm-java/issues/5416
            if (e.getMessage().contains("It's not allowed to delete the file")) {
                // retry after 1 second
                SystemClock.sleep(1000);
                assertTrue(Realm.deleteRealm(config));
            }
        }

        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL);
        SyncConfiguration config2 = user.createConfiguration(Constants.USER_REALM)
                .fullSynchronization()
                .schema(StringOnly.class)
                .build();

        Realm realm2 = Realm.getInstance(config2);
        SyncManager.getSession(config2).downloadAllServerChanges();
        realm2.refresh();
        assertEquals(1, realm2.where(StringOnly.class).count());
        realm2.close();
        looperThread.testComplete();
    }

    @Test
    @UiThreadTest
    public void waitForInitialRemoteData_mainThreadThrows() {
        final SyncUser user = SyncTestUtils.createTestUser(Constants.AUTH_URL);
        SyncConfiguration config = user.createConfiguration(Constants.USER_REALM)
                .fullSynchronization()
                .waitForInitialRemoteData()
                .build();

        Realm realm = null;
        try {
            realm = Realm.getInstance(config);
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString(
                    "downloadAllServerChanges() cannot be called from the main thread."));
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Test
    public void waitForInitialRemoteData() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
                .schema(StringOnly.class)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
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
        SyncManager.getSession(configOld).uploadAllLocalChanges();
        realm.close();
        user.logOut();

        // 2. Local state should now be completely reset. Open the same sync Realm but different local name again with
        // a new configuration which should download the uploaded changes (pray it managed to do so within the time frame).
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = user.createConfiguration(Constants.USER_REALM)
                .name("newRealm")
                .fullSynchronization()
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .build();

        realm = Realm.getInstance(config);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < 10; i++) {
                    realm.createObject(StringOnly.class).setChars("Foo 1" + i);
                }
            }
        });
        try {
            assertEquals(20, realm.where(StringOnly.class).count());
        } finally {
            realm.close();
        }
    }

    // This tests will start and cancel getting a Realm 10 times. The Realm should be resilient towards that
    // We cannot do much better since we cannot control the order of events internally in Realm which would be
    // needed to correctly test all error paths.
    @Test
    @Ignore("Sync somehow keeps a Realm alive, causing the Realm.deleteRealm to throw " +
            " https://github.com/realm/realm-java/issues/5416")
    public void waitForInitialData_resilientInCaseOfRetries() throws InterruptedException {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        final SyncConfiguration config = user.createConfiguration(Constants.USER_REALM)
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
    @Ignore("See https://github.com/realm/realm-java/issues/5373")
    public void waitForInitialData_resilientInCaseOfRetriesAsync() {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        final SyncConfiguration config = user.createConfiguration(Constants.USER_REALM)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .directory(configurationFactory.getRoot())
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
    public void waitForInitialRemoteData_readOnlyTrue() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server (and pray it does it within 10 seconds)
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
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
        SyncManager.getSession(configOld).uploadAllLocalChanges();
        realm.close();
        user.logOut();

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes (pray it managed to do so within the time frame).
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL);
        final SyncConfiguration configNew = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .name("newRealm")
                .fullSynchronization()
                .waitForInitialRemoteData()
                .readOnly()
                .schema(StringOnly.class)
                .build();
        assertFalse(configNew.realmExists());

        realm = Realm.getInstance(configNew);
        assertEquals(10, realm.where(StringOnly.class).count());
        realm.close();
        user.logOut();
    }
    
    @Test
    public void waitForInitialRemoteData_readOnlyTrue_throwsIfWrongServerSchema() {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
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
            user.logOut();
        }
    }

    @Test
    public void waitForInitialRemoteData_readOnlyFalse_upgradeSchema() {
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "password", true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
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
            user.logOut();
        }
    }

    @Test
    public void defaultRealm() throws InterruptedException {
        SyncCredentials credentials = SyncCredentials.nickname("test", false);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        SyncConfiguration config = user.getDefaultConfiguration();
        Realm realm = Realm.getInstance(config);
        SyncManager.getSession(config).downloadAllServerChanges();
        realm.refresh();

        try {
            assertTrue(realm.isEmpty());
        } finally {
            realm.close();
            user.logOut();
        }
    }

    // Check that custom headers and auth header renames are correctly used for HTTP requests
    // performed from Java.
    @Test
    @RunTestInLooperThread
    public void javaRequestCustomHeaders() {
        SyncManager.addCustomRequestHeader("Foo", "bar");
        SyncManager.setAuthorizationHeaderName("RealmAuth");
        runJavaRequestCustomHeadersTest();
    }

    // Check that custom headers and auth header renames are correctly used for HTTP requests
    // performed from Java.
    @Test
    @RunTestInLooperThread
    public void javaRequestCustomHeaders_specificHost() {
        SyncManager.addCustomRequestHeader("Foo", "bar", Constants.HOST);
        SyncManager.setAuthorizationHeaderName("RealmAuth", Constants.HOST);
        runJavaRequestCustomHeadersTest();
    }

    private void runJavaRequestCustomHeadersTest() {
        SyncCredentials credentials = SyncCredentials.nickname("test", false);

        RealmLog.setLevel(LogLevel.ALL);
        RealmLog.add((level, tag, throwable, message) -> {
            if (level == LogLevel.TRACE
                    && message.contains("Foo: bar")
                    && message.contains("RealmAuth: ")) {
                looperThread.testComplete();
            }});

        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        try {
            user.changePassword("foo");
        } catch (ObjectServerError e) {
            if (e.getErrorCode() != ErrorCode.INVALID_CREDENTIALS) {
                throw e;
            }
        }
    }

    // Test that auth header renaming, custom headers and url prefix are all propagated correctly
    // to Sync. There really isn't a way to create a proper integration test since ROS used for testing
    // isn't configured to accept such requests. Instead we inspect the log from Sync which will
    // output the headers in TRACE mode.
    @Test
    @RunTestInLooperThread
    public void syncAuthHeaderAndUrlPrefix() {
        SyncManager.setAuthorizationHeaderName("TestAuth");
        SyncManager.addCustomRequestHeader("Test", "test");
        runSyncAuthHeadersAndUrlPrefixTest();
    }

    // Test that auth header renaming, custom headers and url prefix are all propagated correctly
    // to Sync. There really isn't a way to create a proper integration test since ROS used for testing
    // isn't configured to accept such requests. Instead we inspect the log from Sync which will
    // output the headers in TRACE mode.
    @Test
    @RunTestInLooperThread
    public void syncAuthHeaderAndUrlPrefix_specificHost() {
        SyncManager.setAuthorizationHeaderName("TestAuth", Constants.HOST);
        SyncManager.addCustomRequestHeader("Test", "test", Constants.HOST);
        runSyncAuthHeadersAndUrlPrefixTest();
    }

    private void runSyncAuthHeadersAndUrlPrefixTest() {
        SyncCredentials credentials = SyncCredentials.nickname("test", false);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM)
                .urlPrefix("/foo")
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        RealmLog.error(error.toString());
                    }
                })
                .build();

        RealmLog.setLevel(LogLevel.ALL);
        RealmLog.add((level, tag, throwable, message) -> {
            if (tag.equals("REALM_SYNC")
                    && message.contains("GET /foo/%2Fdefault%2F__partial%")
                    && message.contains("TestAuth: Realm-Access-Token version=1")
                    && message.contains("Test: test")) {
                looperThread.testComplete();
            }
        });
        Realm realm = Realm.getInstance(config);
        looperThread.closeAfterTest(realm);
    }

}
