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

package io.realm.rule;

import android.os.Handler;
import android.os.Looper;

import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.TestHelper;
import io.realm.internal.ObjectServerFacade;
import io.realm.internal.android.AndroidCapabilities;


/**
 * Rule that runs the test inside a worker looper thread. This rule is responsible
 * of creating a temp directory containing a Realm instance then deleting it, once the test finishes.
 * <p>
 * All Realms used in a method method annotated with {@code @RunTestInLooperThread } should use
 * {@link RunInLooperThread#createConfiguration()} and friends to create their configurations.
 * Failing to do so can result in the test failing because the Realm could not be deleted
 * (this class and {@link TestRealmConfigurationFactory} do not agree in which order to delete
 * the open Realms).
 */
public class RunInLooperThread extends TestRealmConfigurationFactory {
    private static final long WAIT_TIMEOUT_MS = 60 * 1000;

    // lock protecting objects shared with the test thread
    private final Object lock = new Object();

    // Thread safe
    private final CountDownLatch signalTestCompleted = new CountDownLatch(1);

    // Thread safe
    private boolean ruleBeingUsed = false;

    // Access guarded by 'lock'
    private RealmConfiguration realmConfiguration;

    // Default Realm created by this Rule. It is guaranteed to be closed when the test finishes.
    // Access guarded by 'lock'
    private Realm realm;

    // Access guarded by 'lock'
    private Handler backgroundHandler;

    // the variables created inside the test are local and eligible for GC.
    // but sometimes we need the variables to survive across different Looper
    // events (Callbacks happening in the future), so we add a strong reference
    // to them for the duration of the test.
    // Access guarded by 'lock'
    private List<Object> keepStrongReference;

    // Custom Realm used by the test. Saving the reference here will guarantee
    // that the instance is closed when exiting the test.
    // Access guarded by 'lock'
    private List<Realm> testRealms;

    // List of closable resources that will be automatically closed when the test finishes.
    // Access guarded by 'lock'
    private List<Closeable> closableResources;

    // Runnable guaranteed to trigger after the test either succeeded or failed.
    // Access guarded by 'lock'
    private List<Runnable> runAfterTestIsComplete = new ArrayList<>();

    /**
     * Get the configuration for the test realm.
     * <p>
     * Set on main thread, accessed from test thread.
     * Valid after {@code before}.
     *
     * @return the test realm configuration.
     */
    public RealmConfiguration getConfiguration() {
        synchronized (lock) {
            return realmConfiguration;
        }
    }

    /**
     * Get the test realm.
     * <p>
     * Set on test thread, accessed from main thread.
     * Valid only after the test thread has started.
     *
     * @return the test realm.
     */
    public Realm getRealm() {
        synchronized (lock) {
            while (backgroundHandler == null) {
                try {
                    lock.wait(WAIT_TIMEOUT_MS);
                } catch (InterruptedException ignore) {
                    break;
                }
            }
            return realm;
        }
    }

    /**
     * Hold a reference to an object, to prevent it from being GCed,
     * until after the test completes.
     * <p>
     * Accessed only from the main thread, here, but synchronized in case it is called from within a test.
     * Valid after {@code before}.
     */
    public void keepStrongReference(Object obj) {
        synchronized (lock) {
            keepStrongReference.add(obj);
        }
    }

    /**
     * Add a closable resource which this test will guarantee to call {@link Closeable#close()} on
     * when the tests is done.
     *
     * @param closeable {@link Closeable} to close.
     */
    public void closeAfterTest(Closeable closeable) {
        synchronized (lock) {
            closableResources.add(closeable);
        }
    }

    /**
     * Run this task after the unit test either failed or succeeded.
     * This is a work-around for the the current @After being triggered right after the unit test method exits,
     * but before the @RunTestInLooperThread has determined the test is done
     *
     * TODO: Consider replacing this pattern with `@AfterLooperTest` annotation.
     *
     * @param task task to run. Only one task can be provided
     */
    public void runAfterTest(Runnable task) {
        synchronized (lock) {
            runAfterTestIsComplete.add(task);
        }
    }

    /**
     * Add a Realm to be closed when test is complete.
     * <p>
     * Accessed from both test and main threads.
     * Valid after {@code before}.
     */
    public void addTestRealm(Realm realm) {
        synchronized (lock) {
            testRealms.add(realm);
        }
    }

