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

package io.realm.internal.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom thread pool settings, instances of this executor can be paused, and resumed, this will also set
 * appropriate number of Threads & wrap submitted tasks to set the thread priority according to
 * <a href="https://developer.android.com/training/multiple-threads/define-runnable.html"> Androids recommendation</a>.
 */
public class RealmThreadPoolExecutor extends ThreadPoolExecutor {
    // reduce context switch by using a number of thread proportionate to the number of cores
    // from AOSP https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/os/AsyncTask.java#182
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;
    private static final int QUEUE_SIZE = 100;

    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();
    private List<Future<?>> transactions = Collections.synchronizedList(new ArrayList<Future<?>>());

    /**
     * Creates a default RealmThreadPool that is bounded by the number of available cores.
     */
    public static RealmThreadPoolExecutor newDefaultExecutor() {
        return new RealmThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE);
    }

    /**
     * Creates a RealmThreadPool with only 1 thread. This is primarily useful for testing.
     */
    public static RealmThreadPoolExecutor newSingleThreadExecutor() {
        return new RealmThreadPoolExecutor(1, 1);
    }

    private RealmThreadPoolExecutor(int corePoolSize, int maxPoolSize) {
        super(corePoolSize, maxPoolSize,
                0L, TimeUnit.MILLISECONDS, //terminated idle thread
                new ArrayBlockingQueue<Runnable>(QUEUE_SIZE));
    }

    /**
     * Submits a runnable for executing a transaction.
     *
     * @param task the task to submit
     * @return a future representing pending completion of the task
     */
    public Future<?> submitTransaction(Runnable task) {
        Future<?> future = super.submit(new BgPriorityRunnable(task));
        transactions.add(future);
        return future;
    }

    /**
     * Submits a runnable for updating a query.
     *
     * @param task the task to submit
     * @return a future representing pending completion of the task
     */
    public Future<?> submitQueryUpdate(Runnable task) {
        return super.submit(new BgPriorityRunnable(task));
    }

    /**
     * Submits a runnable for executing a query.
     *
     * @param task the task to submit
     * @return a future representing pending completion of the task
     */
    public <T> Future<T> submitQuery(Callable<T> task) {
        return super.submit(new BgPriorityCallable<T>(task));
    }

    /**
     * Method invoked prior to executing the given Runnable to pause execution of the thread.
     *
     * @param t the thread that will run task r
     * @param r the task that will be executed
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        pauseLock.lock();
        try {
            while (isPaused) unpaused.await();
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
        super.beforeExecute(t, r);
    }

    /**
     * Method invoked upon completion of execution to clean up pending transactions.
     *
     * @param r the thread that will run task r
     * @param t the exception that caused termination, or null if execution completed normally
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        cleanupTransactions();
    }

    /**
     * Checks whether there are pending transactions.
     *
     * @return true if there are pending transactions
     */
    public boolean hasPendingTransactions() {
        cleanupTransactions();
        return !transactions.isEmpty();
    }

    private void cleanupTransactions() {
        Iterator<Future<?>> iterator = transactions.iterator();
        if (iterator.hasNext()) {
            Future<?> future = iterator.next();
            if (future.isCancelled() || future.isDone()) {
                iterator.remove();
            }
        }
    }

    /**
     * Pauses the executor. Pausing means the executor will stop starting new tasks (but complete current ones).
     */
    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }

    /**
     * Resumes the executor. The executor will start executing new tasks.
     */
    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }
}
