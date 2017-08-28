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

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.entities.StringOnly;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.rule.RunTestInLooperThread;
import io.realm.util.SyncTestUtils;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Catch all class for tests that not naturally fit anywhere else.
 */
@RunWith(AndroidJUnit4.class)
public class SyncedRealmTests extends BaseIntegrationTest {

    @Test
    @UiThreadTest
    public void waitForInitialRemoteData_mainThreadThrows() {
        final SyncUser user = SyncTestUtils.createTestUser(Constants.AUTH_URL);
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

    @Test
    public void waitForInitialRemoteData() {
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
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
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

    // Re-opening the backup Realm after a client reset online should trigger a client reset again
    // since the sync history will diverge.
    @Test
    @RunTestInLooperThread
    public void errorHandler_reopenClientResetRealmFail() {
        String userId = UUID.randomUUID().toString();
        SyncCredentials credentials = SyncCredentials.usernamePassword(userId, "password", true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);

        final SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.AUTH_URL)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        if (error.getErrorCode() != ErrorCode.CLIENT_RESET) {
                            fail("Wrong error " + error.toString());
                            return;
                        }

                        final ClientResetRequiredError handler = (ClientResetRequiredError) error;
                        // Execute Client Reset
                        looperThread.closeTestRealms();
                        handler.executeClientReset();

                        // Validate that files have been moved
                        assertFalse(handler.getOriginalFile().exists());
                        assertTrue(handler.getBackupFile().exists());

                        SyncConfiguration configuration = configurationFactory.createSyncConfigurationBuilder(user, Constants.AUTH_URL)
                                .errorHandler(new SyncSession.ErrorHandler() {
                                    @Override
                                    public void onError(SyncSession session, ObjectServerError error) {
                                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ERROR " + error.getErrorMessage());
                                        looperThread.testComplete();
                                    }
                                }).modules(new StringOnlyModule())
                                .build();
                        Realm instance = Realm.getInstance(configuration);
                        assertFalse(instance.isEmpty());

                        String backupFile = handler.getBackupFile().getAbsolutePath();
                        String name = handler.getBackupFile().getName();
                        String parent = handler.getBackupFile().getParent();
                        SyncConfiguration syncConfiguration = new SyncConfiguration
                                .Builder(user, Constants.AUTH_URL)
                                .name(name)
                                .directory(new File(parent))
                                .errorHandler(new SyncSession.ErrorHandler() {
                                    @Override
                                    public void onError(SyncSession session, ObjectServerError error) {
                                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ERROR " + error.getErrorMessage());
//                                        looperThread.testComplete();
                                    }
                                }).build();
//                        Realm realm = Realm.getInstance(syncConfiguration);
//                        System.out.println(">>>>>>>>>>>>>>>>>> GOT REALM ");
//                        configFactory.createSyncConfigurationBuilder(user, url);
//
//                        // this SyncConf doesn't specify any module, it will throw a migration required
//                        // exception since the backup Realm contain only StringOnly table
//                        SyncConfiguration backupSyncConfiguration = SyncConfiguration.forOffline(backupFile);
//
//                        try {
//                            Realm.getInstance(backupSyncConfiguration);
//                            fail("Expected to throw a Migration required");
//                        } catch (IllegalStateException expected) {
//                        }
//
//                        // opening a DynamicRealm will work though
//                        DynamicRealm dynamicRealm = DynamicRealm.getInstance(backupSyncConfiguration);
//
//                        dynamicRealm.getSchema().checkHasTable(StringOnly.CLASS_NAME, "Dynamic Realm should contains " + StringOnly.CLASS_NAME);
//                        RealmResults<DynamicRealmObject> all = dynamicRealm.where(StringOnly.CLASS_NAME).findAll();
//                        assertEquals(1, all.size());
//                        assertEquals("Foo", all.first().getString(StringOnly.FIELD_CHARS));
//                        // can write to it
//                        dynamicRealm.beginTransaction();
//                        dynamicRealm.createObject(StringOnly.CLASS_NAME).set(StringOnly.FIELD_CHARS, "Bar");
//                        dynamicRealm.commitTransaction();
//                        dynamicRealm.close();
//
//                        try {
//                            SyncConfiguration.forOffline(backupFile, StringOnly.class);
//                            fail("Expected to throw java.lang.Class is not a RealmModule");
//                        } catch (IllegalArgumentException expected) {
//                        }

//                        // specifying the module will allow to open the typed Realm
//                        backupSyncConfiguration = SyncConfiguration.forOffline(backupFile, new StringOnlyModule());
//                        Realm backupRealm = Realm.getInstance(backupSyncConfiguration);
//                        assertFalse(backupRealm.isEmpty());
//                        assertEquals(2, backupRealm.where(StringOnly.class).count());
//                        RealmResults<StringOnly> allSorted = backupRealm.where(StringOnly.class).findAllSorted(StringOnly.FIELD_CHARS);
//                        assertEquals("Bar", allSorted.get(0).getChars());
//                        assertEquals("Foo", allSorted.get(1).getChars());
//                        backupRealm.close();


                    }
                })
                .modules(new StringOnlyModule())
                .build();

        Realm realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        looperThread.addTestRealm(realm);

        // Trigger error
        SyncManager.simulateClientReset(SyncManager.getSession(config));
    }


}
