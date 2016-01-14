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

import android.os.Looper;

import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.TestHelper;


/**
 * Rule that runs the test inside a worker looper thread. This Rule is responsible
 * of creating a temp directory containing a Realm instance then delete it, once the test finishes.
 */
public class RunInLooperThread extends TemporaryFolder {
    public Realm realm;
    public RealmConfiguration realmConfiguration;
    public CountDownLatch signalTestCompleted;
    // the variables created inside the test are local and eligible for GC.
    // but sometimes we need the variables to survive across different Looper
    // events (Callbacks happening in the future), so we add a strong reference
    // to them for the duration of the test.
    public LinkedList<Object> keepStrongReference;

    @Override
    protected void before() throws Throwable {
        super.before();
        realmConfiguration = TestHelper.createConfiguration(getRoot(), Realm.DEFAULT_REALM_NAME);
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
                            try {
                                realm = Realm.getInstance(realmConfiguration);

                                base.evaluate();

                                Looper.loop();
                            } catch (Throwable e) {
                                threadAssertionError[0] = e;

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
                } finally {
                    after();
                }
            }
        };
    }
}
