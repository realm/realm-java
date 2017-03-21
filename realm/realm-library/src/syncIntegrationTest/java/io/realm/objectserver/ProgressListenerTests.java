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

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

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

public class ProgressListenerTests extends BaseIntegrationTest {

    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Test
    @RunTestInLooperThread
    public void testDownloadProgressChangesOnly() {
    }

    @Test
    @RunTestInLooperThread
    public void testDownloadProgressIndefinetely() {

    }

    @Test
    public void testUploadProgressChangesOnly() {
        final CountDownLatch allChangeUploaded = new CountDownLatch(1);

        SyncUser user = loginAdminUser();
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL).build();

        Realm realm = Realm.getInstance(config);

        realm.beginTransaction();
        for (int i = 0; i < 10; i++) {
            AllTypes obj = realm.createObject(AllTypes.class);
            obj.setColumnString("Object " + i);
        }
        realm.commitTransaction();
        SyncSession session = SyncManager.getSession(config);
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    allChangeUploaded.countDown();
                }
            }
        });

        TestHelper.awaitOrFail(allChangeUploaded);
    }

    @Test
    @RunTestInLooperThread
    public void testUploadProgressIndefinetely() {

    }
}
