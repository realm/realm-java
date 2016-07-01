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

package io.realm.tests.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.tests.sync.model.ProcessInfo;
import io.realm.tests.sync.service.SendOneCommit;
import io.realm.tests.sync.utils.Constants;
import io.realm.tests.sync.utils.HttpUtils;

import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class ProcessCommitTests {
    HttpUtils httpUtils = new HttpUtils();

    @Before
    public void setUp () throws Exception {
        httpUtils.startSyncServer();
    }

    @After
    public void tearDown () throws Exception {
        httpUtils.stopSyncServer();
    }

    @Test
    public void expectServerCommit() throws Exception {
        final CountDownLatch testFinished = new CountDownLatch(1);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
                    final RealmConfiguration syncConfig = new RealmConfiguration
                            .Builder(targetContext)
                            .name("main_process")
                            .withSync(Constants.SYNC_SERVER_URL)
                            .syncUserToken(Constants.USER_TOKEN)
                            .build();
                    final Realm realm = Realm.getInstance(syncConfig);
                    Intent intent = new Intent(targetContext, SendOneCommit.class);
                    targetContext.startService(intent);

                    final RealmResults<ProcessInfo> all = realm.where(ProcessInfo.class).findAll();
                    all.addChangeListener(new RealmChangeListener<RealmResults<ProcessInfo>>() {
                        @Override
                        public void onChange(RealmResults<ProcessInfo> element) {
                            assertNotEquals(0, all.size());
                            testFinished.countDown();
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        });
        testFinished.await();
    }
}
