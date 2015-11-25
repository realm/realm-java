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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.test.AndroidTestCase;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.entities.AllTypes;
import io.realm.services.RemoteProcessService;

// This is built for testing multi processes related cases.
// To build a test case, create an InterprocessHandler in your test case. This handler will run in the newly
// created thread's Looper. Remember to call Looper.loop() to start handling messages.
// Pass the first thing you want to run to the constructor which will be posted to the beginning of the message queue.
// And add steps you want to run in the remote process in RemoteProcessService.
// Write the comments of the test case like this:
// A-Z means steps running from remote service process.
// 1-9xx means steps running from the main local process.
// eg.: A. Open a Realm
//      1. Open two Realms
//      B. Open three Realms
//      2. assertTrue("OK, remote process win. You can open more Realms than I do in the main local process", false);
public class RealmInterprocessTest extends AndroidTestCase {
    private static RealmInterprocessTest thiz;
    private static InterprocessHandler interprocessHandler;
    private Realm testRealm;
    private RealmChangeListener listener;
    private Messenger remoteMessenger;
    private Messenger localMessenger;
    private CountDownLatch serviceStartLatch;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            remoteMessenger = new Messenger(iBinder);
            serviceStartLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (serviceStartLatch != null && serviceStartLatch.getCount() > 1) {
                serviceStartLatch.countDown();
            }
            serviceStartLatch = null;
        }
    };

    // It is necessary to overload this method.
    // AndroidTestRunner does call Looper.prepare() and we can have a looper in the case. The problem is all the test
    // cases are running in a single thread!!! And after Looper.quit() called, it cannot start again. That means we
    // can only have one case in this class LoL.
    // By overloading this method, we create a new thread and looper to run the real case. And use latch to wait until
    // it is finished. Then we can get rid of creating the thread in the test method, using array to store exception, many
    // levels of nested code. Make the test case more nature.
    @Override
    public void runBare() throws Throwable {
        final Throwable[] throwableArray = new Throwable[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    RealmInterprocessTest.super.runBare();
                } catch (Throwable throwable) {
                    throwableArray[0] = throwable;
                } finally {
                    latch.countDown();
                }
            }
        });

        thread.start();
        latch.await();

        if (throwableArray[0] != null) {
            throw throwableArray[0];
        }
    }

    // Helper handler to make it easy to interact with remote service process.
    private static class InterprocessHandler extends Handler {
        // Timeout Watchdog. In case the service crashed or expected response is not returned.
        // It is very important to feed the dog after the expected message arrived.
        private final int timeout = 5000;
        private volatile boolean isTimeout = true;
        private Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimeout) {
                    assertTrue("Timeout happened", false);
                } else {
                    isTimeout = true;
                    postDelayed(timeoutRunnable, timeout);
                }
            }
        };

        protected void clearTimeoutFlag() {
            isTimeout = false;
        }

        protected void done() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                looper.quit();
            } else {
                assertTrue("myLooper is null!", false);
            }
        }

        public InterprocessHandler(Runnable startRunnable) {
            super(Looper.myLooper());
            thiz.localMessenger = new Messenger(this);
            // To have the first step from main process run
            post(startRunnable);
            // Start watchdog
            postDelayed(timeoutRunnable, timeout);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String error = bundle.getString(RemoteProcessService.BUNDLE_KEY_ERROR);
            if (error != null) {
                // Assert and show error from service process
                assertTrue(error, false);
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        thiz = this;

        Realm.deleteRealm(new RealmConfiguration.Builder(getContext()).build());

        // Start the testing service
        serviceStartLatch = new CountDownLatch(1);
        Intent intent = new Intent(getContext(), RemoteProcessService.class);
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        assertTrue(serviceStartLatch.await(10, TimeUnit.SECONDS));
    }

    @Override
    protected void tearDown() throws Exception {
        if (interprocessHandler != null) {
            interprocessHandler.removeCallbacksAndMessages(null);
            interprocessHandler = null;
        }

        int counter = 10;
        if (testRealm != null) {
            testRealm.removeAllChangeListeners();
            testRealm.close();
        }
        listener = null;

        getContext().unbindService(serviceConnection);
        remoteMessenger = null;

        // Kill the remote process.
        ActivityManager.RunningAppProcessInfo info = getRemoteProcessInfo();
        if (info != null) {
            android.os.Process.killProcess(info.pid);
        }
        while (getRemoteProcessInfo() != null) {
            if (counter == 0) {
                assertTrue("The remote service process is still alive.", false);
            }
            Thread.sleep(300);
            counter--;
        }
        super.tearDown();
    }

    // Call this to trigger the next step of service process
    private void triggerServiceStep(RemoteProcessService.Step step) {
        Message msg = Message.obtain(null, step.message);
        msg.replyTo = localMessenger;
        try {
            remoteMessenger.send(msg);
        } catch (RemoteException e) {
            assertTrue(false);
        }
    }

    // Return the service info if it is alive.
    // When this method return null, it doesn't mean the remote process is not existed. An 'empty' process could
    // be retained by the system to be used next time.
    // Use getRemoteProcessInfo if you want to check the existence of remote process.
    private ActivityManager.RunningServiceInfo getServiceInfo() {
        ActivityManager manager = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfoList = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : serviceInfoList) {
            if (RemoteProcessService.class.getName().equals(service.service.getClassName())) {
                return service;
            }
        }
        return null;
    }

    // Get the remote process info if it is alive.
    private ActivityManager.RunningAppProcessInfo getRemoteProcessInfo() {
        ActivityManager manager = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : processInfoList) {
            if (info.processName.equals(getContext().getPackageName() + ":remote")) {
                return info;
            }
        }

        return null;
    }

    // A. Open a realm, close it, then call Runtime.getRuntime().exit(0).
    // 1. Wait 3 seconds to see if the service process existed.
    private static class TestExitProcessHandler extends InterprocessHandler {
        @SuppressWarnings("ConstantConditions")
        final int servicePid = thiz.getServiceInfo().pid;

        TestExitProcessHandler() {
            super(new Runnable() {
                @Override
                public void run() {
                    // Step A
                    thiz.triggerServiceStep(RemoteProcessService.stepExitProcess_A);
                }
            });
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RemoteProcessService.stepExitProcess_A.message) {
                // Step 1
                clearTimeoutFlag();
                try {
                    // Timeout is 5 seconds. 3 (6x500ms) seconds should be enough to quit the process.
                    for (int i = 1; i <= 6; i++) {
                        // We need to retrieve the service's pid again since the system might restart it automatically.
                        ActivityManager.RunningAppProcessInfo processInfo = thiz.getRemoteProcessInfo();
                        if (processInfo != null && processInfo.pid == servicePid && i >= 6) {
                            // The process is still alive.
                            assertTrue(false);
                        } else if (processInfo == null || processInfo.pid != servicePid) {
                            // The process is gone
                            break;
                        }
                        Thread.sleep(500, 0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    assertTrue(false);
                }
                done();
            }
        }
    }
    public void testExitProcess() {
        interprocessHandler = new TestExitProcessHandler();
        Looper.loop();
    }

    // 1. Main process creates Realm, writes one object.
    // A. Service process opens Realm, checks if there is one and only one object.
    private static class TestCreateInitialRealmHandler extends InterprocessHandler {
        TestCreateInitialRealmHandler() {
            super(new Runnable() {
            @Override
            public void run() {
                // Step 1
                thiz.testRealm = Realm.getInstance(thiz.getContext());
                assertEquals(thiz.testRealm.allObjects(AllTypes.class).size(), 0);
                thiz.testRealm.beginTransaction();
                thiz.testRealm.createObject(AllTypes.class);
                thiz.testRealm.commitTransaction();

                // Step A
                thiz.triggerServiceStep(RemoteProcessService.stepCreateInitialRealm_A);
            }});
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RemoteProcessService.stepCreateInitialRealm_A.message) {
                clearTimeoutFlag();
                done();
            } else {
                assertTrue(false);
            }
        }
    }
    public void testCreateInitialRealm() throws InterruptedException {
        interprocessHandler = new TestCreateInitialRealmHandler();
        Looper.loop();
    }

    // 1. Main process creates Realm, adds a change listener.
    // A. Service process opens Realm, creates one object in the Realm.
    // 2. Main process's listener gets triggered, create the 2nd object in the Realm.
    // B. Service process's listener gets triggered, checks if there are 2 objects, finish the test.
    private static class TestNotificationHandler extends InterprocessHandler {
        TestNotificationHandler() {
            super(new Runnable() {
            @Override
            public void run() {
                // Step 1
                thiz.testRealm = Realm.getInstance(thiz.getContext());
                assertEquals(thiz.testRealm.allObjects(AllTypes.class).size(), 0);
                thiz.testRealm.addChangeListener(thiz.listener);

                // Step A
                thiz.triggerServiceStep(RemoteProcessService.stepNotification_A);
            }});

            thiz.listener = new RealmChangeListener() {
                @Override
                public void onChange() {
                    // Step 2
                    assertEquals(thiz.testRealm.allObjects(AllTypes.class).size(), 1);

                    // To avoid onChange to be triggered again which will cause a dead loop
                    thiz.testRealm.removeAllChangeListeners();

                    // Step B
                    thiz.triggerServiceStep(RemoteProcessService.stepNotification_B);

                    try {
                        // HACK to make sure the other process's next step is triggered first before the Realm change
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        assertTrue(e.getMessage(), false);
                    }

                    // Create another object and trigger the other process's listener
                    thiz.testRealm.beginTransaction();
                    thiz.testRealm.createObject(AllTypes.class);
                    thiz.testRealm.commitTransaction();
                }
            };
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RemoteProcessService.stepNotification_A.message) {
                clearTimeoutFlag();
            } else if (msg.what == RemoteProcessService.stepNotification_B.message) {
                done();
            } else {
                assertTrue(false);
            }
        }
    }
    public void testNotification() {
        interprocessHandler = new TestNotificationHandler();
        Looper.loop();
    }

    // 1. Enable the interprocess notification, create Realm, and then set auto-refresh disabled.
    // A. Enable the interprocess notification, create Realm, create a object in Realm, wait 100ms for let main process
    //    handle the REALM_CHANGED before next step.
    // 2. Enable auto-refresh
    // B. Create another object in Realm
    // 3. The change listener gets triggered by step B, done.
    private static class TestSetAutoRefreshHandler extends  InterprocessHandler {
        public TestSetAutoRefreshHandler() {
            super(new Runnable() {
                @Override
                public void run() {
                    // Step 1
                    thiz.testRealm = Realm.getInstance(thiz.getContext());
                    thiz.testRealm.setAutoRefresh(false);
                    assertEquals(thiz.testRealm.allObjects(AllTypes.class).size(), 0);
                    thiz.listener = new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            // This should not be triggered
                            assertTrue(false);
                        }
                    };
                    thiz.testRealm.addChangeListener(thiz.listener);

                    // Step A
                    thiz.triggerServiceStep(RemoteProcessService.stepSetAutoRefresh_A);
                }
            });
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RemoteProcessService.stepSetAutoRefresh_A.message) {
                // Step 2
                clearTimeoutFlag();
                thiz.testRealm.removeChangeListener(thiz.listener);
                thiz.listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        // Step 3
                        assertEquals(2, thiz.testRealm.allObjects(AllTypes.class).size());
                        done();
                    }
                };
                thiz.testRealm.addChangeListener(thiz.listener);
                thiz.testRealm.setAutoRefresh(true);

                // Step B
                thiz.triggerServiceStep(RemoteProcessService.stepSetAutoRefresh_B);
            } else if (msg.what == RemoteProcessService.stepSetAutoRefresh_B.message) {
                clearTimeoutFlag();
            } else {
                assertTrue(false);
            }
        }
    }
    public void testSetAutoRefresh() {
        interprocessHandler = new TestSetAutoRefreshHandler();
        Looper.loop();
    }


    // 1. Wait the service process starts.
    // A. Open the Realm instance.
    // 2. Try to compact Realm, and it should return false.
    // B. Starts a transaction.
    // 3. Try to compact Realm, and it should return false.
    // C. Cancel transaction, and close the Realm.
    // 4. Compact the Realm, and it should return true. Done.
    private static class TestCompactHandler extends  InterprocessHandler {
        private RealmConfiguration configuration = new RealmConfiguration.Builder(thiz.getContext()).build();

        public TestCompactHandler() {
            super(new Runnable() {
                @Override
                public void run() {
                    // Step 1

                    // Step A
                    thiz.triggerServiceStep(RemoteProcessService.stepCompact_A);
                }
            });
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RemoteProcessService.stepCompact_A.message) {
                // Step 2
                clearTimeoutFlag();
                assertFalse(Realm.compactRealm(configuration));

                // Step B
                thiz.triggerServiceStep(RemoteProcessService.stepCompact_B);
            } else if (msg.what == RemoteProcessService.stepCompact_B.message) {
                // Step 3
                clearTimeoutFlag();
                assertFalse(Realm.compactRealm(configuration));

                // Step C
                thiz.triggerServiceStep(RemoteProcessService.stepCompact_C);
            } else if (msg.what == RemoteProcessService.stepCompact_C.message) {
                // Step 4
                clearTimeoutFlag();
                assertTrue(Realm.compactRealm(configuration));

                done();
            } else {
                assertTrue(false);
            }
        }
    }
    public void testCompact() {
        interprocessHandler = new TestCompactHandler();
        Looper.loop();
    }

    // 1. Create a Realm instance in main process.
    // A. Check if the Realm file exist, then delete it.
    // 2.1. Check if the file exist, should be no.
    // 2.2. Create an object in the opened Realm, Check if it gets written. And check the file existence, should be no.
    //      then close the Realm.
    // 2.3. Create a new Realm instance, check if there is any objects in. Should be no, the object created in 2.2 is
    //      not in this Realm file. Close the Realm, check file existence, should be yes.
    private static class TestDeleteHandler extends  InterprocessHandler {
        public TestDeleteHandler() {
            super(new Runnable() {
                @Override
                public void run() {
                    // Step 1
                    thiz.testRealm = Realm.getInstance(thiz.getContext());

                    // Step A
                    thiz.triggerServiceStep(RemoteProcessService.stepDelete_A);
                }
            });
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RemoteProcessService.stepDelete_A.message) {
                clearTimeoutFlag();

                // Step 2.1
                RealmConfiguration configuration = thiz.testRealm.getConfiguration();
                File file = new File(configuration.getPath());
                assertFalse(file.exists());

                // Step 2.2
                thiz.testRealm.beginTransaction();
                thiz.testRealm.createObject(AllTypes.class);
                thiz.testRealm.commitTransaction();
                assertEquals(1, thiz.testRealm.allObjects(AllTypes.class).size());

                assertFalse(file.exists());
                thiz.testRealm.close();

                // Step 2.3
                thiz.testRealm = Realm.getInstance(thiz.getContext());
                assertEquals(0, thiz.testRealm.allObjects(AllTypes.class).size());
                thiz.testRealm.close();
                thiz.testRealm = null;
                assertTrue(file.exists());

                done();
            } else {
                assertTrue(false);
            }
        }
    }
    public void testDelete() {
        interprocessHandler = new TestDeleteHandler();
        Looper.loop();
    }
}
