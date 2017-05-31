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

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Custom thread pool settings, instances of this executor can be paused, and resumed, this will also set
 * appropriate number of Threads & wrap submitted tasks to set the thread priority according to
 * <a href="https://developer.android.com/training/multiple-threads/define-runnable.html"> Androids recommendation</a>.
 */
public class RealmThreadPoolExecutor extends ThreadPoolExecutor {
    private static final String SYS_CPU_DIR = "/sys/devices/system/cpu/";

    // Reduces context switching by using a number of thread proportionate to the number of cores.
    private static final int CORE_POOL_SIZE = calculateCorePoolSize();
    private static final int QUEUE_SIZE = 100;

    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();

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

    /**
     * Tries using the number of files named 'cpuNN' in sysfs to figure out the number of
     * processors on this device. `Runtime.getRuntime().availableProcessors()` may return
     * a smaller number when the device is sleeping.
     *
     * @return the number of threads to be allocated for the executor pool
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private static int calculateCorePoolSize() {
        int cpus = countFilesInDir(SYS_CPU_DIR, "cpu[0-9]+");
        if (cpus <= 0) {
            cpus = Runtime.getRuntime().availableProcessors();
        }
        return (cpus <= 0) ? 1 : (cpus * 2) + 1;
    }

    /**
     * @param dirPath a directory path
     * @param pattern a regex
     * @return the number of files, in the `dirPath` directory, whose names match `pattern`
     */
    private static int countFilesInDir(String dirPath, String pattern) {
        final Pattern filePattern = Pattern.compile(pattern);
        try {
            File[] files = new File(dirPath).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return filePattern.matcher(file.getName()).matches();
                }
            });
            return (files == null) ? 0 : files.length;
        } catch (SecurityException ignore) {
        }
        return 0;
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
        return future;
    }

    /**
     * Method invoked prior to executing the given Runnable to pause execution of the thread.
     *
     * @param t the thread that will run task r
     * @param r the task that will be executed
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) { unpaused.await(); }
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
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
     * Resumes executing any scheduled tasks.
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
