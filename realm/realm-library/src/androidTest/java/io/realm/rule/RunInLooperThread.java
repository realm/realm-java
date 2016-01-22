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
import org.junit.runners.model.Statement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.TestHelper;

import static org.junit.Assert.fail;

/**
 * Rule that runs the test inside a worker looper thread. This rule is responsible
 * of creating a temp directory containing a Realm instance then delete it, once the test finishes.
 *
 * All Realms used in a method method annotated with {@code @RunTestInLooperThread } should use
 * {@link RunInLooperThread#createConfiguration()} and friends to create their configurations. Failing to do so can
 * result in the test failing because the Realm could not be deleted (Reason is that {@link TestRealmConfigurationFactory}
 * and this class does not agree in which order to delete all open Realms.
 */
public class RunInLooperThread extends TestRealmConfigurationFactory {
    public Realm realm;
    public RealmConfiguration realmConfiguration;
    private CountDownLatch signalTestCompleted;
    private Handler backgroundHandler;

    // the variables created inside the test are local and eligible for GC.
    // but sometimes we need the variables to survive across different Looper
    // events (Callbacks happening in the future), so we add a strong reference
    // to them for the duration of the test.
    public LinkedList<Object> keepStrongReference;

    @Override
    protected void before() throws Throwable {
        super.before();
        realmConfiguration = createConfiguration(UUID.randomUUID().toString());
        signalTestCompleted = new CountDownLatch(1);
        keepStrongReference = new LinkedList<Object>();
    }

    @Override
    protected void after() {
        super.after();
        realmConfiguration = null;
        realm = null;
        keepStrongReference = null;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        if (description.getAnnotation(RunTestInLooperThread.class) == null) {
            return base;
        }
        return new Statement() {
            public Throwable testError;

            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    final CountDownLatch signalClosedRealm = new CountDownLatch(1);
                    final Throwable[] threadAssertionError = new Throwable[1];
                    final Looper[] backgroundLooper = new Looper[1];
                    final ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            backgroundLooper[0] = Looper.myLooper();
                            backgroundHandler = new Handler(backgroundLooper[0]);
                            try {
                                realm = Realm.getInstance(realmConfiguration);
                                base.evaluate();
                                Looper.loop();
                            } catch (Throwable e) {
                                threadAssertionError[0] = e;
                                unitTestFailed = true;
                            } finally {
                                if (signalTestCompleted.getCount() > 0) {
                                    signalTestCompleted.countDown();
                                }
                                if (realm != null) {
                                    realm.close();
                                }
                                signalClosedRealm.countDown();
                            }
                        }
                    });
                    TestHelper.exitOrThrow(executorService, signalTestCompleted, signalClosedRealm, backgroundLooper, threadAssertionError);
                } catch (Throwable error) {
                    testError = error;
                } finally {
                    try {
                        after();
                    } catch (Throwable e) {
                        if (testError != null) {
                            // after() threw an error that will mask an original error from the test case
                            // Make sure that user is aware of that fact, but printing both errors.
                            StringWriter testStackTrace = new StringWriter();
                            testError.printStackTrace(new PrintWriter(testStackTrace));

                            StringWriter aftertStackTrace = new StringWriter();
                            e.printStackTrace(new PrintWriter(aftertStackTrace));

                            StringBuilder errorMessage = new StringBuilder();
                            errorMessage.append("after() threw an error that shadows a test case error");
                            errorMessage.append('\n');
                            errorMessage.append("== Test case exception ==\n");
                            errorMessage.append(testStackTrace.toString());
                            errorMessage.append('\n');
                            errorMessage.append("== after() exception ==\n");
                            errorMessage.append(aftertStackTrace.toString());
                            fail(errorMessage.toString());
                        }
                    }
                }
            }
        };
    }

    /**
     * Signal that the test has completed.
     */
    public void testComplete() {
        signalTestCompleted.countDown();
    }

    /**
     * Posts a runnable to this worker threads looper.
     */
    public void postRunnable(Runnable runnable) {
        backgroundHandler.post(runnable);
    }
}