    /**
     * Explicitly close all held realms.
     * <p>
     * 'testRealms' is accessed from both test and main threads.
     * 'testRealms' is valid after {@code before}.
     */
    public void closeTestRealms() {
        List<Realm> realms = new ArrayList<>();
        synchronized (lock) {
            List<Realm> tmp = testRealms;
            testRealms = realms;
            realms = tmp;
        }

        for (Realm testRealm : realms) {
            testRealm.close();
        }
    }

    /**
     * Posts a runnable to the currently running looper.
     */
    public void postRunnable(Runnable runnable) {
        getBackgroundHandler().post(runnable);
    }

    /**
     * Posts a runnable to this worker threads looper with a delay in milli second.
     */
    public void postRunnableDelayed(Runnable runnable, long delayMillis) {
        getBackgroundHandler().postDelayed(runnable, delayMillis);
    }

    /**
     * Signal that the test has completed.
     * <p>
     * Used on both the main and test threads.
     * Valid after {@code before}.
     */
    public void testComplete() {
        signalTestCompleted.countDown();
    }

    /**
     * Signal that the test has completed, after waiting for any additional latches.
     *
     * @param latches additional latches to wait on, before setting the test completed flag.
     */
    public void testComplete(CountDownLatch... latches) {
        for (CountDownLatch latch : latches) {
            TestHelper.awaitOrFail(latch);
        }
        testComplete();
    }

    // Accessed from both test and main threads
    // Valid after the test thread has started.
    private Handler getBackgroundHandler() {
        synchronized (lock) {
            while (backgroundHandler == null) {
                try {
                    lock.wait(WAIT_TIMEOUT_MS);
                } catch (InterruptedException ignore) {
                    break;
                }
            }
            return this.backgroundHandler;
        }
    }

    // Accessed from both test and main threads
    // Storing the handler is the gate that indicates that the test thread has started.
    void setBackgroundHandler(Handler backgroundHandler) {
        synchronized (lock) {
            this.backgroundHandler = backgroundHandler;
            lock.notifyAll();
        }
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        RealmConfiguration config = createConfiguration(UUID.randomUUID().toString());
        List<Object> refs = new ArrayList<>();
        List<Realm> realms = new ArrayList<>();
        List<Closeable> closeables = new ArrayList<>();

        synchronized (lock) {
            realmConfiguration = config;
            realm = null;
            backgroundHandler = null;
            keepStrongReference = refs;
            testRealms = realms;
            closableResources = closeables;
        }
    }

