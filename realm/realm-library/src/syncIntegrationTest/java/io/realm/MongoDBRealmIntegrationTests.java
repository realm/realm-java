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
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.entities.AllTypes;
import io.realm.entities.StringOnly;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.internal.OsRealmConfig;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.log.RealmLogger;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Class for exploring the API for the new API when integrating with Realm Cloud (formerly Stitch).
 */
@RunWith(AndroidJUnit4.class)
public class MongoDBRealmIntegrationTests extends StandardIntegrationTest {


    @Test
    public void loginAndGetRealm() {

        RealmAppCredentials

                RealmAppUser
                RealmAppUserIdentity




// Open Question. What concepts are we teaching users on the website?
// My Understand so far:
//
// MongoDB Stitch (shorthand Stitch) being renamed to MongoDB Realm (shorthand Realm)
// MongoDB Realm is a serverless platform that makes it easy creating mobile/web apps backed by Atlas.
// Realm App(lication): A specific project created in MongoDB Realm.
// Realm Database: The local database
// Realm Sync: The synchronization protocol that synchronizes changes between devices and server.
// Others?
//
// Biggest concern:
// Just saying Realm seems to imply both the Cloud product and the local database.
// Not sure what to do about it though.

// Current concepts in Realm Java
io.realm.SyncConfiguration; // For opening a Realm
io.realm.SyncCredentials; // For logging in
io.realm.SyncUser; // For all user related stuff and permissions
io.realm.SyncUserInfo; // Result from SyncUser.retrieveUserInfo(identity)
io.realm.SyncManager; // For global sync settings
io.realm.SyncSession; // For Realm specific sync settings

// New concepts mentioned in Merge docs
RealmApp
RealmAppConfiguration;
RealmAppUserIdentity
RealmAppUser
RealmAppUserProfile // Why have User and UserProfile as separate structures?
RealmAppUser
RealmFunctions
RealmFCMPush
RealmMongoDBService,    // Is this StitchServiceClient in Stich
RealmMongoDBDatabase,   // Called MongoDatabase in JVM Driver. Is this RemoteMongoDatabase in Stich?
RealmMongoDBCollection  // Is this RemoteMongoCollection in Stitch?

// Especially the MongoDB interface is a bit confusing
// What exactly does these classes do?
// Is it a copy of the API found here? If so, it is a lot more than just these 3 classes?
// https://docs.mongodb.com/stitch-sdks/java/4/com/mongodb/stitch/android/services/mongodb/remote/package-summary.html
// Why the DB suffix?
// Package name used com.mongodb.stitch.android.services.mongodb.remote

// Java Design criteria
// 1. Names should accurately reflect the concept they cover.
// 2. Names should feel idiomatic on Java. E.g use package names for scope instead of Prefixing everything
// 3. Consistency in naming, e.g prefixes or suffixes.
// 4. Minimize breaking existing customers.
// 5. Implementation should be easy, i.e. hiding implementation details using package protected methods.

// Some suggestions:
// RealmApp<X>: Two words feels problematic. To long?
// Sync<XXX>: Doesn't correctly reflect the product?
// <Nothing>: We already have precedence with things like RealmResults, RealmQuery. No boundary between Sync/Non-sync classes?
// MongoDBRealm: Just no...

// Suggestion 1:
// Use RealmApp (even if a bit long) as prefix for most relevant classes.
// Move MongoDB classes to seperate package to keep them isolated since it is a secondary API
// Everything is at io.realm package which is what we currently do and will make it easier
// to hide implementation details.
// Annoying: Why isn't it RealmAppFunctions? (well it sounds bad, but breaks consistency).
io.realm.RealmApp;
io.realm.RealmAppConfiguration;
io.realm.RealmAppCredentials;
io.realm.RealmAppUser;
io.realm.RealmAppUserIdentity;
io.realm.RealmFunctions
io.realm.RealmFCMPushNotifications;
io.realm.SyncConfiguration;
io.realm.SyncSession;
io.realm.mongodb.MongoDBService;    // Conflict with existing MongoDB driver/stitch API's?
io.realm.mongodb.MongoDBDatabase;
io.realm.mongodb.MongoDBCollection;

// Suggestion 2:
// Move "App" classes to 'app' package name instead of part of the class name.
// Only exception is "RealmApp" because it wouldn't be called RealmAppApp either
// Will make it clearer which classes are relevant to the synced use-case.
// Annoying: SyncConfiguration/SyncSession are currently in `io.realm`. Should they move?
io.realm.app.RealmApp; // Might be problematic to create sessions from here
io.realm.app.RealmAppConfiguration;
io.realm.app.RealmCredentials;
io.realm.app.RealmUser;
io.realm.app.UserIdentity;
io.realm.app.RealmFunctions;
io.realm.app.RealmFCMPushNotifications;
io.realm.SyncConfiguration; // Move to `io.realm.app`?
io.realm.SyncSession; // Move to `io.realm.app`?
io.realm.mongodb.MongoDBService;
io.realm.mongodb.MongoDBDatabase;
io.realm.mongodb.MongoDBCollection;

// Suggestion 3:
// Mixing the two approaches. Keeps everything primary in "io.realm"
// Removes "App" from all but RealmApp to keep noise down (kinda subjective)
// Keeps the pattern of Jus
// Annoying: Makes it unclear what is "cloud"-functionality and what is not? Maybe it doesn't
// matter since Sync is always an additive addon, so local database users will never see
// these classes anyway.
io.realm.RealmApp;
io.realm.RealmAppConfiguration;
io.realm.RealmCredentials;
io.realm.RealmUser;
io.realm.RealmUserIdentity;
io.realm.RealmFunctions
io.realm.RealmFCMPushNotifications;
io.realm.SyncConfiguration;
io.realm.SyncSession;
io.realm.mongodb.MongoDBService;    // Conflict with existing MongoDB driver/stitch API's?
io.realm.mongodb.MongoDBDatabase;
io.realm.mongodb.MongoDBCollection;



        // Login screen
        SyncCredentials credentials = SyncCredentials.anonymous();
        SyncUser user = RealmApp.login(credentials);

        // Returns the default Realm for the current active user
        // Problem: For anything beyond trivial use, this is pointless
        // D
        Realm.getInstance(user.getDefaultConfiguration());

        Realm.getI
        user.create
        RealmApp.
        RealmApp.getDefaultConfiguration()


        // Creating a


    }




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
            // closed for now https://github.com/realm/realm-java/issues/5416
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
        } catch (IllegalStateException ignore) {
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
        } catch (IllegalStateException ignored) {
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
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "test", true);
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
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "test", true);

        AtomicBoolean headerSet = new AtomicBoolean(false);
        RealmLog.setLevel(LogLevel.ALL);
        RealmLogger logger = (level, tag, throwable, message) -> {
            if (level == LogLevel.TRACE
                    && message.contains("Foo: bar")
                    && message.contains("RealmAuth: ")) {
                headerSet.set(true);
            }
        };
        looperThread.runAfterTest(() -> {
            RealmLog.remove(logger);
        });
        RealmLog.add(logger);

        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        try {
            user.changePassword("foo");
        } catch (ObjectServerError e) {
            if (e.getErrorCode() != ErrorCode.INVALID_CREDENTIALS) {
                throw e;
            }
        }

        assertTrue(headerSet.get());
        looperThread.testComplete();
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
        SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), "test", true);
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

        AtomicBoolean headersSet = new AtomicBoolean(false);
        RealmLog.setLevel(LogLevel.ALL);
        RealmLogger logger = (level, tag, throwable, message) -> {
            if (tag.equals("REALM_SYNC")
                    && message.contains("GET /foo/%2Fdefault%2F__partial%")
                    && message.contains("TestAuth: Realm-Access-Token version=1")
                    && message.contains("Test: test")) {
                looperThread.testComplete();
            }
        };
        looperThread.runAfterTest(() -> {
            RealmLog.remove(logger);
        });
        RealmLog.add(logger);
        Realm realm = Realm.getInstance(config);
        looperThread.closeAfterTest(realm);
    }


    /**
     * Tests https://github.com/realm/realm-java/issues/6235
     * This checks that the INITIAL callback is called for query-based notifications even when
     * the device is offline.
     */
    @Test
    @RunTestInLooperThread
    public void listenersTriggerWhenOffline() {
        SyncUser user = SyncTestUtils.createTestUser(); // Creating a fake user will make it behave as "offline"
        String url = "http://foo.com/offlineListeners";
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, url)
                .build();
        Realm realm = Realm.getInstance(config);
        looperThread.closeAfterTest(realm);

        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();

        looperThread.keepStrongReference(results);
        results.addChangeListener((objects, changeSet) -> {
            if(changeSet.getState() == OrderedCollectionChangeSet.State.INITIAL) {
                assertTrue(results.isLoaded());
                assertFalse(changeSet.isCompleteResult());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void progressListenersWorkWhenUsingWaitForInitialRemoteData() throws InterruptedException {
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
        assertTrue(SyncManager.getAllSessions(user).isEmpty());

        // 2. Local state should now be completely reset. Open the same sync Realm but different local name again with
        // a new configuration which should download the uploaded changes (pray it managed to do so within the time frame).
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = user.createConfiguration(Constants.USER_REALM)
                .name("newRealm")
                .fullSynchronization()
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .build();
        assertFalse(config.realmExists());
        AtomicBoolean indefineteListenerComplete = new AtomicBoolean(false);
        AtomicBoolean currentChangesListenerComplete = new AtomicBoolean(false);
        RealmAsyncTask task = Realm.getInstanceAsync(config, new Realm.Callback() {

            @Override
            public void onSuccess(Realm realm) {
                realm.close();
                if (!indefineteListenerComplete.get()) {
                    fail("Indefinete progress listener did not report complete.");
                }
                if (!currentChangesListenerComplete.get()) {
                    fail("Current changes progress listener did not report complete.");
                }
                looperThread.testComplete();
            }

            @Override
            public void onError(Throwable exception) {
                fail(exception.toString());
            }
        });
        looperThread.keepStrongReference(task);
        SyncManager.getSession(config).addDownloadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    indefineteListenerComplete.set(true);
                }
            }
        });
        SyncManager.getSession(config).addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    currentChangesListenerComplete.set(true);
                }
            }
        });
    }
}
