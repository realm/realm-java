/*
 * Copyright 2015 Realm Inc.
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

import android.os.Looper;
import android.test.InstrumentationTestCase;

import junit.framework.AssertionFailedError;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.entities.AllTypes;
import io.realm.entities.NonLatinFieldNames;

public class RealmAsyncQueryTests extends InstrumentationTestCase {

    //TODO use Junit4 & define a rule to setup the threading, to avoid
    //     this boiler plat code, or use a custom test runner
    public void testFindAll() throws Throwable {
        // We need to control precisely which Looper/Thread our Realm
        // will operate on. This is unfortunately not possible when using the
        // current Instrumentation#InstrumentationThread, because InstrumentationTestRunner#onStart
        // Call Looper.prepare() for us and surprisingly doesn't call Looper#loop(), this is problematic
        // as the async query callback will not run (because the Handler is sending Runnables to a Looper
        // that didn't loop.
        //
        // In the other hand, using a dedicated 'ExecutorService' will allow us to fine grain control the
        // desired behaviour
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance();
                    populateTestRealm(realm, 10);

                    // async query (will run on different thread)
                    realm.findAsync(AllTypes.class,
                            new Realm.QueryCallback<RealmResults<AllTypes>>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    try {
                                        assertEquals(10, results.size());

                                        //Make sure access to RealmObject will not throw an Exception
                                        for (int i = 0, size = results.size(); i < size; i++) {
                                            assertEquals(i, results.get(i).getColumnLong());
                                        }

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        // whatever happened, make sure to notify the waiting TestCase Thread
                                        signalCallbackFinished.countDown();
                                    }
                                }

                                @Override
                                public void onError(Throwable t) {
                                    try {
                                        threadAssertionError[0] = t;
                                        t.printStackTrace();
                                    } finally {
                                        signalCallbackFinished.countDown();
                                    }
                                }
                            })
                            .between("columnLong", 0, 9).findAll();

                    Looper.loop();//ready to receive callback

                } finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });

        // wait until the callback of our async query proceed
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // Throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // *** Helper methods ***

    // This could be done from #setUp but then we can't control
    // which Looper we want to associate this Realm instance with
    private Realm openRealmInstance() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(getInstrumentation().getTargetContext())
                .name("test.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.deleteRealm(realmConfiguration);
        return Realm.getInstance(realmConfiguration);
    }

    private void populateTestRealm(final Realm testRealm, int objects) {
        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        testRealm.allObjects(NonLatinFieldNames.class).clear();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567f + i);
            nonLatinFieldNames.setΒήτα(1.234567f + i);
        }
        testRealm.commitTransaction();
        testRealm.refresh();
    }
}
