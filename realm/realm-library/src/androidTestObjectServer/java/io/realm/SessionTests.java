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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.StringOnly;
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

                        SyncConfiguration backupSyncConfiguration = handler.getBackupSyncConfiguration();
                        assertNotNull(backupSyncConfiguration);
                        assertTrue(backupSyncConfiguration.isSyncConfiguration());
                        assertTrue(backupSyncConfiguration.isRealmOffline());

                        Realm backupRealm = Realm.getInstance(backupSyncConfiguration);
                        assertFalse(backupRealm.isEmpty());
                        assertEquals(1, backupRealm.where(StringOnly.class).count());
                        assertEquals("Foo", backupRealm.where(StringOnly.class).findAll().first().getChars());
                        backupRealm.close();

                        // opening a Dynamic Realm should also work
                        DynamicRealm dynamicRealm = DynamicRealm.getInstance(backupSyncConfiguration);
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
                        SyncConfiguration backupSyncConfiguration = SyncConfiguration.forOffline(backupFile);

                        try {
                            Realm.getInstance(backupSyncConfiguration);
                            fail("Expected to throw a Migration required");
                        } catch (RealmMigrationNeededException expected) {
                        }

                        // opening a DynamicRealm will work though
                        DynamicRealm dynamicRealm = DynamicRealm.getInstance(backupSyncConfiguration);

                        dynamicRealm.getSchema().checkHasTable(StringOnly.CLASS_NAME, "Dynamic Realm should contains " + StringOnly.CLASS_NAME);
                        RealmResults<DynamicRealmObject> all = dynamicRealm.where(StringOnly.CLASS_NAME).findAll();
                        assertEquals(1, all.size());
                        assertEquals("Foo", all.first().getString(StringOnly.FIELD_CHARS));
                        // can write to it
                        dynamicRealm.beginTransaction();
                        dynamicRealm.createObject(StringOnly.CLASS_NAME).set(StringOnly.FIELD_CHARS, "Bar");
                        dynamicRealm.commitTransaction();
                        dynamicRealm.close();

                        try {
                            SyncConfiguration.forOffline(backupFile, StringOnly.class);
                            fail("Expected to throw java.lang.Class is not a RealmModule");
                        } catch (IllegalArgumentException expected) {
                        }

                        // specifying the module will allow to open the typed Realm
                        backupSyncConfiguration = SyncConfiguration.forOffline(backupFile, new StringOnlyModule());
                        Realm backupRealm = Realm.getInstance(backupSyncConfiguration);
                        assertFalse(backupRealm.isEmpty());
                        assertEquals(2, backupRealm.where(StringOnly.class).count());
                        RealmResults<StringOnly> allSorted = backupRealm.where(StringOnly.class).findAllSorted(StringOnly.FIELD_CHARS);
                        assertEquals("Bar", allSorted.get(0).getChars());
                        assertEquals("Foo", allSorted.get(1).getChars());
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

    @Test
    @UiThreadTest
    public void uploadAllLocalChanges_throwsOnUiThread() throws InterruptedException {
        SyncUser user = createTestUser();
        Realm realm = Realm.getInstance(configuration);
        try {
            SyncManager.getSession(configuration).uploadAllLocalChanges();
        } catch (IllegalStateException ignored) {
        } finally {
            realm.close();
        }
    }

    @Test
    @UiThreadTest
    public void downloadAllServerChanges_throwsOnUiThread() throws InterruptedException {
        SyncUser user = createTestUser();
        Realm realm = Realm.getInstance(configuration);
        try {
            SyncManager.getSession(configuration).downloadAllServerChanges();
        } catch (IllegalStateException ignored) {
        } finally {
            realm.close();
        }
    }
}
