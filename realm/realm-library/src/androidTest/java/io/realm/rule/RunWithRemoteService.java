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

package io.realm.rule;

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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.realm.TestHelper;
import io.realm.services.RemoteTestService;

import static android.support.test.InstrumentationRegistry.getContext;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * This is a helper {@link TestRule} to do test which needs interaction with a remote process.
 * To use this:
 * 1. Define a subclass of {@link RemoteTestService} and create steps as static member of it. Those steps should be
 * named as "stepA_doXXX", "stepB_doYYY", etc. to indicate the order of them.
 * 2. Add a base message id in {@link RemoteTestService}.
 * 3. Add the service into the AndroidManifest.xml. And the android:process property must be ":remote".
 * 4. Annotate your test case by {@link RunTestWithRemoteService} with your remote service class.
 * 5. To run the tests on the looper thread:
 *    a) Add {@link RunTestInLooperThread} to the tests.
 *    b) Add {@code @RunTestWithRemoteService(remoteService = SimpleCommitRemoteService.class, onLooperThread = true)}
 *       Please notice that {@code onLooperThread} needs to be set to true to avoid the remote service getting killed
 *       before looper thread finished
 *    c) Call {@code looperThread.runAfterTest(remoteService.afterRunnable)} to kill the remote service after test.
 * 6. When your looper thread starts, register the service messenger by calling
 * {@link RunWithRemoteService#createHandler(Looper)}.
 * 7. Trigger your first step in the remote service process by calling
 * {@link RunWithRemoteService#triggerServiceStep(RemoteTestService.Step)}.
 * 8. Name steps in the foreground process with step1, step2 ... stepN.
 *    Name steps in the remote process with stepA, stepB ... stepZ.
 *
 * See the existing test cases for examples.
 */
public class RunWithRemoteService implements TestRule {

    private class InterprocessHandler extends Handler {

        private InterprocessHandler(Looper looper) {
            super(looper);
            localMessenger = new Messenger(this);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String error = bundle.getString(RemoteTestService.BUNDLE_KEY_ERROR);
            if (error != null) {
                // Assert and show error from remote process
                fail(error);
            }
        }
    }

    private static final String REMOTE_PROCESS_POSTFIX = ":remote";

    private Messenger remoteMessenger;
    private Messenger localMessenger;
    private CountDownLatch serviceStartLatch;
    public Runnable afterRunnable = new Runnable() {
        @Override
        public void run() {
            after();
        }
    };

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
            remoteMessenger = null;
        }
    };

    private void before(Class<?> serviceClass) throws Throwable {
        // Start the testing remote process.
        serviceStartLatch = new CountDownLatch(1);
        Intent intent = new Intent(getContext(), serviceClass);
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        TestHelper.awaitOrFail(serviceStartLatch);
    }

    private void after() {
        getContext().unbindService(serviceConnection);

        // Kill the remote process.
        ActivityManager.RunningAppProcessInfo info = getRemoteProcessInfo();
        if (info != null) {
            android.os.Process.killProcess(info.pid);
        }
        int counter = 10;
        while (getRemoteProcessInfo() != null) {
            if (counter == 0) {
                fail("The remote process is still alive.");
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            counter--;
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        final RunTestWithRemoteService annotation = description.getAnnotation(RunTestWithRemoteService.class);
        if (annotation == null) {
            return base;
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(annotation.remoteService());
                try {
                    base.evaluate();
                } finally {
                    if (!annotation.onLooperThread()) {
                        after();
                    }
                }
            }
        };
    }

    public void createHandler(Looper looper) {
        new InterprocessHandler(looper);
    }

    // Call this to trigger the next step of remote process
    public void triggerServiceStep(RemoteTestService.Step step) {
        Message msg = Message.obtain(null, step.message);
        msg.replyTo = localMessenger;
        try {
            remoteMessenger.send(msg);
        } catch (RemoteException e) {
            fail(e.getMessage());
        }
        // TODO: Find a way to block caller thread until the service process finishes current step.
    }

    // Get the remote process info if it is alive.
    private ActivityManager.RunningAppProcessInfo getRemoteProcessInfo() {
        ActivityManager manager = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : processInfoList) {
            if (info.processName.equals(getContext().getPackageName() + REMOTE_PROCESS_POSTFIX)) {
                return info;
            }
        }

        return null;
    }
}
