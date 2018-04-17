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
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import io.realm.Progress;
import io.realm.ProgressListener;
import io.realm.ProgressMode;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.entities.AllTypes;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ProgressListenerTests extends StandardIntegrationTest {

    private static final long TEST_SIZE = 10;
    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Nonnull
    private SyncConfiguration createSyncConfig() {
        SyncUser user = UserFactory.createAdminUser(Constants.AUTH_URL);
        return configFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .fullSynchronization()
                .build();
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
    private URI createRemoteData(final SyncConfiguration config) {
        final Realm realm = Realm.getInstance(config);
        final CountDownLatch changesUploaded = new CountDownLatch(1);
        final SyncSession session = SyncManager.getSession(config);
        final long beforeAdd = realm.where(AllTypes.class).count();
        writeSampleData(realm);

        final long threadId = Thread.currentThread().getId();

        session.addUploadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                // FIXME: This check is to make sure before this method returns, all the uploads has been done.
                // See https://github.com/realm/realm-object-store/issues/581#issuecomment-339353832
                if (threadId == Thread.currentThread().getId()) {
                    return;
                }
                if (progress.isTransferComplete()) {
                    Realm realm = Realm.getInstance(config);
                    final long afterAdd = realm.where(AllTypes.class).count();
                    realm.close();

                    RealmLog.warn(String.format(Locale.ENGLISH,"createRemoteData upload %d/%d objects count:%d",
                            progress.getTransferredBytes(), progress.getTransferableBytes(), afterAdd));
                    // FIXME: Remove this after https://github.com/realm/realm-object-store/issues/581
                    if (afterAdd == TEST_SIZE + beforeAdd) {
                        session.removeProgressListener(this);
                        changesUploaded.countDown();
                    } else if (afterAdd < TEST_SIZE + beforeAdd) {
                        fail("The added objects are more than expected.");
                    }
                }
            }
        });
        TestHelper.awaitOrFail(changesUploaded);
        realm.close();
        return config.getServerUrl();
    }

    private long getStoreTestDataSize(RealmConfiguration config) {
        Realm adminRealm = Realm.getInstance(config);
        long objectCounts = adminRealm.where(AllTypes.class).count();
        adminRealm.close();

        return objectCounts;
    }

    @Test
    public void downloadProgressListener_changesOnly() {
        final CountDownLatch allChangesDownloaded = new CountDownLatch(1);
        SyncUser userWithData = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration userWithDataConfig = configFactory.createSyncConfigurationBuilder(userWithData, Constants.USER_REALM)
                .fullSynchronization()
                .build();
        URI serverUrl = createRemoteData(userWithDataConfig);
        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);

        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(adminUser, serverUrl.toString())
                .fullSynchronization()
                .build();
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    assertTransferComplete(progress, true);
                    assertEquals(TEST_SIZE, getStoreTestDataSize(config));
                    allChangesDownloaded.countDown();
                }
            }
        });
        TestHelper.awaitOrFail(allChangesDownloaded);
        realm.close();
    }

    @Test
    public void downloadProgressListener_indefinitely() throws InterruptedException {
        final AtomicInteger transferCompleted = new AtomicInteger(0);
        final CountDownLatch allChangesDownloaded = new CountDownLatch(1);
        final CountDownLatch startWorker = new CountDownLatch(1);
        final SyncUser userWithData = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final SyncConfiguration userWithDataConfig = configFactory.createSyncConfigurationBuilder(userWithData, Constants.USER_REALM)
                .name("remote")
                .fullSynchronization()
                .build();

        URI serverUrl = createRemoteData(userWithDataConfig);

        // Create worker thread that puts data into another Realm.
        // This is to avoid blocking one progress listener while waiting for another to complete.
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                TestHelper.awaitOrFail(startWorker);
                createRemoteData(userWithDataConfig);
            }
        });
        worker.start();

        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        final SyncConfiguration adminConfig = configFactory.createSyncConfigurationBuilder(adminUser, serverUrl.toString())
                .name("local")
                .fullSynchronization()
                .build();
        Realm adminRealm = Realm.getInstance(adminConfig);
        SyncSession session = SyncManager.getSession(adminConfig);
        session.addDownloadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                long objectCounts = getStoreTestDataSize(adminConfig);
                // The downloading progress listener could be triggered at the db version where only contains the meta
                // data. So we start checking from when the first 10 objects downloaded.
                RealmLog.warn(String.format(
                        Locale.ENGLISH,"downloadProgressListener_indefinitely download %d/%d objects count:%d",
                        progress.getTransferredBytes(), progress.getTransferableBytes(), objectCounts));
                if (objectCounts != 0 && progress.isTransferComplete()) {

                    switch (transferCompleted.incrementAndGet()) {
                        case 1: {
                            assertEquals(TEST_SIZE, objectCounts);
                            assertTransferComplete(progress, true);
                            startWorker.countDown();
                            break;
                        }
                        case 2: {
                            assertTransferComplete(progress, true);
                            assertEquals(TEST_SIZE * 2, objectCounts);
                            allChangesDownloaded.countDown();
                            break;
                        }
                        default:
                            fail("Transfer complete called too many times:" + transferCompleted.get());
                    }
                }
            }
        });
        TestHelper.awaitOrFail(allChangesDownloaded);
        adminRealm.close();
        // worker thread will hang if logout happens before listener triggered.
        worker.join();
        userWithData.logOut();
        adminUser.logOut();
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
                Realm tempRealm = Realm.getInstance(config);
                long objectsCount = tempRealm.where(AllTypes.class).count();
                tempRealm.close();
                // FIXME: Remove the objectsCount checking when
                // https://github.com/realm/realm-object-store/issues/581 gets fixed
                if (objectsCount != 0 && progress.isTransferComplete()) {
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
