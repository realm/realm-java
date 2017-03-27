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

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
import io.realm.exceptions.RealmFileException;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ProgressListenerTests extends BaseIntegrationTest {

    private static final long TEST_SIZE = 10;
    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @NonNull
    private SyncConfiguration createSyncConfig() {
        SyncUser user = loginAdminUser();
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

    private void assertTransferComplete(Progress progress) {
        assertTrue(progress.isTransferComplete());
        assertEquals(1.0D, progress.getFractionTransferred(), 0.0D);
        assertEquals(progress.getTransferableBytes(), progress.getTransferredBytes());
    }

    // Create remote data for a given user. This will block until changes have been uploaded.
    private URI createRemoteData(SyncUser user) {
        final CountDownLatch changesUploaded = new CountDownLatch(1);
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, Constants.SYNC_USER_REALM).build();
        final Realm realm = Realm.getInstance(config);
        writeSampleData(realm);
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
    public void testDownloadProgressChangesOnly() {
        final CountDownLatch allChangesDownloaded = new CountDownLatch(1);
        SyncUser userWithData = loginUser();
        URI serverUrl = createRemoteData(userWithData);
        SyncUser adminUser = loginAdminUser();

        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(adminUser, serverUrl.toString()).build();
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        session.addDownloadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    assertTransferComplete(progress);
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
    public void testDownloadProgressIndefinetely() {
        final AtomicInteger transferCompleted = new AtomicInteger(0);
        final CountDownLatch allChangesDownloaded = new CountDownLatch(1);
        final SyncUser userWithData = loginUser();
        URI serverUrl = createRemoteData(userWithData);
        SyncUser adminUser = loginAdminUser();

        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(adminUser, serverUrl.toString()).build();
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        session.addDownloadProgressListener(ProgressMode.INDEFINETELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    assertTransferComplete(progress);
                    switch (transferCompleted.incrementAndGet()) {
                        case 1:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    createRemoteData(userWithData);
                                    Realm adminRealm = Realm.getInstance(config);
                                    assertEquals(TEST_SIZE, adminRealm.where(AllTypes.class).count());
                                    adminRealm.close();
                                }
                            }).start();
                            break;
                        case 2:
                            // Ignore. ObjectStore seems to trigger this for some reason.
                            // A guess is when adding the UPLOAD listener for the sample
                            // data.
                            break;
                        case 3:
                            Realm adminRealm = Realm.getInstance(config);
                            assertEquals(TEST_SIZE*2, adminRealm.where(AllTypes.class).count());
                            adminRealm.close();
                            allChangesDownloaded.countDown();
                            break;
                        default:
                            fail();
                    }
                }
            }
        });
        TestHelper.awaitOrFail(allChangesDownloaded);
        realm.close();
        userWithData.logout();
        adminUser.logout();
    }

    @Test
    public void testUploadProgressChangesOnly() {
        final CountDownLatch allChangeUploaded = new CountDownLatch(1);
        SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);
        writeSampleData(realm);

        SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    assertTransferComplete(progress);
                    allChangeUploaded.countDown();
                }
            }
        });

        TestHelper.awaitOrFail(allChangeUploaded);
        realm.close();

    }

    @Test
    public void testUploadProgressIndefinetely() {
        final AtomicInteger transferCompleted = new AtomicInteger(0);
        final CountDownLatch testDone = new CountDownLatch(1);
        final SyncConfiguration config = createSyncConfig();
        Realm realm = Realm.getInstance(config);

        writeSampleData(realm); // Write first batch of sample data
        SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.INDEFINETELY, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    switch(transferCompleted.incrementAndGet()) {
                        case 1:
                            // Write more sample data
                            // Adding listeners inside another listener seems to deadlock
                            // sync, but nothing gets reported.
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Realm realm = Realm.getInstance(config);
                                    writeSampleData(realm);
                                    realm.close();
                                }
                            }).start();
                            break;
                        case 2:
                            assertTransferComplete(progress);
                            testDone.countDown();
                            break;
                        default:
                            fail();
                    }
                }
            }
        });

        TestHelper.awaitOrFail(testDone);
        realm.close();
    }
}
