/*
 * Copyright 2016 Realm Inc.
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

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestSyncConfigurationFactory;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SessionTests {

    private static String REALM_URI = "realm://objectserver.realm.io/~/default";

    private SyncConfiguration configuration;
    private SyncUser user;

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Before
    public void setUp() {
        user = createTestUser();
        configuration = new SyncConfiguration.Builder(user, REALM_URI).build();
    }

    @Test
    public void get_syncValues() {
        SyncSession session = new SyncSession(configuration);
        assertEquals("realm://objectserver.realm.io/" + user.getIdentity() + "/default", session.getServerUrl().toString());
        assertEquals(user, session.getUser());
        assertEquals(configuration, session.getConfiguration());
    }

    @Test
    public void addDownloadProgressListener_nullThrows() {
        SyncSession session = SyncManager.getSession(configuration);
        try {
            session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void addUploadProgressListener_nullThrows() {
        SyncSession session = SyncManager.getSession(configuration);
        try {
            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void removeProgressListener() {
        Realm realm = Realm.getInstance(configuration);
        SyncSession session = SyncManager.getSession(configuration);
        ProgressListener[] listeners = new ProgressListener[] {
                null,
                new ProgressListener() {
                    @Override
                    public void onChange(Progress progress) {
                        // Listener 1, not present
                    }
                },
                new ProgressListener() {
                    @Override
                    public void onChange(Progress progress) {
                        // Listener 2, present
                    }
                }
        };
        session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, listeners[2]);

        // Check that remove works unconditionally for all input
        for (ProgressListener listener : listeners) {
            session.removeProgressListener(listener);
        }
        realm.close();
    }

    // Check that a Client Reset is correctly reported.
    @Test
    @RunTestInLooperThread
    public void errorHandler_clientResetReported() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, url)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        if (error.getErrorCode() != ErrorCode.CLIENT_RESET) {
                            fail("Wrong error " + error.toString());
                            return;
                        }

                        final ClientResetRequiredError handler = (ClientResetRequiredError) error;
                        String filePathFromError = handler.getOriginalFile().getAbsolutePath();
                        String filePathFromConfig = session.getConfiguration().getPath();
                        assertEquals(filePathFromError, filePathFromConfig);
                        assertFalse(handler.getBackupFile().exists());
                        assertTrue(handler.getOriginalFile().exists());

                        looperThread.testComplete();
                    }
                })
                .build();

        Realm realm = Realm.getInstance(config);
        looperThread.addTestRealm(realm);

        // Trigger error
        SyncManager.simulateClientReset(SyncManager.getSession(config));
    }

    // Check that we can manually execute the Client Reset.
    @Test
    @RunTestInLooperThread
    public void errorHandler_manualExecuteClientReset() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, url)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        if (error.getErrorCode() != ErrorCode.CLIENT_RESET) {
                            fail("Wrong error " + error.toString());
                            return;
                        }

                        final ClientResetRequiredError handler = (ClientResetRequiredError) error;
                        try {
                            handler.executeClientReset();
                            fail("All Realms should be closed before executing Client Reset can be allowed");
                        } catch(IllegalStateException ignored) {
                        }

                        // Execute Client Reset
                        looperThread.closeTestRealms();
                        handler.executeClientReset();

                        // Validate that files have been moved
                        assertFalse(handler.getOriginalFile().exists());
                        assertTrue(handler.getBackupFile().exists());
                        looperThread.testComplete();
                    }
                })
                .build();

        Realm realm = Realm.getInstance(config);
        looperThread.addTestRealm(realm);

        // Trigger error
        SyncManager.simulateClientReset(SyncManager.getSession(config));
    }

    // Check that if we manually trigger a Client Reset, then it should be possible to start
    // downloading the Realm immediately after.
    @Test
    @RunTestInLooperThread
    @Ignore("https://github.com/realm/realm-java/issues/5143")
    public void clientReset_manualTriggerAllowSessionToRestart() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/~/myrealm";
        final AtomicReference<SyncConfiguration> configRef = new AtomicReference<>(null);
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user , url)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        final ClientResetRequiredError handler = (ClientResetRequiredError) error;

                        // Execute Client Reset
                        looperThread.closeTestRealms();
                        handler.executeClientReset();

                        // Try to re-open Realm and download it again
                        looperThread.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                // Validate that files have been moved
                                assertFalse(handler.getOriginalFile().exists());
                                assertTrue(handler.getBackupFile().exists());

                                SyncConfiguration config = configRef.get();
                                Realm instance = Realm.getInstance(config);
                                looperThread.addTestRealm(instance);
                                try {
                                    SyncManager.getSession(config).downloadAllServerChanges();
                                    looperThread.testComplete();
                                } catch (InterruptedException e) {
                                    fail(e.toString());
                                }
                            }
                        });
                    }
                })
                .build();
        configRef.set(config);

        Realm realm = Realm.getInstance(config);
        looperThread.addTestRealm(realm);

        // Trigger error
        SyncManager.simulateClientReset(SyncManager.getSession(config));
    }

    // Check that we can use the backup SyncConfiguration to open the Realm.
    @Test
    @RunTestInLooperThread
    public void errorHandler_useBackupSyncConfigurationForClientReset() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, url)
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

                        RealmConfiguration backupRealmConfiguration = handler.getBackupRealmConfiguration();
                        assertNotNull(backupRealmConfiguration);
                        assertFalse(backupRealmConfiguration.isSyncConfiguration());
                        assertTrue(backupRealmConfiguration.isRecoveryConfiguration());

                        Realm backupRealm = Realm.getInstance(backupRealmConfiguration);
                        assertFalse(backupRealm.isEmpty());
                        assertEquals(1, backupRealm.where(StringOnly.class).count());
                        assertEquals("Foo", backupRealm.where(StringOnly.class).findAll().first().getChars());
                        backupRealm.close();

                        // opening a Dynamic Realm should also work
                        DynamicRealm dynamicRealm = DynamicRealm.getInstance(backupRealmConfiguration);
                        dynamicRealm.getSchema().checkHasTable(StringOnly.CLASS_NAME, "Dynamic Realm should contains " + StringOnly.CLASS_NAME);
                        RealmResults<DynamicRealmObject> all = dynamicRealm.where(StringOnly.CLASS_NAME).findAll();
                        assertEquals(1, all.size());
                        assertEquals("Foo", all.first().getString(StringOnly.FIELD_CHARS));
                        dynamicRealm.close();
                        looperThread.testComplete();
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

    // Check that we can open the backup file without using the provided SyncConfiguration,
    // this might be the case if the user decide to act upon the client reset later (providing s/he
    // persisted the location of the file)
    @Test
    @RunTestInLooperThread
    public void errorHandler_useBackupSyncConfigurationAfterClientReset() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, url)
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

                        String backupFile = handler.getBackupFile().getAbsolutePath();

                        // this SyncConf doesn't specify any module, it will throw a migration required
                        // exception since the backup Realm contain only StringOnly table
                        RealmConfiguration backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile);

                        try {
                            Realm.getInstance(backupRealmConfiguration);
                            fail("Expected to throw a Migration required");
                        } catch (RealmMigrationNeededException expected) {
                        }

                        // opening a DynamicRealm will work though
                        DynamicRealm dynamicRealm = DynamicRealm.getInstance(backupRealmConfiguration);

                        dynamicRealm.getSchema().checkHasTable(StringOnly.CLASS_NAME, "Dynamic Realm should contains " + StringOnly.CLASS_NAME);
                        RealmResults<DynamicRealmObject> all = dynamicRealm.where(StringOnly.CLASS_NAME).findAll();
                        assertEquals(1, all.size());
                        assertEquals("Foo", all.first().getString(StringOnly.FIELD_CHARS));

                        // make sure we can't write to it (read-only Realm)
                        try {
                            dynamicRealm.beginTransaction();
                            fail("Can't perform transactions on read-only Realms");
                        } catch (IllegalStateException expected) {
                        }
                        dynamicRealm.close();

                        try {
                            SyncConfiguration.forRecovery(backupFile, null, StringOnly.class);
                            fail("Expected to throw java.lang.Class is not a RealmModule");
                        } catch (IllegalArgumentException expected) {
                        }

                        // specifying the module will allow to open the typed Realm
                        backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, null, new StringOnlyModule());
                        Realm backupRealm = Realm.getInstance(backupRealmConfiguration);
                        assertFalse(backupRealm.isEmpty());
                        assertEquals(1, backupRealm.where(StringOnly.class).count());
                        RealmResults<StringOnly> allSorted = backupRealm.where(StringOnly.class).findAll();
                        assertEquals("Foo", allSorted.get(0).getChars());
                        backupRealm.close();

                        looperThread.testComplete();
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

    // make sure the backup file Realm is encrypted with the same key as the original synced Realm.
    @Test
    @RunTestInLooperThread
    public void errorHandler_useClientResetEncrypted() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        final byte[] randomKey = TestHelper.getRandomKey();
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, url)
                .encryptionKey(randomKey)
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

                        RealmConfiguration backupRealmConfiguration = handler.getBackupRealmConfiguration();

                        // can open encrypted backup Realm
                        Realm backupEncryptedRealm = Realm.getInstance(backupRealmConfiguration);
                        assertEquals(1, backupEncryptedRealm.where(StringOnly.class).count());
                        RealmResults<StringOnly> allSorted = backupEncryptedRealm.where(StringOnly.class).findAll();
                        assertEquals("Foo", allSorted.get(0).getChars());
                        backupEncryptedRealm.close();

                        String backupFile = handler.getBackupFile().getAbsolutePath();
                        // build a conf to open a DynamicRealm
                        backupRealmConfiguration = SyncConfiguration.forRecovery(backupFile, randomKey, new StringOnlyModule());
                        backupEncryptedRealm = Realm.getInstance(backupRealmConfiguration);
                        assertEquals(1, backupEncryptedRealm.where(StringOnly.class).count());
                        allSorted = backupEncryptedRealm.where(StringOnly.class).findAll();
                        assertEquals("Foo", allSorted.get(0).getChars());
                        backupEncryptedRealm.close();

                        // using wrong key throw
                        try {
                            Realm.getInstance(SyncConfiguration.forRecovery(backupFile, TestHelper.getRandomKey(), new StringOnlyModule()));
                            fail("Expected to throw when using wrong encryption key");
                        } catch (RealmFileException expected) {
                        }

                        looperThread.testComplete();
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

    @Test
    @UiThreadTest
    public void uploadAllLocalChanges_throwsOnUiThread() throws InterruptedException {
        Realm realm = Realm.getInstance(configuration);
        try {
            SyncManager.getSession(configuration).uploadAllLocalChanges();
            fail("Should throw an IllegalStateException on Ui Thread");
        } catch (IllegalStateException ignored) {
        } finally {
            realm.close();
        }
    }

    @Test
    @UiThreadTest
    public void downloadAllServerChanges_throwsOnUiThread() throws InterruptedException {
        Realm realm = Realm.getInstance(configuration);
        try {
            SyncManager.getSession(configuration).downloadAllServerChanges();
            fail("Should throw an IllegalStateException on Ui Thread");
        } catch (IllegalStateException ignored) {
        } finally {
            realm.close();
        }
    }
}
