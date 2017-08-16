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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import io.realm.BaseIntegrationTest;
import io.realm.Progress;
import io.realm.ProgressListener;
import io.realm.ProgressMode;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.entities.AllTypes;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ProgressListenerTests extends BaseIntegrationTest {

    private static final long TEST_SIZE = 10;
    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Nonnull
    private SyncConfiguration createSyncConfig() {
        SyncUser user = UserFactory.createAdminUser(Constants.AUTH_URL);
        return configFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL).build();
    }

    private void writeSampleData(Realm realm) {
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            AllTypes obj = realm.createObject(AllTypes.class);
            obj.setColumnString("Object " + i);
        }
        realm.commitTransaction();
    }

    private void assertTransferComplete(Progress progress, boolean nonZeroChange) {
        assertTrue(progress.isTransferComplete());
        assertEquals(1.0D, progress.getFractionTransferred(), 0.0D);
        assertEquals(progress.getTransferableBytes(), progress.getTransferredBytes());
        if (nonZeroChange) {
            assertTrue(progress.getTransferredBytes() > 0);
        }
    }

    // Create remote data for a given user.
    private URI createRemoteData(SyncUser user) {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM).build();
        final Realm realm = Realm.getInstance(config);
        writeSampleData(realm);
        final CountDownLatch changesUploaded = new CountDownLatch(1);
        final SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    session.removeProgressListener(this);
                    changesUploaded.countDown();
                }
            }
        });
        TestHelper.awaitOrFail(changesUploaded);
        realm.close();
        return config.getServerUrl();
    }

    @Test
    public void downloadProgressListener_changesOnly() {
        final CountDownLatch allChangesDownloaded = new CountDownLatch(1);
        SyncUser userWithData = UserFactory.createUniqueUser(Constants.AUTH_URL);
        URI serverUrl = createRemoteData(userWithData);
        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);

        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(adminUser, serverUrl.toString()).build();
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    assertTransferComplete(progress, true);
                    Realm realm = Realm.getInstance(config);
                    assertEquals(TEST_SIZE, realm.where(AllTypes.class).count());
                    realm.close();
                    allChangesDownloaded.countDown();
                }
            }
        });
        TestHelper.awaitOrFail(allChangesDownloaded);
        realm.close();
        userWithData.logout();
        adminUser.logout();
    }

    @Test
    public void downloadProgressListener_indefinitely() throws InterruptedException {
        final AtomicInteger transferCompleted = new AtomicInteger(0);
        final CountDownLatch allChangesDownloaded = new CountDownLatch(1);
        final CountDownLatch startWorker = new CountDownLatch(1);
        final SyncUser userWithData = UserFactory.createUniqueUser(Constants.AUTH_URL);

        URI serverUrl = createRemoteData(userWithData);

        // Create worker thread that puts data into another Realm.
        // This is to avoid blocking one progress listener while waiting for another to complete.
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                TestHelper.awaitOrFail(startWorker);
                createRemoteData(userWithData);
            }
        });
        worker.start();

        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        final SyncConfiguration adminConfig = configFactory.createSyncConfigurationBuilder(adminUser, serverUrl.toString()).build();
        Realm adminRealm = Realm.getInstance(adminConfig);
        Realm userRealm = Realm.getInstance(configFactory.createSyncConfigurationBuilder(userWithData, Constants.USER_REALM).build()); // Keep session alive
        SyncSession session = SyncManager.getSession(adminConfig);
        session.addDownloadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    switch (transferCompleted.incrementAndGet()) {
                        case 1:
                            // Initial trigger when registering
                            assertTransferComplete(progress, false);
                            break;
                        case 2: {
                            assertTransferComplete(progress, true);
                            Realm adminRealm = Realm.getInstance(adminConfig);
                            assertEquals(TEST_SIZE, adminRealm.where(AllTypes.class).count());
                            adminRealm.close();
                            startWorker.countDown();
                            break;
                        }
                        case 3: {
                            assertTransferComplete(progress, true);
                            Realm adminRealm = Realm.getInstance(adminConfig);
                            assertEquals(TEST_SIZE * 2, adminRealm.where(AllTypes.class).count());
                            adminRealm.close();
                            allChangesDownloaded.countDown();
                            break;
                        }
                        default:
                            fail();
                    }
                }
            }
        });
        TestHelper.awaitOrFail(allChangesDownloaded);
        adminRealm.close();
        userRealm.close();
        userWithData.logout();
        adminUser.logout();
        worker.join();
    }

    // Make sure that a ProgressListener continues to report the correct thing, even if it crashed
    @Test
    public void uploadListener_worksEvenIfCrashed() throws InterruptedException {
        final AtomicInteger transferCompleted = new AtomicInteger(0);
        final CountDownLatch testDone = new CountDownLatch(1);
        final SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);

        writeSampleData(realm); // Write first batch of sample data
        SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    switch(transferCompleted.incrementAndGet()) {
                        case 1:
                            Realm realm = Realm.getInstance(config);
                            writeSampleData(realm);
                            realm.close();
                            throw new RuntimeException("Crashing the changelistener");
                        case 2:
                            assertTransferComplete(progress, true);
                            testDone.countDown();
                            break;
                        default:
                            fail("Unsupported number of transfers completed: " + transferCompleted.get());
                    }
                }
            }
        });

        TestHelper.awaitOrFail(testDone);
        realm.close();
    }


    @Test
    public void uploadProgressListener_changesOnly() {
        final CountDownLatch allChangeUploaded = new CountDownLatch(1);
        SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);
        writeSampleData(realm);

        SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    assertTransferComplete(progress, true);
                    allChangeUploaded.countDown();
                }
            }
        });

        TestHelper.awaitOrFail(allChangeUploaded);
        realm.close();
    }

    @Test
    public void uploadProgressListener_indefinitely() {
        final AtomicInteger transferCompleted = new AtomicInteger(0);
        final CountDownLatch testDone = new CountDownLatch(1);
        final SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);

        writeSampleData(realm); // Write first batch of sample data
        SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    switch(transferCompleted.incrementAndGet()) {
                        case 1:
                            Realm realm = Realm.getInstance(config);
                            writeSampleData(realm);
                            realm.close();
                            break;
                        case 2:
                            assertTransferComplete(progress, true);
                            testDone.countDown();
                            break;
                        default:
                            fail("Unsupported number of transfers completed: " + transferCompleted.get());
                    }
                }
            }
        });

        TestHelper.awaitOrFail(testDone);
        realm.close();
    }

    @Test
    public void addListenerInsideCallback() {
        final CountDownLatch allChangeUploaded = new CountDownLatch(1);
        final SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);
        writeSampleData(realm);

        final SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    Realm realm = Realm.getInstance(config);
                    writeSampleData(realm);
                    realm.close();
                    session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
                        @Override
                        public void onChange(Progress progress) {
                            if (progress.isTransferComplete()) {
                                allChangeUploaded.countDown();
                            }
                        }
                    });
                }
            }
        });

        TestHelper.awaitOrFail(allChangeUploaded);
        realm.close();
    }

    @Test
    public void addListenerInsideCallback_mixProgressModes() {
        final CountDownLatch allChangeUploaded = new CountDownLatch(3);
        final AtomicBoolean progressCompletedReported = new AtomicBoolean(false);
        final SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);
        writeSampleData(realm);

        final SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    allChangeUploaded.countDown();
                    if (progressCompletedReported.compareAndSet(false, true)) {
                        Realm realm = Realm.getInstance(config);
                        writeSampleData(realm);
                        realm.close();
                        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
                            @Override
                            public void onChange(Progress progress) {
                                if (progress.isTransferComplete()) {
                                    allChangeUploaded.countDown();
                                }
                            }
                        });
                    }
                }
            }
        });

        TestHelper.awaitOrFail(allChangeUploaded);
        realm.close();
    }

    @Test
    public void addProgressListener_triggerImmediatelyWhenRegistered() {
        final SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);

        checkListener(session, ProgressMode.INDEFINITELY);
        checkListener(session, ProgressMode.CURRENT_CHANGES);

        realm.close();
    }

    @Test
    public void uploadListener_keepIncreasingInSize() {
        SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        for (int i = 0; i < 10; i++) {
            final CountDownLatch changesUploaded = new CountDownLatch(1);
            writeSampleData(realm);
            final int testNo = i;
            session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
                @Override
                public void onChange(Progress progress) {
                    RealmLog.info("Test %s -> %s", Integer.toString(testNo), progress.toString());
                    if (progress.isTransferComplete()) {
                        assertTransferComplete(progress, true);
                        changesUploaded.countDown();
                    }
                }
            });
            TestHelper.awaitOrFail(changesUploaded);
        }

        realm.close();
    }

    private void checkListener(SyncSession session, ProgressMode progressMode) {
        final CountDownLatch listenerCalled = new CountDownLatch(1);
        session.addDownloadProgressListener(progressMode, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                listenerCalled.countDown();
            }
        });
        TestHelper.awaitOrFail(listenerCalled);
    }

}
