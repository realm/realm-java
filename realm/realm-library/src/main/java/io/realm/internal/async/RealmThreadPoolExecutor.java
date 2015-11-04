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

    private static volatile RealmThreadPoolExecutor instance;

    public static RealmThreadPoolExecutor getInstance() {
           if (instance == null) {
               synchronized (RealmThreadPoolExecutor.class) {
                   if (instance == null) {
                       instance = new RealmThreadPoolExecutor();
                   }
               }
           }
        return instance;
    }

    private RealmThreadPoolExecutor() {
        super(CORE_POOL_SIZE, CORE_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS, //terminated idle thread
                new ArrayBlockingQueue<Runnable>(QUEUE_SIZE));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(new BgPriorityRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(new BgPriorityCallable<T>(task));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) unpaused.await();
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }

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