    @Override
    protected void after() {
        // Wait for all async tasks to have completed to ensure a successful deleteRealm call.
        // If it times out, it will throw.
        TestHelper.waitRealmThreadExecutorFinish();
        TestHelper.waitForNetworkThreadExecutorToFinish();
        AndroidCapabilities.EMULATE_MAIN_THREAD = false;
        super.after();

        // probably belt *and* suspenders...
        synchronized (lock) {
            backgroundHandler = null;
            keepStrongReference = null;
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        setTestName(description);
        final RunTestInLooperThread annotation = description.getAnnotation(RunTestInLooperThread.class);
        if (annotation == null) {
            return base;
        }
        synchronized (lock) {
            ruleBeingUsed = true;
        }
        return new RunInLooperThreadStatement(annotation, base);
    }

    /**
     * Tears down logic which is guaranteed to run after the looper test has either completed or failed.
     * This will run on the same thread as the looper test.
     */
    public void looperTearDown() {
    }

    private void initRealm() {
        synchronized (lock) {
            realm = Realm.getInstance(realmConfiguration);
        }
    }

    private void closeRealms() {
        closeTestRealms();

        Realm oldRealm;
        synchronized (lock) {
            oldRealm = realm;

            realm = null;
            realmConfiguration = null;
        }

        if (oldRealm != null) {
            oldRealm.close();
        }
    }

    private void closeResources() throws IOException {
        synchronized (lock) {
            for (Closeable cr : closableResources) {
                cr.close();
            }
        }
    }

    /**
     * Checks if the current test is considered completed or not.
     * It is completed if either {@link #testComplete()} was called or an uncaught exception was thrown.
     */
    public boolean isTestComplete() {
        synchronized (lock) {
            return signalTestCompleted.getCount() == 0;
        }
    }

    /**
     * Returns true if the current test being run is using this rule.
     */
    public boolean isRuleUsed() {
        return ruleBeingUsed;
    }

    /**
     * If an implementation of this is supplied with the annotation, the {@link RunnableBefore#run(RealmConfiguration)}
     * will be executed before the looper thread starts. It is normally for populating the Realm before the test.
     */
    public interface RunnableBefore {
        void run(RealmConfiguration realmConfig);
    }

    private class RunInLooperThreadStatement extends Statement {
        private final RunTestInLooperThread annotation;
        private final Statement base;

        RunInLooperThreadStatement(RunTestInLooperThread annotation, Statement base) {
            this.annotation = annotation;
            this.base = base;
        }

        @Override
        @SuppressWarnings("ClassNewInstance")
        public void evaluate() throws Throwable {
            before();

            Class<? extends RunnableBefore> runnableBefore = annotation.before();
            if (!runnableBefore.isInterface()) {
                // this is dangerous: newInstance can throw checked exceptions.
                // this is dangerous: config is mutable.
                runnableBefore.newInstance().run(getConfiguration());
            }

            AndroidCapabilities.EMULATE_MAIN_THREAD = annotation.emulateMainThread();
            runTest(annotation.threadName());
        }

        private void runTest(final String threadName) throws Throwable {
            Throwable failure = null;

            try {
                ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) { return new Thread(runnable, threadName); }
                });

                TestThread test = new TestThread(base);

                @SuppressWarnings({"UnusedAssignment", "unused"})
                Future<?> ignored = executorService.submit(test);

                TestHelper.exitOrThrow(executorService, signalTestCompleted, test);
            } catch (Throwable testfailure) {
                // These exceptions should only come from TestHelper.awaitOrFail()
                failure = testfailure;
            } finally {
                // Tries as hard as possible to close down gracefully, while still keeping all exceptions intact.
                failure = cleanUp(failure);
            }
            if (failure != null) {
                throw failure;
            }
        }

        private Throwable cleanUp(Throwable testfailure) {
            try {
                after();
                return testfailure;
            } catch (Throwable afterFailure) {
                if (testfailure == null) {
                    // Only after() threw an exception
                    return afterFailure;
                }

                // Both TestHelper.awaitOrFail() and after() threw exceptions
                return new MultipleFailureException(Arrays.asList(testfailure, afterFailure)) {
                    @Override
                    public void printStackTrace(PrintStream out) {
                        int i = 0;
                        for (Throwable t : getFailures()) {
                            out.println("Error " + i + ": " + t.getMessage());
                            t.printStackTrace(out);
                            out.println();
                            i++;
                        }
                    }
                };
            }
        }
    }

    private class TestThread implements Runnable, TestHelper.LooperTest {
        private final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        private final Statement base;
        private Looper looper;
        private Throwable threadAssertionError;

        TestThread(Statement base) {
            this.base = base;
        }

        @Override
        public CountDownLatch getRealmClosedSignal() {
            return signalClosedRealm;
        }

        @Override
        public synchronized Looper getLooper() {
            return looper;
        }

        private synchronized void setLooper(Looper looper) {
            this.looper = looper;
            setBackgroundHandler(new Handler(looper));
        }

        @Override
        public synchronized Throwable getAssertionError() {
            return threadAssertionError;
        }

        // Only record the first error
        private synchronized void setAssertionError(Throwable threadAssertionError) {
            if (this.threadAssertionError == null) {
                this.threadAssertionError = threadAssertionError;
            }
        }

        @Override
        public void run() {
            Looper.prepare();
            try {
                initRealm();
                setLooper(Looper.myLooper());
                base.evaluate();
                Looper.loop();
            } catch (Throwable t) {
                setAssertionError(t);
                setUnitTestFailed();
            } finally {
                try {
                    looperTearDown();
                    closeResources();
                    for (Runnable task : runAfterTestIsComplete) {
                        task.run();
                    }
                } catch (Throwable t) {
                    setAssertionError(t);
                    setUnitTestFailed();
                }
                testComplete();
                closeRealms();
                signalClosedRealm.countDown();
            }
        }
    }
}
