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

package io.realm.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.TestHelper;


/**
 * Utility class for running a task on a non-looper background thread.
 *
 * This class ensures that the background Realm is automatically closed no matter the outcome of
 * the test.
 *
 * Failures can be asserted using {@code task.checkFailure()}
 */
public abstract class RealmBackgroundTask {

    private final RealmConfiguration configuration;
    private final ExceptionHolder exceptionHolder = new ExceptionHolder();

    public RealmBackgroundTask(RealmConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Runs the task on a background thread. It will either return when it completes successfully or throw an
     * {@link junit.framework.AssertionFailedError} if it failed or timed out. The background task is limited to
     * 10 seconds after which it will time out.
     */
    public void awaitOrFail() {
        final CountDownLatch jobDone = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(configuration);
                try {
                    doInBackground(realm);
                } catch (Throwable throwable) {
                    exceptionHolder.setException(throwable);
                } finally {
                    realm.close();
                    jobDone.countDown();
                }
            }
        }, "RealmBackgroundTask").start();

        try {
            if (!jobDone.await(TestHelper.STANDARD_WAIT_SECS, TimeUnit.SECONDS)) {
                exceptionHolder.setError("Job timed out!");
            }
        } catch (InterruptedException e) {
            exceptionHolder.setException(e);
        }

        exceptionHolder.checkFailure();
    }

    /**
     * Executes the task. This method is called on a background thread.
     *
     * @param realm Realm instance created by the provided configuration.
     */
    protected abstract void doInBackground(Realm realm);
}
