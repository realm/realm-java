/*
 * Copyright 2020 Realm Inc.
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
package io.realm.rule

import android.os.Handler
import android.os.Looper
import io.realm.TestHelper
import io.realm.TestHelper.LooperTest
import io.realm.internal.android.AndroidCapabilities
import org.junit.runners.model.MultipleFailureException
import java.io.Closeable
import java.io.PrintStream
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList

/**
 * Helper class that makes it easier to run a piece of code inside a Looper Thread. This is done by
 * calling the `run` method. This method will block until the Looper either completes or throws
 * an exception.
 *
 * Usage:
 * ```
 * val lopperThread = LooperThreadTest()
 *
 * @Before
 * fun setUp() {
 *     // Runs before test
 * }
 *
 * @After
 * fun tearDown() {
 *     // Runs after test completed or failed
 * }
 *
 * @Test
 * fun myTest() = looperThread.run {
 *     // test code
 * }
 * ```
 */
class BlockingLooperThread {
    // lock protecting objects shared with the test thread
    private val lock = ReentrantLock()
    private var condition: CountDownLatch = CountDownLatch(1)

    // Thread safe
    private val signalTestCompleted = CountDownLatch(1)

    // Access guarded by 'lock'
    private var backgroundHandler: Handler? = null

    // the variables created inside the test are local and eligible for GC.
    // but sometimes we need the variables to survive across different Looper
    // events (Callbacks happening in the future), so we add a strong reference
    // to them for the duration of the test.
    // Access guarded by 'lock'
    private var keepStrongReference = ArrayList<Any>()

    // List of closable resources that will be automatically closed when the test finishes.
    // These will run before any methods marked with `@After`.
    // Access guarded by 'lock'
    private var closableResources = ArrayList<Closeable>()

    // Runnable guaranteed to trigger after the test either succeeded or failed.
    // These will run before any methods marked with `@After`.
    // Access guarded by 'lock'
    private val runAfterTestIsComplete = ArrayList<Runnable>()

    /**
     * Runs the test on a Looper thread
     */
    fun runBlocking(threadName: String = "TestLooperThread", emulateMainThread: Boolean = false, test: () -> Unit) {
        RunInLooperThreadStatement(threadName, emulateMainThread, test).evaluate()
    }

    /**
     * Runs the test on a Looper thread. Returns an object that can be used to wait for the test to complete
     */
    fun runDetached(threadName: String = "TestLooperThread", emulateMainThread: Boolean = false, test: () -> Unit): Condition {
        return RunInLooperThreadStatement(threadName, emulateMainThread, test).evaluateDetached()
    }

    /**
     * Hold a reference to an object, to prevent it from being GCed,
     * until after the test completes.
     *
     * Accessed only from the main thread, here, but synchronized in case it is called from within a test.
     */
    fun keepStrongReference(obj: Any) {
        synchronized(lock) { keepStrongReference.add(obj) }
    }

    /**
     * Add a closable resource which this test will guarantee to call [Closeable.close] on
     * when the tests is done.
     *
     * @param closeable [Closeable] to close.
     */
    fun closeAfterTest(closeable: Closeable) {
        synchronized(lock) { closableResources.add(closeable) }
    }

    /**
     * Posts a runnable to the currently running looper.
     */
    fun postRunnable(runnable: Runnable) {
        getBackgroundHandler().post(runnable)
    }

    /**
     * Posts a runnable to this worker threads looper with a delay in milli second.
     */
    fun postRunnableDelayed(runnable: Runnable, delayMillis: Long) {
        getBackgroundHandler().postDelayed(runnable, delayMillis)
    }

    /**
     * Signal that the test has completed.
     */
    fun testComplete() {
        // Close all resources and run any after test tasks
        // Post as runnable to ensure that this code runs on the correct thread.
        postRunnable(Runnable { closeTestResources() })
    }

    /**
     * Internal logic for shutting down a test.
     */
    private fun closeTestResources() {
        try {
            closeResources()
            for (task in runAfterTestIsComplete) {
                task.run()
            }
        } catch (t: Throwable) {
            throw AssertionError("Failed to close test resources correctly", t)
        } finally {
            signalTestCompleted.countDown()
        }
    }

    /**
     * Signal that the test has completed, after waiting for any additional latches.
     *
     * @param latches additional latches to wait on, before setting the test completed flag.
     */
    fun testComplete(vararg latches: CountDownLatch) {
        for (latch in latches) {
            TestHelper.awaitOrFail(latch)
        }
        testComplete()
    }

