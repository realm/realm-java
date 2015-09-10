package io.realm;

import android.annotation.SuppressLint;
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
    private Realm testRealm;
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
    @SuppressLint("HandlerLeak") // SuppressLint bug, doesn't work
    private class InterprocessHandler extends Handler {
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
            Looper.myLooper().quit();
        }

        public InterprocessHandler(Runnable startRunnable) {
            super(Looper.myLooper());
            localMessenger = new Messenger(this);
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

        Realm.deleteRealm(new RealmConfiguration.Builder(getContext()).build());

        // Start the testing service
        serviceStartLatch = new CountDownLatch(1);
        Intent intent = new Intent(getContext(), RemoteProcessService.class);
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        assertTrue(serviceStartLatch.await(10, TimeUnit.SECONDS));
    }

    @Override
    protected void tearDown() throws Exception {
        int counter = 10;
        if (testRealm != null) {
            testRealm.close();
        }

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
    // When this method return null, it doesn't mean the remote process is not exsited. An 'empty' process could
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
    public void testExitProcess() {
        new InterprocessHandler(new Runnable() {
            @Override
            public void run() {
                // Step A
                triggerServiceStep(RemoteProcessService.stepExitProcess_A);
            }
        }) {

            @SuppressWarnings("ConstantConditions")
            final int servicePid = getServiceInfo().pid;

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
                            ActivityManager.RunningAppProcessInfo processInfo = getRemoteProcessInfo();
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
        };
        Looper.loop();
    }

    // 1. Main process create Realm, write one object.
    // A. Service process open Realm, check if there is one and only one object.
    public void testCreateInitialRealm() throws InterruptedException {
        new InterprocessHandler(new Runnable() {
            @Override
            public void run() {
                // Step 1
                testRealm = Realm.getInstance(getContext());
                assertEquals(testRealm.allObjects(AllTypes.class).size(), 0);
                testRealm.beginTransaction();
                testRealm.createObject(AllTypes.class);
                testRealm.commitTransaction();

                // Step A
                triggerServiceStep(RemoteProcessService.stepCreateInitialRealm_A);
            }}) {

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
        };
        Looper.loop();
    }
}
