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

package io.realm.internal.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadDispatcher implements Dispatcher {
    private final ExecutorService executorService;

    public ThreadDispatcher() {
        executorService =
                new ThreadPoolExecutor(
                        8,
                        32,
                        60,
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<Runnable>(),
                        Executors.defaultThreadFactory()
                );
    }

    @Override
    public <T> void dispatch(final Callable<T> callable) {
        executorService.submit(callable).isDone(); // this isDone tricks findBugs.
    }

    protected <T, U> U dispatch(
            final Callable<T> callable,
            final CallbackAsyncAdapter<T, Exception, U> callbackAdapter
    ) {
        dispatch(callable, (Callback<T, Exception>) callbackAdapter);
        return callbackAdapter.getAdapter();
    }

    private <T> void dispatch(final Callable<T> callable, final Callback<T, Exception> callback) {
        // future is ignored - the compiler will complain if not stored in a variable though
        Future<?> submit = executorService.submit(() -> {
            try {
                callback.onComplete(
                        OperationResult.<T, Exception>successfulResultOf(callable.call())
                );
            } catch (final Exception e) {
                callback.onComplete(OperationResult.<T, Exception>failedResultOf(e));
            }
        });
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }
}