    private fun getBackgroundHandler(): Handler {
        synchronized(lock) {
            while (backgroundHandler == null) {
                try {
                    condition.await(5 * 1000, TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) {
                    throw AssertionError("Could not acquire the test handler.", e)
                }
            }
            return backgroundHandler!!
        }
    }

    // Accessed from both test and main threads
    // Storing the handler is the gate that indicates that the test thread has started.
    private fun setBackgroundHandler(backgroundHandler: Handler?) {
        synchronized(lock) {
            this.backgroundHandler = backgroundHandler
            condition.countDown()
        }
    }

    private fun before() {
        synchronized(lock) {
            backgroundHandler = null
            keepStrongReference.clear()
            closableResources.clear()
        }
    }

    private fun after() {
        // Wait for all async tasks to have completed to ensure a successful deleteRealm call.
        // If it times out, it will throw.
        TestHelper.waitRealmThreadExecutorFinish()
        TestHelper.waitForNetworkThreadExecutorToFinish()
        AndroidCapabilities.EMULATE_MAIN_THREAD = false

        // probably belt *and* suspenders...
        synchronized(lock) {
            backgroundHandler = null
            keepStrongReference.clear()
            condition = CountDownLatch(1)
        }
    }

    private fun closeResources() {
        synchronized(lock) {
            for (cr in closableResources) {
                cr.close()
            }
        }
    }

    inner class RunInLooperThreadStatement(private val threadName: String,
                                           private val emulateMainThread: Boolean,
                                           private val test: () -> Unit) {

        fun evaluate() {
            before()
            AndroidCapabilities.EMULATE_MAIN_THREAD = emulateMainThread
            runTest(threadName)
            after()
        }

        fun evaluateDetached(): Condition {
            before()
            AndroidCapabilities.EMULATE_MAIN_THREAD = emulateMainThread

            return runTestDetached(threadName)
        }

        private fun runTest(threadName: String) {
            var failure: Throwable? = null

            try {
                val executorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, threadName) }
                val test = TestThread(test)
                executorService.submit(test)
                TestHelper.exitOrThrow(executorService, signalTestCompleted, test)
            } catch (testFailure: Throwable) {
                // These exceptions should only come from TestHelper.awaitOrFail()
                failure = testFailure
            } finally {
                // Tries as hard as possible to close down gracefully, while still keeping all exceptions intact.
                failure = cleanUp(failure)
            }

            if (failure != null) {
                throw failure
            }
        }

        private fun runTestDetached(threadName: String): Condition {
            val executorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, threadName) }
            val test = TestThread(test)
            executorService.submit(test)

            return Condition(this, executorService, signalTestCompleted, test)
        }

        fun cleanUp(testfailure: Throwable?): Throwable? {
            return try {
                after()
                testfailure
            } catch (afterFailure: Throwable) {
                if (testfailure == null) {
                    // Only after() threw an exception
                    afterFailure
                } else object : MultipleFailureException(Arrays.asList(testfailure, afterFailure)) {
                    override fun printStackTrace(out: PrintStream) {
                        var i = 0
                        for (t in failures) {
                            out.println("Error " + i + ": " + t.message)
                            t.printStackTrace(out)
                            out.println()
                            i++
                        }
                    }
                }
            }
        }
    }

    inner class Condition(private val threadStatement: RunInLooperThreadStatement,
                          private val executorService: ExecutorService,
                          private val signalTestCompleted: CountDownLatch,
                          private val test: LooperTest) {

        fun await() {
            var failure: Throwable? = null

            try {
                TestHelper.exitOrThrow(executorService, signalTestCompleted, test)
            } catch (testFailure: Throwable) {
                // These exceptions should only come from TestHelper.awaitOrFail()
                failure = testFailure
            } finally {
                // Tries as hard as possible to close down gracefully, while still keeping all exceptions intact.
                failure = threadStatement.cleanUp(failure)
            }

            if (failure != null) {
                throw failure
            }
            TestHelper.exitOrThrow(executorService, signalTestCompleted, test)
            after()
        }
    }

    private inner class TestThread(private val test: () -> Unit) : Runnable, LooperTest {
        private var threadAssertionError: Throwable? = null
        private var looper: Looper? = null

        @Synchronized
        override fun getLooper(): Looper? {
            return looper
        }

        @Synchronized
        private fun setLooper(looper: Looper) {
            this.looper = looper
            setBackgroundHandler(Handler(looper))
        }

        @Synchronized
        override fun getAssertionError(): Throwable? {
            return threadAssertionError
        }

        // Only record the first error
        @Synchronized
        private fun setAssertionError(threadAssertionError: Throwable) {
            if (this.threadAssertionError == null) {
                this.threadAssertionError = threadAssertionError
            }
        }

        override fun run() {
            Looper.prepare()
            try {
                setLooper(Looper.myLooper()!!)
                test()
                Looper.loop()
            } catch (t: Throwable) {
                setAssertionError(t)
                // If an exception occurred, `looperThread.testComplete()` was probably no called.
                // Rerun it here, but ignore any failures as the first failure is more important.
                try {
                    closeTestResources()
                } catch (ignore: Throwable) {
                }
            }
        }
    }
}