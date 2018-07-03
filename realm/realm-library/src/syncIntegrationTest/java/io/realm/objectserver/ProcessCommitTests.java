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

package io.realm.objectserver;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.annotations.RealmModule;
import io.realm.objectserver.model.ProcessInfo;
import io.realm.objectserver.model.TestObject;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.RemoteIntegrationTestService;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.RunTestWithRemoteService;
import io.realm.rule.RunWithRemoteService;
import io.realm.services.RemoteTestService;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class ProcessCommitTests extends StandardIntegrationTest {

    @RealmModule(classes = { ProcessInfo.class, TestObject.class })
    public static class ProcessCommitTestsModule { }

    @Rule
    public RunWithRemoteService remoteService = new RunWithRemoteService();

    @Override
    public void setupTest() throws IOException {
        super.setupTest();
        UserFactory.resetInstance();
    }

    public static class SimpleCommitRemoteService extends RemoteIntegrationTestService {
        private static SyncUser user;
        public static final Step stepA_openRealmAndCreateOneObject = new Step(RemoteTestService.BASE_SIMPLE_COMMIT, 1) {

            @Override
            protected void run() {
                user = UserFactory.getInstance().loginWithDefaultUser(Constants.AUTH_URL);
                String realmUrl = Constants.SYNC_SERVER_URL;

                final SyncConfiguration syncConfig = user.createConfiguration(realmUrl)
                        .fullSynchronization()
                        .modules(new ProcessCommitTestsModule())
                        .directory(getService().getRoot())
                        .build();
                getService().setRealm(Realm.getInstance(syncConfig));
                Realm realm = getService().getRealm();

                realm.beginTransaction();
                ProcessInfo processInfo = realm.createObject(ProcessInfo.class);
                processInfo.setName("Background_Process1");
                processInfo.setPid(android.os.Process.myPid());
                processInfo.setThreadId(Thread.currentThread().getId());
                realm.commitTransaction();
                Thread t = new Thread(() -> {
                    try {
                        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException("Upload interrupted", e);
                    }
                });
                t.start();
                try {
                    t.join(TestHelper.SHORT_WAIT_SECS * 1000);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Waiting for background thread interrupted", e);
                }
                realm.close();
            }
        };

        public static final Step stepB_closeRealmAndLogOut = new Step(RemoteTestService.BASE_SIMPLE_COMMIT, 2) {
            @Override
            protected void run() {
                getService().getRealm().close();
                user.logOut();
            }
        };
    }

    // 1. Open a sync Realm and listen to changes.
    // A. Open the same sync Realm and add one object.
    // 2. Get the notification, check if the change in A is received.
    @Test
    @RunTestInLooperThread
    @RunTestWithRemoteService(remoteService = SimpleCommitRemoteService.class, onLooperThread = true)
    public void expectSimpleCommit() {
        looperThread.runAfterTest(remoteService.afterRunnable);
        remoteService.createHandler(Looper.myLooper());

        final SyncUser user = UserFactory.getInstance().createDefaultUser(Constants.AUTH_URL);
        String realmUrl = Constants.SYNC_SERVER_URL;
        final SyncConfiguration syncConfig = user.createConfiguration(realmUrl)
                .fullSynchronization()
                .modules(new ProcessCommitTestsModule())
                .directory(looperThread.getRoot())
                .build();
        final Realm realm = Realm.getInstance(syncConfig);
        final RealmResults<ProcessInfo> all = realm.where(ProcessInfo.class).findAll();
        looperThread.keepStrongReference(all);
        all.addChangeListener(new RealmChangeListener<RealmResults<ProcessInfo>>() {
            @Override
            public void onChange(RealmResults<ProcessInfo> element) {
                assertEquals(1, all.size());
                assertEquals("Background_Process1", all.get(0).getName());
                realm.close();
                user.logOut();

                remoteService.triggerServiceStep(SimpleCommitRemoteService.stepB_closeRealmAndLogOut);

                looperThread.testComplete();
            }
        });

        remoteService.triggerServiceStep(SimpleCommitRemoteService.stepA_openRealmAndCreateOneObject);
    }

    public static class ALotCommitsRemoteService extends RemoteIntegrationTestService {
        private static SyncUser user;
        public static final Step stepA_openRealm = new Step(RemoteTestService.BASE_A_LOT_COMMITS, 1) {

            @Override
            protected void run() {
                user = UserFactory.getInstance().loginWithDefaultUser(Constants.AUTH_URL);
                String realmUrl = Constants.SYNC_SERVER_URL;

                final SyncConfiguration syncConfig = user.createConfiguration(realmUrl)
                        .fullSynchronization()
                        .modules(new ProcessCommitTestsModule())
                        .directory(getService().getRoot())
                        .name(UUID.randomUUID().toString() + ".realm")
                        .build();
                getService().setRealm(Realm.getInstance(syncConfig));
            }
        };

        public static final Step stepB_createObjects = new Step(RemoteTestService.BASE_A_LOT_COMMITS, 2) {
            @Override
            protected void run() {
                Realm realm = getService().getRealm();
                realm.beginTransaction();
                for (int i = 0; i < 100; i++) {
                    Number max = realm.where(TestObject.class).findAll().max("intProp");
                    int pk = max == null ? 0 : max.intValue() + 1;
                    TestObject testObject = realm.createObject(TestObject.class, pk);
                    testObject.setStringProp("Str" + pk);
                }
                realm.commitTransaction();
            }
        };

        public static final Step stepC_closeRealm = new Step(RemoteTestService.BASE_A_LOT_COMMITS, 3) {
            @Override
            protected void run() {
                getService().getRealm().close();
                user.logOut();
            }
        };
    }

    // 1. Open a sync Realm and listen to changes.
    // A. Open the same sync Realm.
    // B. Create 100 objects.
    // 2. Check if the 100 objects are received.
    // #. Repeat B/2 10 times.
    @Test
    @RunTestWithRemoteService(remoteService = ALotCommitsRemoteService.class, onLooperThread = true)
    @RunTestInLooperThread
    public void expectALot() throws Throwable {
        looperThread.runAfterTest(remoteService.afterRunnable);
        remoteService.createHandler(Looper.myLooper());

        final SyncUser user = UserFactory.getInstance().createDefaultUser(Constants.AUTH_URL);
        String realmUrl = Constants.SYNC_SERVER_URL;
        final SyncConfiguration syncConfig = user.createConfiguration(realmUrl)
                .fullSynchronization()
                .modules(new ProcessCommitTestsModule())
                .directory(looperThread.getRoot())
                .build();
        final Realm realm = Realm.getInstance(syncConfig);
        final RealmResults<TestObject> all = realm.where(TestObject.class).sort("intProp").findAll();
        looperThread.keepStrongReference(all);
        final AtomicInteger listenerCalledCounter = new AtomicInteger(0);
        all.addChangeListener(new RealmChangeListener<RealmResults<TestObject>>() {
            @Override
            public void onChange(RealmResults<TestObject> element) {
                int counter = listenerCalledCounter.incrementAndGet();
                int size = all.size();
                if (size == 0) {
                    listenerCalledCounter.decrementAndGet();
                    return;
                }
                assertEquals(0, size % 100); // Added 100 objects every time.
                assertEquals(counter * 100 - 1, all.last().getIntProp());
                assertEquals("Str" + (counter * 100 - 1), all.last().getStringProp());
                if (counter == 10) {
                    remoteService.triggerServiceStep(ALotCommitsRemoteService.stepC_closeRealm);
                    realm.close();
                    user.logOut();
                    looperThread.testComplete();
                } else {
                    remoteService.triggerServiceStep(ALotCommitsRemoteService.stepB_createObjects);
                }
            }
        });

        remoteService.triggerServiceStep(ALotCommitsRemoteService.stepA_openRealm);
        remoteService.triggerServiceStep(ALotCommitsRemoteService.stepB_createObjects);
    }
}
