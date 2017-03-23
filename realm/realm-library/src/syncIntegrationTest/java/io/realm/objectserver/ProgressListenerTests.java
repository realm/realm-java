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

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProgressListenerTests extends BaseIntegrationTest {

    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Test
    public void testDownloadProgressChangesOnly() {
    }

    @Test
    public void testDownloadProgressIndefinetely() {

    }

    @NonNull
    private SyncConfiguration createSyncConfig() {
        SyncUser user = loginAdminUser();
        return configFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL).build();
    }

    private void writeSampleData(Realm realm) {
        realm.beginTransaction();
        for (int i = 0; i < 10; i++) {
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
                            Realm realm = Realm.getInstance(config);
                            writeSampleData(realm);
                            realm.close();
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
    }
}
