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

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.objectserver.model.ProcessInfo;
import io.realm.objectserver.model.TestObject;
import io.realm.objectserver.service.SendOneCommit;
import io.realm.objectserver.service.SendsALot;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.HttpUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ProcessCommitTests {
    @BeforeClass
    public static void setUp () throws Exception {
        HttpUtils.startSyncServer();
    }

    @AfterClass
    public static void tearDown () throws Exception {
        HttpUtils.stopSyncServer();
    }

    // FIXME: At least need one method in the test class
    @Test
    public void dummy() {

    }

    // FIXME: Disable for now.
    /*
    @Test
    public void expectServerCommit() throws Throwable {
        final Throwable[] exception = new Throwable[1];
        final CountDownLatch testFinished = new CountDownLatch(1);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

                    final SyncConfiguration syncConfig = new SyncConfiguration.Builder()
                            .name(SendOneCommit.class.getSimpleName())
                            .serverUrl(Constants.SYNC_SERVER_URL )
                            .user(UserFactory.createDefaultUser(Constants.SYNC_SERVER_URL, Constants.USER_TOKEN))
                            .build();
                    Realm.deleteRealm(syncConfig);//TODO do this in Rule as async tests
                    final Realm realm = Realm.getInstance(syncConfig);
                    Intent intent = new Intent(targetContext, SendOneCommit.class);
                    targetContext.startService(intent);

                    final RealmResults<ProcessInfo> all = realm.where(ProcessInfo.class).findAll();
                    all.addChangeListener(new RealmChangeListener<RealmResults<ProcessInfo>>() {
                        @Override
                        public void onChange(RealmResults<ProcessInfo> element) {
                            assertEquals(1, all.size());
                            assertEquals("Background_Process1", all.get(0).getName());
                            testFinished.countDown();
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    exception[0] = e;
                    testFinished.countDown();
                }
            }
        });
        boolean testTimedOut = testFinished.await(300, TimeUnit.SECONDS);
        if (exception[0] != null) {
            throw exception[0];
        } else if (!testTimedOut) {
            fail("Test timed out ");
        }
    }
    */

    //TODO send string from service and match
    //     replicate integration tests from Cocoa
    //     add gradle task to start the sh script automatically (create pid file, ==> run or kill existing process
    //     check the requirement for the issue again
    /*
    @Test
    public void expectALot() throws Throwable {
        final Throwable[] exception = new Throwable[1];
        final CountDownLatch testFinished = new CountDownLatch(1);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

                    final SyncConfiguration syncConfig = new SyncConfiguration.Builder(targetContext)
                            .name(SendsALot.class.getSimpleName())
                            .serverUrl(Constants.SYNC_SERVER_URL_2)
                            .user(UserFactory.createDefaultUser(Constants.SYNC_SERVER_URL_2, Constants.USER_TOKEN))
                            .build();
                    Realm.deleteRealm(syncConfig);//TODO do this in Rule as async tests
                    final Realm realm = Realm.getInstance(syncConfig);
                    Intent intent = new Intent(targetContext, SendsALot.class);
                    targetContext.startService(intent);

                    final RealmResults<TestObject> all = realm.where(TestObject.class).findAllSorted("intProp");
                    all.addChangeListener(new RealmChangeListener<RealmResults<TestObject>>() {
                        @Override
                        public void onChange(RealmResults<TestObject> element) {
                            assertEquals(100, element.size());
                            for (int i = 0; i < 100; i++) {
                                assertEquals(i, element.get(i).getIntProp());
                                assertEquals("property " + i, element.get(i).getStringProp());
                            }

                            testFinished.countDown();
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    exception[0] = e;
                    testFinished.countDown();
                }
            }
        });
        boolean testTimedOut = testFinished.await(30, TimeUnit.SECONDS);
        if (exception[0] != null) {
            throw exception[0];
        } else if (!testTimedOut) {
            fail("Test timed out ");
        }
    }
    */
}
